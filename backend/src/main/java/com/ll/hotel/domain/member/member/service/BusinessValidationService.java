package com.ll.hotel.domain.member.member.service;

import com.ll.hotel.domain.member.member.dto.request.BusinessRequest;
import com.ll.hotel.domain.member.member.dto.response.BusinessResponse;
import com.ll.hotel.domain.member.member.type.BusinessApiProperties;
import com.ll.hotel.global.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BusinessValidationService {
    private final RestTemplate restTemplate;
    private final BusinessApiProperties properties;

    public String validateBusiness(BusinessRequest.RegistrationInfo registrationInfo) {
        String apiUrl = properties.getValidationUrl() + "?serviceKey=" + properties.getServiceKey();
        URI uri = URI.create(apiUrl);

        BusinessRequest.RegistrationApiForm registrationApiForm = BusinessRequest.RegistrationApiForm.from(registrationInfo);

        try {
            ResponseEntity<BusinessResponse.Verification> responseEntity = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    new HttpEntity<>(registrationApiForm, createHeaders()),
                    BusinessResponse.Verification.class
            );

            BusinessResponse.Verification response = responseEntity.getBody();

            if (response == null || response.data() == null || response.data().isEmpty()) {
                ErrorCode.EXTERNAL_API_UNEXPECTED_RESPONSE.throwServiceException();
            }

            Map<String, Object> result = response.data().getFirst();
            return (String) result.get("valid");

        } catch (Exception e) {
            throw ErrorCode.EXTERNAL_API_COMMUNICATION_ERROR.throwServiceException(e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
