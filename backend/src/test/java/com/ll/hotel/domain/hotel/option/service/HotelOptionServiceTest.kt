package com.ll.hotel.domain.hotel.option.service

import com.ll.hotel.domain.hotel.option.dto.request.OptionRequest
import com.ll.hotel.domain.hotel.option.dto.response.OptionResponse
import com.ll.hotel.domain.hotel.option.entity.HotelOption
import com.ll.hotel.domain.hotel.option.repository.HotelOptionRepository
import com.ll.hotel.global.exceptions.ErrorCode
import com.ll.hotel.global.exceptions.ServiceException
import jakarta.transaction.Transactional
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class HotelOptionServiceTest (
    private val hotelOptionService: HotelOptionService,
    private val hotelOptionRepository: HotelOptionRepository
) {
    private var testId: Long = 0L

    @BeforeEach
    fun setUp() {
        testId = hotelOptionRepository.save(HotelOption("호텔 옵션")).id
    }

    @Test
    @DisplayName("호텔 옵션 추가")
    fun `should add a new hotel option`() {
        // Given
        val optionRequest = OptionRequest("추가 테스트")

        // When
        val result: OptionResponse = hotelOptionService.add(optionRequest)

        // Then
        assertThat(result).extracting { it.name }.isEqualTo(optionRequest.name)

        // DB에 저장된 값 검증
        val savedOption: HotelOption = hotelOptionRepository.findById(result.optionId).get()
        assertThat(savedOption.name).isEqualTo(optionRequest.name)
    }

    @Test
    @DisplayName("호텔 옵션 전체 조회")
    fun `should return all hotel options`() {
        // Given
        val initialSize = hotelOptionService.findAll().size

        listOf("추가 옵션1", "추가 옵션2").forEach {
            hotelOptionService.add(OptionRequest(it))
        }

        // When
        val result: List<OptionResponse> = hotelOptionService.findAll()

        // Then
        assertThat(result.size).isEqualTo(initialSize + 2)
    }

    @Test
    @DisplayName("호텔 옵션 수정")
    fun `should modify existing hotel option`() {
        // Given
        val modifiedRequest = OptionRequest("수정 후 테스트")

        // When
        val result = hotelOptionService.modify(testId, modifiedRequest)

        // Then
        assertThat(result.name).isEqualTo(modifiedRequest.name)

        // DB에 저장된 값 검증
        val savedOption: HotelOption = hotelOptionRepository.findById(result.optionId).get()
        assertThat(savedOption.name).isEqualTo(modifiedRequest.name)
    }

    @Test
    @DisplayName("호텔 옵션 삭제")
    fun `should delete existing hotel option`() {
        // When
        hotelOptionService.delete(testId)

        // Then
        assertThatThrownBy { hotelOptionService.findById(testId) }
            .isInstanceOf(ServiceException::class.java)
            .hasMessageContaining(ErrorCode.HOTEL_OPTION_NOT_FOUND.message)
    }
}