package com.ll.hotel.domain.booking.payment.service

import com.ll.hotel.domain.booking.booking.dto.BookingRequest
import com.ll.hotel.domain.booking.payment.dto.*
import com.ll.hotel.domain.booking.payment.entity.Payment
import com.ll.hotel.domain.booking.payment.repository.PaymentRepository
import com.ll.hotel.domain.booking.payment.type.PaymentStatus
import com.ll.hotel.global.exceptions.ErrorCode
import com.ll.hotel.global.exceptions.ServiceException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import com.ll.hotel.standard.util.Ut
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository
) {
    private val log = LoggerFactory.getLogger(PaymentService::class.java)

    /*
     * portone api 호출에 필요한 keys
     * application-api-keys.yml에 저장된 값을 가져옴
     */
    @Value("\${api-keys.portone.impKey}")
    private lateinit var impKey: String

    @Value("\${api-keys.portone.impSecret}")
    private lateinit var impSecret: String

    @Value("\${api-keys.portone.apiId}")
    private lateinit var apiId: String

    @Value("\${api-keys.portone.channel-key}")
    private lateinit var channelKey: String

    private val UID_GENERATE_LENGTH = 10

    // Uid 생성
    fun generateMerchantUid(): UidResponse {
        val merchantUid = Ut.random.generateUID(UID_GENERATE_LENGTH)
        if (paymentRepository.existsByMerchantUid(merchantUid)) {
            throw ErrorCode.PAYMENT_UID_FAIL.throwServiceException()
        }
        return UidResponse(apiId, channelKey, merchantUid)
    }

    /*
     * 결제 정보 저장
     * BookingService에서 호출하여 예약 정보 저장과 동시에 처리하도록 함
     */
    @Transactional
    fun create(bookingRequest: BookingRequest): Payment {
        try {
            val paymentRequest = PaymentRequest.from(bookingRequest)

            // Unix Timestamp를 LocalDateTime으로 변환
            val paidAt = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(paymentRequest.paidAtTimestamp),
                ZoneId.systemDefault()
            )

            val payment = Payment(paymentRequest.merchantUid, paymentRequest.amount, paidAt)
            return paymentRepository.save(payment)
        } catch (e: ServiceException) {
            throw e
        } catch (e: Exception) {
            throw ErrorCode.PAYMENT_CREATE_FAIL.throwServiceException(e)
        }
    }

    /*
     * 결제 취소
     * access token 발급 후 결제 취소 api 호출
     * portone api를 통해 결제는 취소하지만 데이터를 삭제하지는 않음 (soft delete)
     * paymentStatus를 CANCELLED로 변경하여 취소로 처리
     */
    @Transactional
    fun softDelete(payment: Payment): Payment {
        // 이미 취소되었을 경우
        if (payment.paymentStatus == PaymentStatus.CANCELLED) {
            throw ErrorCode.PAYMENT_CANCEL_TO_CANCEL.throwServiceException()
        }

        val webClient = WebClient.create("https://api.iamport.kr")
        val accessToken = getAccessToken(webClient)

        return cancelPayment(webClient, accessToken, payment)
    }

    // access token 발급
    fun getAccessToken(webClient: WebClient): String {
        try {
            val tokenResponse = webClient.post()
                .uri("/users/getToken")
                .header("Content-Type", "application/json")
                .bodyValue(TokenRequest(impKey, impSecret))
                .retrieve()
                .toEntity(TokenResponse::class.java)
                .block()

            if (tokenResponse?.statusCode?.is2xxSuccessful == true) {
                return tokenResponse.body?.response?.accessToken
                    ?: throw ErrorCode.PAYMENT_TOKEN_FORBIDDEN.throwServiceException()
            } else {
                throw ErrorCode.PAYMENT_TOKEN_FORBIDDEN.throwServiceException()
            }
        } catch (e: ServiceException) {
            throw e
        } catch (e: Exception) {
            throw ErrorCode.PAYMENT_TOKEN_FAIL.throwServiceException(e)
        }
    }

    // 결제 취소
    @Transactional
    fun cancelPayment(webClient: WebClient, accessToken: String, payment: Payment): Payment {
        try {
            // 취소 api 호출
            val merchantUid = payment.merchantUid
            val response = webClient.post()
                .uri("/payments/cancel")
                .header("Authorization", "Bearer $accessToken")
                .bodyValue(mapOf("merchant_uid" to merchantUid))
                .retrieve()
                .toEntity(Void::class.java) // 본문 무시
                .block()

            // 취소 상태 변경
            if (response?.statusCode?.is2xxSuccessful == true) {
                payment.paymentStatus = PaymentStatus.CANCELLED
                return paymentRepository.save(payment)
            } else {
                throw ErrorCode.PAYMENT_CANCEL_FORBIDDEN.throwServiceException()
            }
        } catch (e: ServiceException) {
            throw e
        } catch (e: Exception) {
            throw ErrorCode.PAYMENT_CANCEL_FAIL.throwServiceException(e)
        }
    }

    // 기본 조회 메서드
    fun findById(paymentId: Long): Payment {
        return paymentRepository.findById(paymentId)
            .orElseThrow { ErrorCode.PAYMENT_NOT_FOUND.throwServiceException() }
    }
}