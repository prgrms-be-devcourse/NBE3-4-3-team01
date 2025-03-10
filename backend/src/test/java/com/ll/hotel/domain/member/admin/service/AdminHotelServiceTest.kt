package com.ll.hotel.domain.member.admin.service

import com.ll.hotel.domain.hotel.hotel.entity.Hotel
import com.ll.hotel.domain.hotel.hotel.repository.HotelRepository
import com.ll.hotel.domain.hotel.hotel.type.HotelStatus
import com.ll.hotel.domain.member.admin.dto.request.AdminHotelRequest
import com.ll.hotel.domain.member.admin.dto.response.AdminHotelResponse
import com.ll.hotel.domain.member.member.entity.Business
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.member.member.entity.Role
import com.ll.hotel.domain.member.member.repository.BusinessRepository
import com.ll.hotel.domain.member.member.repository.MemberRepository
import com.ll.hotel.domain.member.member.type.MemberStatus
import com.ll.hotel.global.exceptions.ErrorCode
import com.ll.hotel.global.exceptions.ServiceException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminHotelServiceTest @Autowired constructor(
    private val adminHotelService: AdminHotelService,
    private val hotelRepository: HotelRepository,
    private val businessRepository: BusinessRepository,
    private val memberRepository: MemberRepository
) {
    var testId: Long = 0L

    @BeforeEach
    fun setUp() {
        val member = memberRepository.save(
            Member(
                birthDate = LocalDate.now(),
                memberEmail = "member@email.com",
                memberName = "member",
                memberPhoneNumber = "01012345678",
                memberStatus = MemberStatus.ACTIVE,
                role = Role.BUSINESS
            )
        )

        val business = businessRepository.save(
            Business(
                businessRegistrationNumber = "1234567890",
                startDate = LocalDate.now(),
                ownerName = "홍길동",
                member = member
            )
        )

        val hotel = hotelRepository.save(
            Hotel(
                hotelName = "호텔",
                hotelExplainContent = "호텔 설명",
                streetAddress = "호텔 주소",
                zipCode = 12345,
                hotelEmail = "hotel@email.com",
                hotelGrade = 5,
                hotelStatus = HotelStatus.PENDING,
                checkInTime = LocalTime.of(15, 0),
                checkOutTime = LocalTime.of(11, 0),
                hotelPhoneNumber = "01012345678",
                averageRating = 5.6,
                totalReviewCount = 3L,
                totalReviewRatingSum = 5L,
                business = business,
            )
        )
        testId = hotel.id
    }

    @Test
    @DisplayName("호텔 페이지 조회")
    fun `should return paged list of hotels`() {
        // Given
        val page = 0
        val pageSize = 10
        val totalItems = hotelRepository.count()
        val expectedPages = (totalItems + pageSize - 1) / pageSize

        // When
        val result = adminHotelService.findAllPaged(page)

        // Then
        assertThat(result).isNotNull()
        assertThat(result.totalItems).isEqualTo(hotelRepository.count())
        assertThat(result.totalPages).isEqualTo(expectedPages)
    }

    @Test
    @DisplayName("호텔 페이지 조회 - 존재하지 않는 페이지 조회 시 예외 발생")
    fun `should throw exception when invalid page requested`() {
        // Given
        val pageSize = 10
        val totalElements = hotelRepository.count()
        val invalidPage = (totalElements / pageSize).toInt() + 1

        // When
        val exception = assertThrows<ServiceException> {
            adminHotelService.findAllPaged(invalidPage)
        }

        // Then
        assertThat(exception.resultCode).isEqualTo(ErrorCode.PAGE_NOT_FOUND.httpStatus)
    }

    @Test
    @DisplayName("호텔 조회")
    fun `should find hotel by id`() {
        // Given
        val expectedResult = AdminHotelResponse.Detail.from(
            hotelRepository.findById(testId)
                .orElseThrow(ErrorCode.BUSINESS_NOT_FOUND::throwServiceException)
        )

        // When
        val result = adminHotelService.findById(testId)

        // Then
        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    @DisplayName("호텔 조회 - 존재하지 않는 호텔 조회 시 예외 발생")
    fun `should throw exception when hotel not found`() {
        // Given
        val invalidHotelId = hotelRepository.count() + 1

        // When
        val exception = assertThrows<ServiceException> {
            adminHotelService.findById(invalidHotelId)
        }

        // Then
        assertThat(exception.resultCode).isEqualTo(ErrorCode.HOTEL_NOT_FOUND.httpStatus)
    }

    @Test
    @DisplayName("승인 정보 수정")
    fun `should approve hotel and update status`() {
        // Given
        val adminHotelRequest = AdminHotelRequest(HotelStatus.AVAILABLE)

        // When
        val result = adminHotelService.approve(testId, adminHotelRequest)

        // Then: 메모리 상의 데이터 검증
        assertThat(result.status).isEqualTo(HotelStatus.AVAILABLE)

        // Then: DB의 데이터 검증
        val savedHotel = hotelRepository.findById(testId).orElseThrow(
            ErrorCode.HOTEL_NOT_FOUND::throwServiceException
        )
        assertThat(savedHotel.hotelStatus).isEqualTo(HotelStatus.AVAILABLE)
    }
}