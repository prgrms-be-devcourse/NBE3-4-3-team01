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
    fun validateBusiness(registrationInfo: BusinessRequest.RegistrationInfo): String {
        val apiUrl: String = "${properties.validationUrl}?serviceKey=${properties.serviceKey}"
        val uri: URI = URI.create(apiUrl)

        val registrationApiForm = BusinessRequest.RegistrationApiForm.from(registrationInfo)

        try {
            val responseEntity = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                HttpEntity(registrationApiForm, createHeaders()),
                BusinessResponse.Verification::class.java
            )

            val response = responseEntity.body

            if (response?.data.isNullOrEmpty()) {
                ErrorCode.EXTERNAL_API_UNEXPECTED_RESPONSE.throwServiceException()
            }

            val result = response?.data?.firstOrNull()
                ?: ErrorCode.EXTERNAL_API_UNEXPECTED_RESPONSE.throwServiceException()

            return result["valid"] as? String
                ?: ErrorCode.EXTERNAL_API_UNEXPECTED_RESPONSE.throwServiceException()

        } catch (e: Exception) {
            ErrorCode.EXTERNAL_API_COMMUNICATION_ERROR.throwServiceException(e)
        }
    }

    private fun createHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
    }
}