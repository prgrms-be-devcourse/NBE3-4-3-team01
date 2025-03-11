package com.ll.hotel.domain.member.member.service

import com.ll.hotel.domain.member.member.dto.SmsResponseDto
import com.ll.hotel.domain.member.member.repository.MemberRepository
import com.ll.hotel.domain.member.member.repository.SmsCertificationRepository
import com.ll.hotel.global.exceptions.ErrorCode.*
import com.ll.hotel.standard.util.SmsCertificationUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class SmsServiceImpl(
    private val smsCertificationUtil: SmsCertificationUtil,
    private val smsCertificationDao: SmsCertificationRepository,
    private val memberRepository: MemberRepository
) : SmsService {

    private val logger = LoggerFactory.getLogger(SmsServiceImpl::class.java)

    override fun sendSms(phoneNumber: String): SmsResponseDto {
        logger.debug("SMS 발송 요청: phoneNumber={}", phoneNumber)

        // 휴대폰 번호당 가입 제한 체크
        if (!checkPhoneNumberLimit(phoneNumber)) {
            logger.debug("휴대폰 번호 제한 초과: phoneNumber={}", phoneNumber)
            SMS_PHONE_NUMBER_LIMIT_EXCEEDED.throwServiceException()
        }

        try {
            // 랜덤 인증번호 생성 (6자리)
            val certificationCode = Random.nextInt(100000, 1000000).toString()
            logger.debug("인증번호 생성: certificationCode={}", certificationCode)

            // SMS 발송
            logger.debug("SMS 발송 시도: phoneNumber={}", phoneNumber)
            val response = smsCertificationUtil.sendSMS(phoneNumber, certificationCode)
            // 개발 환경 테스트 예외 처리
            if (response == null) {
                logger.debug("개발 환경 - SMS 발송 생략")
            } else {
                logger.debug("SMS 발송 결과: response={}", response)
            }

            // Redis에 저장 (3분 유효)
            smsCertificationDao.createSmsCertification(phoneNumber, certificationCode)
            logger.debug("Redis에 인증번호 저장 완료")

            return SmsResponseDto(true, "인증번호가 발송되었습니다.")
        } catch (e: Exception) {
            logger.error("SMS 발송 실패: {}", e.message, e)
            throw SMS_SEND_FAILED.throwServiceException(e)
        }
    }
    
    override fun verifySms(phoneNumber: String, code: String): SmsResponseDto {
        logger.debug("인증번호 확인 요청: phoneNumber={}, code={}", phoneNumber, code)
        val storedCode = smsCertificationDao.getSmsCertification(phoneNumber)
        logger.debug("저장된 인증번호: storedCode={}", storedCode)
        
        if (storedCode != null && storedCode == code) {
            // 인증 성공 시 Redis에서 삭제
            smsCertificationDao.removeSmsCertification(phoneNumber)
            logger.debug("인증 성공: Redis에서 인증번호 삭제")
            return SmsResponseDto(true, "인증이 완료되었습니다.")
        } else {
            logger.debug("인증 실패: 인증번호 불일치 또는 만료")
            throw SMS_CODE_MISMATCH.throwServiceException()
        }
    }
    
    override fun checkPhoneNumberLimit(phoneNumber: String, limit: Int): Boolean {
        val count = memberRepository.countByMemberPhoneNumber(phoneNumber)
        logger.debug("휴대폰 번호 가입 수 확인: phoneNumber={}, count={}, limit={}", phoneNumber, count, limit)
        return count < limit
    }
} 