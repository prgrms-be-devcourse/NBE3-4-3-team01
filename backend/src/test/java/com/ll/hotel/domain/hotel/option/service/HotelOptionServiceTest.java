package com.ll.hotel.domain.hotel.option.service;

import com.ll.hotel.domain.hotel.option.dto.request.OptionRequest;
import com.ll.hotel.domain.hotel.option.entity.HotelOption;
import com.ll.hotel.domain.hotel.option.repository.HotelOptionRepository;
import com.ll.hotel.global.exceptions.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class HotelOptionServiceTest {
    @Autowired
    private HotelOptionService hotelOptionService;

    @Autowired
    private HotelOptionRepository hotelOptionRepository;

    private Long testId;

    @BeforeEach
    void setUp() {
        HotelOption hotelOption = hotelOptionRepository.save(HotelOption
                .builder()
                .name("호텔 옵션")
                .build()
        );
        testId = hotelOption.getId();
    }

    @Test
    @DisplayName("호텔 옵션 추가")
    void addHotelOptionTest() {
        // Given
        OptionRequest optionRequest = new OptionRequest(
                "추가 테스트");

        // When
        HotelOption result = hotelOptionService.add(optionRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(optionRequest.name());

        // DB에 실제로 저장된 값 검증
        Optional<HotelOption> savedHotelOption = hotelOptionRepository.findById(result.getId());
        assertThat(savedHotelOption).isPresent();
        assertThat(savedHotelOption.get().getName()).isEqualTo(optionRequest.name());
    }

    @Test
    @DisplayName("호텔 옵션 전체 조회")
    void findAllHotelOptionsTest() {
        // Given
        int beforeSize = hotelOptionService.findAll().size();

        OptionRequest request1 = new OptionRequest("추가 옵션1");
        OptionRequest request2 = new OptionRequest("추가 옵션2");

        hotelOptionService.add(request1);
        hotelOptionService.add(request2);

        // When
        List<HotelOption> result = hotelOptionService.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat((long) result.size()).isEqualTo(beforeSize + 2);
    }

    @Test
    @DisplayName("호텔 옵션 수정")
    void modifyHotelOptionTest() {
        // Given
        HotelOption hotelOption = hotelOptionService.findById(testId);
        OptionRequest modifiedRequest = new OptionRequest("수정 후 테스트");

        // When
        hotelOptionService.modify(hotelOption, modifiedRequest);

        HotelOption result = hotelOptionService.findById(hotelOption.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(modifiedRequest.name());
    }

    @Test
    @DisplayName("호텔 옵션 삭제")
    void deleteHotelOptionTest() {
        // Given
        HotelOption hotelOption = hotelOptionService.findById(testId);

        // When
        hotelOptionService.delete(hotelOption);

        // Then
        assertThatThrownBy(() -> hotelOptionService.findById(testId))
                .isInstanceOf(ServiceException.class);
    }
}
