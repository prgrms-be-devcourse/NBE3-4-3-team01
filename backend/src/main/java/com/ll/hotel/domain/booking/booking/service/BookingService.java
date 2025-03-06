package com.ll.hotel.domain.booking.booking.service;

import com.ll.hotel.domain.booking.booking.dto.*;
import com.ll.hotel.domain.booking.booking.entity.Booking;
import com.ll.hotel.domain.booking.booking.repository.BookingRepository;
import com.ll.hotel.domain.booking.booking.type.BookingStatus;
import com.ll.hotel.domain.booking.payment.entity.Payment;
import com.ll.hotel.domain.booking.payment.service.PaymentService;
import com.ll.hotel.domain.booking.payment.type.PaymentStatus;
import com.ll.hotel.domain.hotel.hotel.entity.Hotel;
import com.ll.hotel.domain.hotel.hotel.repository.HotelRepository;
import com.ll.hotel.domain.hotel.room.entity.Room;
import com.ll.hotel.domain.hotel.room.repository.RoomRepository;
import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.global.exceptions.ErrorCode;
import com.ll.hotel.global.exceptions.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final PaymentService paymentService;
    private final BookingDtoMapper bookingDtoMapper;

    /*
     * 예약, 결제 정보 저장
     * preCreate : 예약 페이지를 생성하기 위한 api
     * 결제 -> 예약 순으로 진행
     */
    public BookingFormResponse preCreate(long hotelId, long roomId, Member member) {
        return bookingDtoMapper.getForm(hotelId, roomId, member);
    }

    @Transactional
    public void create(Member member, BookingRequest bookingRequest) {
        try {
            Room room = roomRepository.findById(bookingRequest.roomId())
                    .orElseThrow(ErrorCode.ROOM_NOT_FOUND::throwServiceException);
            Hotel hotel = hotelRepository.findById(bookingRequest.hotelId())
                    .orElseThrow(ErrorCode.HOTEL_NOT_FOUND::throwServiceException);
            Payment payment = paymentService.create(bookingRequest);

            // Booking 생성, 리팩터링 예정
            Booking booking = Booking.builder()
                    .room(room)
                    .hotel(hotel)
                    .member(member)
                    .payment(payment)
                    .checkInDate(bookingRequest.checkInDate())
                    .checkOutDate(bookingRequest.checkOutDate())
                    .build();

            booking = bookingRepository.save(booking);
            booking.setBookingNumber(String.format("B%08d", booking.getId())); // ID 기반으로 예약 번호 생성, 리팩터링 예정
            bookingRepository.save(booking);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw ErrorCode.BOOKING_CREATE_FAIL.throwServiceException(e);
        }
    }

    /*
     * 예약 조회는 아래의 세 조회만 가능
     * 사용자측, 사업자측 조회 (페이징)
     * 상세 조회
     * 이외에는 설계된 바가 없음
     */

    // 사용자측 예약 조회 (사업자, 관리자도 조회는 가능)
    public Page<BookingResponseSummary> tryGetMyBookings(Member member, int page, int pageSize) {
        return findByMember(member, page, pageSize).map((booking) -> bookingDtoMapper.getSummary(booking));
    }

    // 호텔(사업자)측 예약 조회
    public Page<BookingResponseSummary> tryGetHotelBookings(Member member, int page, int pageSize) {
        // 호텔 사업자만 조회 가능
        if (!member.isBusiness()) {
            throw ErrorCode.BUSINESS_ACCESS_FORBIDDEN.throwServiceException();
        }

        Hotel myHotel = member.getBusiness().getHotel();

        // 내 호텔이 없을 경우
        if (myHotel == null) {
            throw ErrorCode.HOTEL_NOT_FOUND.throwServiceException();
        }

        return findByHotel(myHotel, page, pageSize).map((booking) -> bookingDtoMapper.getSummary(booking));
    }

    // 예약 상세 조회
    public BookingResponseDetails tryGetBookingDetails(Member member, long bookingId) {
        Booking booking = findById(bookingId);

        // 관리자, 예약자, 호텔 사업자만 조회 가능
        if (!member.isAdmin() && !booking.isReservedBy(member) && !booking.isOwnedBy(member)) {
            throw ErrorCode.BOOKING_ACCESS_FORBIDDEN.throwServiceException();
        }

        return bookingDtoMapper.getDetails(booking);
    }

    /*
     * 예약, 결제 취소
     * 예약 -> 결제 취소 순으로 진행
     */
    public void tryCancel(Member member, long bookingId) {
        Booking booking = findById(bookingId);

        // 인가, 관리자/예약 당사자/호텔 주인일 경우 가능
        if (!member.isAdmin() && !booking.isReservedBy(member) && !booking.isOwnedBy(member)) {
            throw ErrorCode.BOOKING_CANCEL_FORBIDDEN.throwServiceException();
        }
        // 이미 취소된 예약일 경우
        if (booking.getBookingStatus() == BookingStatus.CANCELLED
                && booking.getPayment().getPaymentStatus() == PaymentStatus.CANCELLED) {
            throw ErrorCode.BOOKING_CANCEL_TO_CANCEL.throwServiceException();
        }

        // 완료된 예약일 경우
        if (booking.getBookingStatus() == BookingStatus.COMPLETED) {
            throw ErrorCode.BOOKING_COMPLETE_TO_CANCEL.throwServiceException();
        }

        cancel(booking);
    }

    @Transactional
    public void cancel(Booking booking) {
        try {
            // Rollback 가능한 작업부터
            booking.setBookingStatus(BookingStatus.CANCELLED);
            bookingRepository.saveAndFlush(booking);

            // 결제는 외부 api를 호출하므로 마지막에 작업
            Payment payment = booking.getPayment();
            paymentService.softDelete(payment);
            bookingRepository.save(booking);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw ErrorCode.BOOKING_CANCEL_FAIL.throwServiceException(e);
        }
    }

    // 예약 완료 처리
    public void trySetCompleted(Member member, long bookingId) {
        Booking booking = findById(bookingId);

        // 인가, 관리자/호텔 사업자만 완료 처리 가능
        if (!member.isAdmin() && !booking.isOwnedBy(member)) {
            throw ErrorCode.BOOKING_COMPLETE_FORBIDDEN.throwServiceException();
        }

        // 이미 완료 처리된 예약일 경우
        if (booking.getBookingStatus() == BookingStatus.COMPLETED) {
            throw ErrorCode.BOOKING_COMPLETE_TO_COMPLETE.throwServiceException();
        }

        // 취소된 예약일 경우
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw ErrorCode.BOOKING_CANCEL_TO_COMPLETE.throwServiceException();
        }

        setCompleted(booking);
    }

    @Transactional
    public void setCompleted(Booking booking) {
        try {
            booking.setBookingStatus(BookingStatus.COMPLETED);
            bookingRepository.save(booking);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw ErrorCode.BOOKING_COMPLETE_FAIL.throwServiceException(e);
        }
    }

    // 기본 조회 메서드
    public Booking findById(long id) {
        return bookingRepository.findById(id)
                .orElseThrow(ErrorCode.BOOKING_NOT_FOUND::throwServiceException);
    }

    public Page<Booking> findByMember(Member member, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.desc("id")));
        return bookingRepository.findByMember(member, pageable);
    }

    public Page<Booking> findByHotel(Hotel hotel, int page, int pageSize) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.desc("id")));
        return bookingRepository.findByHotel(hotel, pageRequest);
    }
}