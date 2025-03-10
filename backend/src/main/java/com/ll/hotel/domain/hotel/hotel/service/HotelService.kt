package com.ll.hotel.domain.hotel.hotel.service

import com.ll.hotel.domain.booking.booking.entity.Booking
import com.ll.hotel.domain.booking.booking.type.BookingStatus
import com.ll.hotel.domain.hotel.hotel.dto.*
import com.ll.hotel.domain.hotel.hotel.entity.Hotel
import com.ll.hotel.domain.hotel.hotel.entity.Hotel.Companion.hotelBuild
import com.ll.hotel.domain.hotel.hotel.repository.HotelRepository
import com.ll.hotel.domain.hotel.hotel.type.HotelStatus
import com.ll.hotel.domain.hotel.option.repository.HotelOptionRepository
import com.ll.hotel.domain.hotel.room.dto.GetRoomRevenueResponse
import com.ll.hotel.domain.hotel.room.entity.Room
import com.ll.hotel.domain.hotel.room.repository.RoomRepository
import com.ll.hotel.domain.hotel.room.type.RoomStatus
import com.ll.hotel.domain.image.service.ImageService
import com.ll.hotel.domain.image.type.ImageType
import com.ll.hotel.domain.member.member.entity.Business
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.member.member.repository.BusinessRepository
import com.ll.hotel.domain.review.review.dto.response.PresignedUrlsResponse
import com.ll.hotel.global.annotation.BusinessOnly
import com.ll.hotel.global.aws.s3.S3Service
import com.ll.hotel.global.exceptions.ErrorCode
import com.ll.hotel.standard.util.CookieUtil
import com.ll.hotel.standard.util.Ut
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.collections.List
import kotlin.collections.Set
import kotlin.collections.set
import kotlin.math.min

@Service
@Transactional
class HotelService(
    private val imageService: ImageService,
    private val s3Service: S3Service,
    private val hotelRepository: HotelRepository,
    private val hotelOptionRepository: HotelOptionRepository,
    private val roomRepository: RoomRepository,
    private val businessRepository: BusinessRepository
) {

    @BusinessOnly
    @Transactional
    fun createHotel(actor: Member, postHotelRequest: PostHotelRequest): PostHotelResponse {
        val business: Business =
            this.businessRepository.findByMember(actor) ?: ErrorCode.BUSINESS_NOT_FOUND.throwServiceException()

        if (business.hotel != null) {
            ErrorCode.BUSINESS_HOTEL_LIMIT_EXCEEDED.throwServiceException()
        }

        val hotelOptions = this.hotelOptionRepository.findByNameIn(postHotelRequest.hotelOptions)

        if (hotelOptions.size != postHotelRequest.hotelOptions.size) {
            ErrorCode.HOTEL_OPTION_NOT_FOUND.throwServiceException()
        }

        val hotel = hotelBuild(postHotelRequest, business, hotelOptions.toMutableSet())

        return try {
            PostHotelResponse(
                this.hotelRepository.save(hotel), this.saveHotelImages(hotel.id, postHotelRequest.imageExtensions)
            )
        } catch (_: DataIntegrityViolationException) {
            ErrorCode.HOTEL_EMAIL_ALREADY_EXISTS.throwServiceException()
        }
    }

    @BusinessOnly
    @Transactional
    fun saveImages(actor: Member, imageType: ImageType, hotelId: Long, urls: List<String>) {
        val hotel = getHotelById(hotelId)

        if (!hotel.isOwnedBy(actor)) {
            ErrorCode.INVALID_BUSINESS.throwServiceException()
        }

        this.imageService.saveImages(imageType, hotelId, urls)
    }

    @Transactional(readOnly = true)
    fun findAllHotels(
        page: Int, pageSize: Int, filterName: String, filterDirection: String?,
        streetAddress: String, checkInDate: LocalDate, checkOutDate: LocalDate,
        personal: Int
    ): Page<GetHotelResponse> {
        val sortFieldMapping = mapOf(
            "latest" to "createdAt",
            "averageRating" to "averageRating",
            "reviewCount" to "totalReviewCount"
        )

        val sortField = sortFieldMapping[filterName] ?: "createdAt"

        val direction: Sort.Direction?
        if (filterDirection == null) {
            direction = Sort.Direction.DESC
        } else {
            try {
                direction = Sort.Direction.valueOf(filterDirection.uppercase(Locale.getDefault()))
            } catch (e: IllegalArgumentException) {
                ErrorCode.INVALID_FILTER_DIRECTION.throwServiceException()
            }
        }

        val sort = Sort.by(direction, sortField)
        val pageRequest = PageRequest.of(page - 1, pageSize, sort)

        val hotels = this.hotelRepository.findAllHotels(
            ImageType.HOTEL, streetAddress, PageRequest.of(0, Int.MAX_VALUE, sort)
        ).content

        val availableHotels = this.getAvailableHotels(checkInDate, checkOutDate, personal, hotels)

        val start = pageRequest.offset.toInt()
        val end = min((start + pageRequest.pageSize).toDouble(), availableHotels.size.toDouble()).toInt()

        val paginatedHotels = availableHotels.subList(start, end)

        return PageImpl(paginatedHotels, pageRequest, availableHotels.size.toLong())
    }

    @Transactional(readOnly = true)
    fun findHotelDetail(hotelId: Long): GetHotelDetailResponse {
        val hotel = this.hotelRepository.findHotelDetail(hotelId)
            .orElseThrow { ErrorCode.HOTEL_NOT_FOUND.throwServiceException() }

        val imageUrls = this.imageService.findImagesById(ImageType.HOTEL, hotelId)
            .map { it.imageUrl }

        val roomDtos = this.roomRepository.findAllRooms(hotelId, ImageType.ROOM)

        return GetHotelDetailResponse(HotelDetailDto(hotel, roomDtos), imageUrls)
    }

    @Transactional(readOnly = true)
    fun findHotelDetailWithAvailableRooms(
        hotelId: Long, checkInDate: LocalDate,
        checkoutDate: LocalDate, personal: Int
    ): GetHotelDetailResponse {
        val hotel = this.hotelRepository.findHotelDetail(hotelId)
            .orElseThrow { ErrorCode.HOTEL_NOT_FOUND.throwServiceException() }

        val imageUrls = this.imageService.findImagesById(ImageType.HOTEL, hotelId)
            .map { it.imageUrl }

        val roomDtos = this.roomRepository.findAllAvailableRooms(hotelId, ImageType.ROOM, personal)
            .map { dto ->
                val room = dto.room
                room.roomNumber = this.countAvailableRoomNumber(room, checkInDate, checkoutDate, personal)
                dto
            }

        return GetHotelDetailResponse(HotelDetailDto(hotel, roomDtos), imageUrls)
    }

    @BusinessOnly
    @Transactional
    fun modifyHotel(hotelId: Long, actor: Member, request: PutHotelRequest): PutHotelResponse {
        val hotel = this.getHotelById(hotelId)

        if (!hotel.isOwnedBy(actor)) {
            ErrorCode.INVALID_BUSINESS.throwServiceException()
        }

        if (hotelRepository.existsByHotelEmailAndIdNot(request.hotelEmail, hotelId)) {
            ErrorCode.HOTEL_EMAIL_ALREADY_EXISTS.throwServiceException()
        }

        this.modifyIfPresent(request.hotelName, hotel::hotelName, hotel::hotelName::set)
        this.modifyIfPresent(request.hotelEmail, hotel::hotelEmail, hotel::hotelEmail::set)
        this.modifyIfPresent(request.hotelPhoneNumber, hotel::hotelPhoneNumber, hotel::hotelPhoneNumber::set)
        this.modifyIfPresent(request.streetAddress, hotel::streetAddress, hotel::streetAddress::set)
        this.modifyIfPresent(request.zipCode, hotel::zipCode, hotel::zipCode::set)
        this.modifyIfPresent(request.hotelGrade, hotel::hotelGrade, hotel::hotelGrade::set)
        this.modifyIfPresent(request.checkInTime, hotel::checkInTime, hotel::checkInTime::set)
        this.modifyIfPresent(request.checkOutTime, hotel::checkOutTime, hotel::checkOutTime::set)
        this.modifyIfPresent(request.hotelExplainContent, hotel::hotelExplainContent, hotel::hotelExplainContent::set)

        try {
            hotel.hotelStatus = HotelStatus.valueOf(request.hotelStatus.uppercase())
        } catch (e: Exception) {
            ErrorCode.HOTEL_STATUS_NOT_FOUND.throwServiceException()
        }

        this.modifyOptions(hotel, request.hotelOptions)

        val deleteImageUrls = request.deleteImageUrls

        this.imageService.deleteImagesByIdAndUrls(ImageType.HOTEL, hotelId, deleteImageUrls)
        this.s3Service.deleteObjectsByUrls(deleteImageUrls)

        return PutHotelResponse(hotel, this.saveHotelImages(hotelId, request.imageExtensions))
    }

    private fun <T> modifyIfPresent(newValue: T, getter: Supplier<T>, setter: Consumer<T>) {
        if (newValue != getter.get()) {
            setter.accept(newValue)
        }
    }

    private fun modifyOptions(hotel: Hotel, optionNames: Set<String>) {
        if (optionNames.isEmpty()) {
            hotel.hotelOptions = mutableSetOf()
            return
        }

        val options = hotelOptionRepository.findByNameIn(optionNames)

        if (options.size != optionNames.size) {
            ErrorCode.HOTEL_OPTION_NOT_FOUND.throwServiceException()
        }

        hotel.hotelOptions = options.toMutableSet()
    }

    @BusinessOnly
    @Transactional
    fun deleteHotel(hotelId: Long, actor: Member) {
        val hotel = this.getHotelById(hotelId)

        if (!hotel.isOwnedBy(actor)) {
            ErrorCode.INVALID_BUSINESS.throwServiceException()
        }

        hotel.hotelStatus = HotelStatus.UNAVAILABLE

        if (this.imageService.deleteImages(ImageType.HOTEL, hotelId) > 0) {
            this.s3Service.deleteAllObjectsById(ImageType.HOTEL, hotelId)
        }
    }

    @BusinessOnly
    @Transactional
    fun findRevenue(hotelId: Long, actor: Member): GetHotelRevenueResponse {
        val hotel = this.getHotelById(hotelId)

        if (!hotel.isOwnedBy(actor)) {
            ErrorCode.INVALID_BUSINESS.throwServiceException()
        }

        var hotelRevenue: Long = 0
        val roomRevenueResponses = mutableListOf<GetRoomRevenueResponse>()

        for (room in hotel.rooms) {
            val roomRevenue: Long = room.bookings
                .filter { booking -> booking.bookingStatus == BookingStatus.COMPLETED }
                .map { it.payment }
                .sumOf { payment -> payment.amount.toLong() }

            hotelRevenue += roomRevenue

            roomRevenueResponses.add(
                GetRoomRevenueResponse(room.id, room.roomName, room.basePrice, roomRevenue)
            )
        }

        return GetHotelRevenueResponse(roomRevenueResponses, hotelRevenue)
    }

    // 호텔 이미지 저장
    private fun saveHotelImages(hotelId: Long, extensions: List<String>): PresignedUrlsResponse {
        val urls = this.s3Service.generatePresignedUrls(ImageType.HOTEL, hotelId, extensions)

        return PresignedUrlsResponse(hotelId, urls)
    }

    fun getHotelById(hotelId: Long): Hotel {
        return this.hotelRepository.findById(hotelId)
            .orElseThrow { ErrorCode.HOTEL_NOT_FOUND.throwServiceException() }
    }

    // 예약 가능한 호텔 리스트 예약 가능한 객실이 없으면 해당 호텔은 보여주지 않음.
    // room 이 변경되므로, 다른 곳에서는 사용하지 말 것
    // 첫 번째 filter 는 중복으로 인해 제거 상태. 문제 시 아래의 메서드와 함께 주석 해제
    // 배포 시 마지막 filter 부분 주석 해제 (예약 가능한 Room 이 없으면 호텔을 보여주지 않는 부분)
    private fun getAvailableHotels(
        checkInDate: LocalDate, checkOutDate: LocalDate,
        personal: Int, hotels: List<HotelWithImageDto>
    ): List<GetHotelResponse> {
        return hotels.mapNotNull { dto ->
            val hotel = dto.hotel
            val availableRooms = hotel.rooms
                .filter { room ->
                    room.roomStatus == RoomStatus.AVAILABLE
                            && personal in room.standardNumber..room.maxNumber
                }.map { room ->
                    room.roomNumber = this.countAvailableRoomNumber(room, checkInDate, checkOutDate, personal)
                    room
                }.filter { room -> room.roomNumber > 0 }

            if (availableRooms.isEmpty()) null else {
                val minPriceRoom = availableRooms.minByOrNull { it.basePrice }
                GetHotelResponse(dto, minPriceRoom!!.basePrice)
            }
        }
    }

    // 호텔의 예약 가능한 객실 수 Count
    private fun countAvailableRoomNumber(
        room: Room,
        checkInDate: LocalDate,
        checkOutDate: LocalDate,
        personal: Int
    ): Int {
        if (personal < room.standardNumber || personal > room.maxNumber) {
            return 0
        }

        val resolvedCount = room.bookings.count { booking: Booking ->
            (booking.checkInDate.isBefore(checkOutDate) && booking.checkOutDate.isAfter(checkInDate))
                    && booking.bookingStatus != BookingStatus.CANCELLED
        }

        return room.roomNumber - resolvedCount
    }

    @BusinessOnly
    @Transactional(readOnly = true)
    fun findHotelOptions(actor: Member): GetAllHotelOptionsResponse {
        return GetAllHotelOptionsResponse(hotelOptionRepository.findAll())
    }

    fun updateRoleCookie(request: HttpServletRequest, response: HttpServletResponse, hotelId: Long) {
        val roleCookie = CookieUtil.getCookie(request, "role")
        roleCookie?.let { cookie ->
            try {
                // URL 디코딩 후 JSON 파싱
                val decodedValue = URLDecoder.decode(cookie.value, StandardCharsets.UTF_8)
                val roleData = Ut.Json.toMap(decodedValue).toMutableMap()

                // 데이터 업데이트
                roleData["hasHotel"] = true
                roleData["hotelId"] = hotelId // 새로운 호텔 ID

                // 다시 JSON으로 변환하고 URL 인코딩
                val updatedEncodedData = URLEncoder.encode(Ut.Json.toString(roleData), StandardCharsets.UTF_8)

                // 새 쿠키 생성 및 설정
                val updatedCookie = Cookie("role", updatedEncodedData)
                updatedCookie.secure = true
                updatedCookie.path = "/"

                // 응답에 쿠키 추가
                response.addCookie(updatedCookie)
            } catch (e: Exception) {
                // 에러 처리
                e.printStackTrace()
            }
        }
    }
}
