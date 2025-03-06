package com.ll.hotel.domain.booking.payment.service;

import com.ll.hotel.domain.booking.booking.dto.BookingRequest;
import com.ll.hotel.domain.booking.payment.dto.*;
import com.ll.hotel.domain.booking.payment.entity.Payment;
import com.ll.hotel.domain.booking.payment.repository.PaymentRepository;
import com.ll.hotel.domain.booking.payment.type.PaymentStatus;
import com.ll.hotel.global.exceptions.ErrorCode;
import com.ll.hotel.global.exceptions.ServiceException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import com.ll.hotel.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    /*
     * portone api 호출에 필요한 keys
     * application-api-keys.yml에 저장된 값을 가져옴
     */
    @Value("${api-keys.portone.impKey}")
    private String impKey;
    @Value("${api-keys.portone.impSecret}")
    private String impSecret;
    @Value("${api-keys.portone.apiId}")
    private String apiId;
    @Value("${api-keys.portone.channel-key}")
    private String channelKey;

    private final int UID_GENERATE_LENGTH = 10;
    private final PaymentRepository paymentRepository;

    // Uid 생성
    public UidResponse generateMerchantUid() {
        String merchantUid = Ut.random.generateUID(UID_GENERATE_LENGTH);
        if (paymentRepository.existsByMerchantUid(merchantUid)) {
            throw ErrorCode.PAYMENT_UID_FAIL.throwServiceException();
        }
        return new UidResponse(apiId, channelKey, merchantUid);
    }

    /*
     * 결제 정보 저장
     * BookingService에서 호출하여 예약 정보 저장과 동시에 처리하도록 함
     */
    @Transactional
    public Payment create(BookingRequest bookingRequest) {
        try {
            PaymentRequest paymentRequest = PaymentRequest.from(bookingRequest);

            // Unix Timestamp를 LocalDateTime으로 변환
            LocalDateTime paidAt = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(paymentRequest.paidAtTimestamp()),
                    ZoneId.systemDefault()
            );

            Payment payment = Payment.builder()
                    .merchantUid(paymentRequest.merchantUid())
                    .amount(paymentRequest.amount())
                    .paidAt(paidAt)
                    .build();

            return paymentRepository.save(payment);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw ErrorCode.PAYMENT_CREATE_FAIL.throwServiceException(e);
        }
    }

    /*
     * 결제 취소
     * access token 발급 후 결제 취소 api 호출
     * portone api를 통해 결제는 취소하지만 데이터를 삭제하지는 않음 (soft delete)
     * paymentStatus를 CANCELLED로 변경하여 취소로 처리
     */
    @Transactional
    public Payment softDelete(Payment payment) {
        // 이미 취소되었을 경우
        if (payment.getPaymentStatus() == PaymentStatus.CANCELLED) {
            throw ErrorCode.PAYMENT_CANCEL_TO_CANCEL.throwServiceException();
        }

        WebClient webClient = WebClient.create("https://api.iamport.kr");
        String accessToken = getAccessToken(webClient);

        return cancelPayment(webClient, accessToken, payment);
    }

    // access token 발급
    public String getAccessToken(WebClient webClient) {
        try {
            ResponseEntity<TokenResponse> tokenResponse = webClient.post()
                    .uri("/users/getToken")
                    .header("Content-Type", "application/json")
                    .bodyValue(new TokenRequest(impKey, impSecret))
                    .retrieve()
                    .toEntity(TokenResponse.class)
                    .block();

            if (tokenResponse.getStatusCode().is2xxSuccessful()) {
                return tokenResponse.getBody().response().accessToken();
            } else {
                throw ErrorCode.PAYMENT_TOKEN_FORBIDDEN.throwServiceException();
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw ErrorCode.PAYMENT_TOKEN_FAIL.throwServiceException(e);
        }
    }

    // 결제 취소
    @Transactional
    public Payment cancelPayment(WebClient webClient, String accessToken, Payment payment) {
        try {
            // 취소 api 호출
            String merchantUid = payment.getMerchantUid();
            ResponseEntity<Void> response = webClient.post()
                    .uri("/payments/cancel")
                    .header("Authorization", "Bearer " + accessToken)
                    .bodyValue(Map.of("merchant_uid", merchantUid))
                    .retrieve()
                    .toEntity(Void.class) // 본문 무시
                    .block();

            // 취소 상태 변경
            if (response.getStatusCode().is2xxSuccessful()) {
                payment.setPaymentStatus(PaymentStatus.CANCELLED);
                return paymentRepository.save(payment);
            } else {
                throw ErrorCode.PAYMENT_CANCEL_FORBIDDEN.throwServiceException();
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw ErrorCode.PAYMENT_CANCEL_FAIL.throwServiceException(e);
        }
    }

    // 기본 조회 메서드
    public Payment findById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> ErrorCode.PAYMENT_NOT_FOUND.throwServiceException());
    }
}