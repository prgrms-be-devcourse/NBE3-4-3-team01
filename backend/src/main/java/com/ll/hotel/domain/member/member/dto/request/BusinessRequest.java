package com.ll.hotel.domain.member.member.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class BusinessRequest {
        public record RegistrationInfo(
                @NotBlank(message = "사업자 등록 번호는 필수 항목입니다.")
                @Pattern(regexp = "^[0-9]{10}$", message = "사업자 등록 번호는 10자리 숫자여야 합니다.")
                String businessRegistrationNumber,

                @NotNull(message = "개업 일자는 필수 항목입니다.")
                @PastOrPresent(message = "개업 일자는 현재 날짜 또는 과거여야 합니다.")
                LocalDate startDate,

                @NotBlank(message = "대표자명은 필수 항목입니다.")
                @Size(max = 30, message = "대표자명은 최대 30자까지 가능합니다.")
                String ownerName
        ) {}

        public record RegistrationApiForm(
                List<Map<String, String>> businesses
        ) {
                public static RegistrationApiForm from(RegistrationInfo registrationInfo) {
                        return new RegistrationApiForm(List.of(Map.of(
                                "b_no", registrationInfo.businessRegistrationNumber(),
                                "start_dt", registrationInfo.startDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                                "p_nm", registrationInfo.ownerName()
                        )));
                }
        }
}

