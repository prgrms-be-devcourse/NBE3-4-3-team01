package com.ll.hotel.domain.hotel.option.service

import com.ll.hotel.domain.hotel.option.dto.request.OptionRequest
import com.ll.hotel.domain.hotel.option.dto.response.OptionResponse
import com.ll.hotel.domain.hotel.option.entity.HotelOption
import com.ll.hotel.domain.hotel.option.entity.RoomOption
import com.ll.hotel.domain.hotel.option.repository.RoomOptionRepository
import com.ll.hotel.global.exceptions.ErrorCode
import com.ll.hotel.global.exceptions.ServiceException
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class RoomOptionServiceTest (
    private val roomOptionService: RoomOptionService,
    private val roomOptionRepository: RoomOptionRepository
    ) {
        private var testId: Long = 0L

        @BeforeEach
        fun setUp() {
            testId = roomOptionRepository.save(RoomOption("객실 옵션")).id
        }

        @Test
        @DisplayName("객실 옵션 추가")
        fun `should add a new room option`() {
            // Given
            val optionRequest = OptionRequest("추가 테스트")

            // When
            val result: OptionResponse = roomOptionService.add(optionRequest)

            // Then
            assertThat(result).extracting { it.name }.isEqualTo(optionRequest.name)

            // DB에 저장된 값 검증
            val savedOption: RoomOption = roomOptionRepository.findById(result.optionId).get()
            assertThat(savedOption.name).isEqualTo(optionRequest.name)
        }

        @Test
        @DisplayName("객실 옵션 전체 조회")
        fun `should return all room options`() {
            // Given
            val initialSize = roomOptionService.findAll().size

            listOf("추가 옵션1", "추가 옵션2").forEach {
                roomOptionService.add(OptionRequest(it))
            }

            // When
            val result: List<OptionResponse> = roomOptionService.findAll()

            // Then
            assertThat(result.size).isEqualTo(initialSize + 2)
        }

        @Test
        @DisplayName("객실 옵션 수정")
        fun `should modify existing room option`() {
            // Given
            val modifiedRequest = OptionRequest("수정 후 테스트")

            // When
            val result = roomOptionService.modify(testId, modifiedRequest)

            // Then
            assertThat(result.name).isEqualTo(modifiedRequest.name)

            // DB에 저장된 값 검증
            val savedOption: RoomOption = roomOptionRepository.findById(result.optionId).get()
            assertThat(savedOption.name).isEqualTo(modifiedRequest.name)
        }

        @Test
        @DisplayName("객실 옵션 삭제")
        fun `should delete existing room option`() {
            // When
            roomOptionService.delete(testId)

            // Then
            assertThatThrownBy { roomOptionService.findById(testId) }
                .isInstanceOf(ServiceException::class.java)
                .hasMessageContaining(ErrorCode.ROOM_OPTION_NOT_FOUND.message)
        }
}