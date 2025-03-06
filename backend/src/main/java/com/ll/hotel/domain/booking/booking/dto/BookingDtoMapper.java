package com.ll.hotel.domain.booking.booking.dto;

import com.ll.hotel.domain.booking.booking.entity.Booking;
import com.ll.hotel.domain.booking.payment.dto.PaymentResponse;
import com.ll.hotel.domain.hotel.hotel.dto.GetHotelDetailResponse;
import com.ll.hotel.domain.hotel.hotel.service.HotelService;
import com.ll.hotel.domain.hotel.room.dto.GetRoomDetailResponse;
import com.ll.hotel.domain.hotel.room.service.RoomService;
import com.ll.hotel.domain.member.member.dto.MemberDTO;
import com.ll.hotel.domain.member.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingDtoMapper {
    private final String NO_IMAGE = "https://previews.123rf.com/images/oculo/oculo2004/oculo200400003/143645399-사용-가능한-이미지가-없습니다-아이콘.jpg";
    private final HotelService hotelService;
    private final RoomService roomService;

    private GetHotelDetailResponse getHotelDetailResponse(long hotelId) {
        return hotelService.findHotelDetail(hotelId);
    }

    private GetRoomDetailResponse getRoomDetailResponse(long hotelId, long roomId) {
        return roomService.findRoomDetail(hotelId, roomId);
    }

    private String getThumbnailUrl(List<String> urls) {
        return urls.isEmpty() ? NO_IMAGE : urls.get(0);
    }

    public BookingFormResponse getForm(long hotelId, long roomId, Member member) {
        GetHotelDetailResponse hotelDetailResponse = getHotelDetailResponse(hotelId);
        GetRoomDetailResponse roomDetailResponse = getRoomDetailResponse(hotelId, roomId);

        return new BookingFormResponse(
                hotelDetailResponse.hotelDetailDto(),
                roomDetailResponse.roomDto(),
                new String[] {
                        getThumbnailUrl(hotelDetailResponse.hotelImageUrls()),
                        getThumbnailUrl(roomDetailResponse.roomImageUrls())
                },
                MemberDTO.from(member)
        );
    }

    public BookingResponseSummary getSummary(Booking booking) {
        long hotelId = booking.getHotel().getId();
        long roomId = booking.getRoom().getId();

        GetHotelDetailResponse hotelDetailResponse = getHotelDetailResponse(hotelId);
        GetRoomDetailResponse roomDetailResponse = getRoomDetailResponse(hotelId, roomId);

        return new BookingResponseSummary(
                booking.getId(),
                booking.getHotel().getId(),
                booking.getRoom().getId(),
                hotelDetailResponse.hotelDetailDto().hotelName(),
                roomDetailResponse.roomDto().roomName(),
                booking.getMember().getMemberName(),
                getThumbnailUrl(hotelDetailResponse.hotelImageUrls()),
                booking.getBookingStatus(),
                booking.getPayment().getAmount(),
                booking.getCheckInDate(),
                booking.getCheckOutDate()
        );
    }

    public BookingResponseDetails getDetails(Booking booking) {
        long hotelId = booking.getHotel().getId();
        long roomId = booking.getRoom().getId();

        GetHotelDetailResponse hotelDetailResponse = getHotelDetailResponse(hotelId);
        GetRoomDetailResponse roomDetailResponse = getRoomDetailResponse(hotelId, roomId);

        return new BookingResponseDetails(
                booking.getId(),
                hotelDetailResponse.hotelDetailDto(),
                roomDetailResponse.roomDto(),
                new String[] {
                        getThumbnailUrl(hotelDetailResponse.hotelImageUrls()),
                        getThumbnailUrl(roomDetailResponse.roomImageUrls())
                },
                MemberDTO.from(booking.getMember()),
                PaymentResponse.from(booking.getPayment()),
                booking.getBookingNumber(),
                booking.getBookingStatus(),
                booking.getCreatedAt(),
                booking.getModifiedAt(),
                booking.getCheckInDate(),
                booking.getCheckOutDate()
        );
    }
}
