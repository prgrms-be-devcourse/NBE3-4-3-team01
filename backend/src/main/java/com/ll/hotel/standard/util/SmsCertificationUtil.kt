package com.ll.hotel.standard.util

import jakarta.annotation.PostConstruct
import net.nurigo.sdk.NurigoApp
import net.nurigo.sdk.message.model.Message
import net.nurigo.sdk.message.request.SingleMessageSendingRequest
import net.nurigo.sdk.message.response.SingleMessageSentResponse
import net.nurigo.sdk.message.service.DefaultMessageService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class SmsCertificationUtil(
    private val environment: Environment
) {
    private val logger = LoggerFactory.getLogger(SmsCertificationUtil::class.java)

    @Value("\${coolsms.apikey}")
    private lateinit var apiKey: String

    @Value("\${coolsms.apisecret}")
    private lateinit var apiSecret: String

    @Value("\${coolsms.fromnumber}")
    private lateinit var fromNumber: String
    
    // SMS 발송 모드 설정
    // true: 실제 SMS 발송 (운영 모드)
    // false: SMS 발송 생략 (테스트 모드)
    // 운영 모드 발송 시 크레딧이 소모되는 방식이라, false 로 설정한 뒤 콘솔에서 인증번호 받아서 테스트 해주세요.
    private var productionMode = false

    private lateinit var messageService: DefaultMessageService

    @PostConstruct
    fun init() {
        logger.debug("CoolSMS 초기화: apiKey={}, apiSecret={}, fromNumber={}", 
            apiKey.substring(0, 4) + "****", 
            apiSecret.substring(0, 4) + "****", 
            fromNumber)
        this.messageService = NurigoApp.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr")
        
        val profiles = environment.activeProfiles.joinToString()
        logger.debug("현재 활성화된 프로필: {}, SMS 발송 모드: {}",
            profiles, if(productionMode) "실제 발송" else "테스트 모드")
    }
    
    // 운영 모드 설정 메서드 (추후 확장성 고려, 관리자 페이지에서 처리하는 방법 등)
    fun setProductionMode(mode: Boolean) {
        productionMode = mode
        logger.debug("SMS 발송 모드 변경: {}", if(mode) "실제 발송" else "테스트 모드")
    }
    
    fun isProductionMode(): Boolean {
        return productionMode
    }

    fun sendSMS(to: String, certificationCode: String): SingleMessageSentResponse? {
        logger.debug("SMS 메시지 생성: to={}, certificationCode={}", to, certificationCode)
        
        // 운영 모드가 아니면 SMS 발송 생략
        if (!productionMode) {
            logger.debug("테스트 모드 - SMS 발송 생략: 전화번호={}, 인증번호={}", to, certificationCode)
            println("======================================================")
            println("📱 SMS 인증번호: ${certificationCode} (전화번호: ${to})")
            println("======================================================")
            
            logger.debug("테스트 모드 - SMS 발송 성공으로 처리")
            return null
        }
        
        // 운영 모드에서는 실제 SMS 발송
        logger.debug("운영 모드 - 실제 SMS 발송")
        val message = Message()
        message.from = fromNumber
        message.to = to
        message.text = "[서울호텔] 회원가입 인증번호 [${certificationCode}]를 입력해주세요."
        
        try {
            logger.debug("SMS 발송 요청: from={}, to={}", fromNumber, to)
            val response = this.messageService.sendOne(SingleMessageSendingRequest(message))
            logger.debug("SMS 발송 성공: response={}", response)
            return response
        } catch (e: Exception) {
            logger.error("SMS 발송 실패: {}", e.message, e)
            throw e
        }
    }
} 