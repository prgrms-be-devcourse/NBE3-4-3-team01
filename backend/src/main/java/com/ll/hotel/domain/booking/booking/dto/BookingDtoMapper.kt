package com.ll.hotel.domain.booking.booking.dto

import com.ll.hotel.domain.booking.booking.entity.Booking
import com.ll.hotel.domain.booking.payment.dto.PaymentResponse
import com.ll.hotel.domain.hotel.hotel.dto.GetHotelDetailResponse
import com.ll.hotel.domain.hotel.hotel.service.HotelService
import com.ll.hotel.domain.hotel.room.dto.GetRoomDetailResponse
import com.ll.hotel.domain.hotel.room.service.RoomService
import com.ll.hotel.domain.member.member.dto.MemberDTO
import com.ll.hotel.domain.member.member.entity.Member
import org.springframework.stereotype.Component

@Component
class BookingDtoMapper(
    private val hotelService: HotelService,
    private val roomService: RoomService
) {
    private val NO_IMAGE = "https://previews.123rf.com/images/oculo/oculo2004/oculo200400003/143645399-사용-가능한-이미지가-없습니다-아이콘.jpg"

    private fun getHotelDetailResponse(hotelId: Long): GetHotelDetailResponse {
        return hotelService.findHotelDetail(hotelId)
    }

    private fun getRoomDetailResponse(hotelId: Long, roomId: Long): GetRoomDetailResponse {
        return roomService.findRoomDetail(hotelId, roomId)
    }

    private fun getThumbnailUrl(urls: List<String>): String {
        return if (urls.isEmpty()) NO_IMAGE else urls[0]
    }

    fun getForm(hotelId: Long, roomId: Long, member: Member): BookingFormResponse {
        val hotelDetailResponse = getHotelDetailResponse(hotelId)
        val roomDetailResponse = getRoomDetailResponse(hotelId, roomId)

        return BookingFormResponse(
            hotel = hotelDetailResponse.hotelDetailDto,
            room = roomDetailResponse.roomDto,
            thumbnailUrls = listOf(
                getThumbnailUrl(hotelDetailResponse.hotelImageUrls),
                getThumbnailUrl(roomDetailResponse.roomImageUrls)
            ),
            member = MemberDTO.from(member)
        )
    }

    fun getSummary(booking: Booking): BookingResponseSummary {
        val hotelId = booking.hotel.id
        val roomId = booking.room.id

        val hotelDetailResponse = getHotelDetailResponse(hotelId)
        val roomDetailResponse = getRoomDetailResponse(hotelId, roomId)

        return BookingResponseSummary(
            bookingId = booking.id,
            hotelId = booking.hotel.id,
            roomId = booking.room.id,
            hotelName = hotelDetailResponse.hotelDetailDto.hotelName,
            roomName = roomDetailResponse.roomDto.roomName,
            memberName = booking.member.memberName,
            thumbnailUrl = getThumbnailUrl(hotelDetailResponse.hotelImageUrls),
            bookingStatus = booking.bookingStatus,
            amount = booking.payment.amount,
            checkInDate = booking.checkInDate,
            checkOutDate = booking.checkOutDate
        )
    }

    fun getDetails(booking: Booking): BookingResponseDetails {
        val hotelId = booking.hotel.id
        val roomId = booking.room.id

        val hotelDetailResponse = getHotelDetailResponse(hotelId)
        val roomDetailResponse = getRoomDetailResponse(hotelId, roomId)

        return BookingResponseDetails(
            bookingId = booking.id,
            hotel = hotelDetailResponse.hotelDetailDto,
            room = roomDetailResponse.roomDto,
            thumbnailUrls = listOf(
                getThumbnailUrl(hotelDetailResponse.hotelImageUrls),
                getThumbnailUrl(roomDetailResponse.roomImageUrls)
            ),
            member = MemberDTO.from(booking.member),
            payment = PaymentResponse.from(booking.payment),
            bookNumber = booking.bookingNumber,
            bookingStatus = booking.bookingStatus,
            createdAt = booking.createdAt,
            modifiedAt = booking.modifiedAt,
            checkInDate = booking.checkInDate,
            checkOutDate = booking.checkOutDate
        )
    }
}