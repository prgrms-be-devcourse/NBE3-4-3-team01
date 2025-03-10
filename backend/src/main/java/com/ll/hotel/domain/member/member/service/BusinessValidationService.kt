package com.ll.hotel.domain.member.member.service

import com.ll.hotel.domain.member.member.dto.request.BusinessRequest
import com.ll.hotel.domain.member.member.dto.response.BusinessResponse
import com.ll.hotel.domain.member.member.type.BusinessApiProperties
import com.ll.hotel.global.exceptions.ErrorCode
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URI

@Service
class BusinessValidationService(
    private val restTemplate: RestTemplate,
    private val properties: BusinessApiProperties
) {
    fun validateBusiness(registrationInfo: BusinessRequest.RegistrationInfo) {
        val apiUrl = "${properties.validationUrl}?serviceKey=${properties.serviceKey}"
        val uri: URI = URI.create(apiUrl)

        val registrationApiForm = BusinessRequest.RegistrationApiForm.from(registrationInfo)

        try {
            val responseEntity = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                HttpEntity(registrationApiForm, createHeaders()),
                BusinessResponse.VerificationResponse::class.java
            )

            val valid = responseEntity.body?.data?.firstOrNull()?.valid.orEmpty()

            when (valid) {
                "01" -> return
                "02" -> throw ErrorCode.INVALID_BUSINESS_INFO.throwServiceException()
                else -> throw ErrorCode.EXTERNAL_API_UNEXPECTED_RESPONSE.throwServiceException()
            }

        } catch (e: Exception) {
            throw ErrorCode.EXTERNAL_API_COMMUNICATION_ERROR.throwServiceException(e)
        }
    }

    private fun createHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
    }
}