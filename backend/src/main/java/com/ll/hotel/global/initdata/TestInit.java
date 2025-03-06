package com.ll.hotel.global.initdata;

import com.ll.hotel.domain.booking.booking.dto.BookingRequest;
import com.ll.hotel.domain.booking.booking.entity.Booking;
import com.ll.hotel.domain.booking.booking.repository.BookingRepository;
import com.ll.hotel.domain.booking.payment.dto.PaymentRequest;
import com.ll.hotel.domain.booking.payment.entity.Payment;
import com.ll.hotel.domain.booking.payment.repository.PaymentRepository;
import com.ll.hotel.domain.hotel.hotel.dto.PostHotelRequest;
import com.ll.hotel.domain.hotel.hotel.dto.PostHotelResponse;
import com.ll.hotel.domain.hotel.hotel.entity.Hotel;
import com.ll.hotel.domain.hotel.hotel.repository.HotelRepository;
import com.ll.hotel.domain.hotel.hotel.service.HotelService;
import com.ll.hotel.domain.hotel.option.entity.HotelOption;
import com.ll.hotel.domain.hotel.option.entity.RoomOption;
import com.ll.hotel.domain.hotel.option.repository.HotelOptionRepository;
import com.ll.hotel.domain.hotel.option.repository.RoomOptionRepository;
import com.ll.hotel.domain.hotel.room.dto.PostRoomRequest;
import com.ll.hotel.domain.hotel.room.dto.PostRoomResponse;
import com.ll.hotel.domain.hotel.room.entity.Room;
import com.ll.hotel.domain.hotel.room.repository.RoomRepository;
import com.ll.hotel.domain.hotel.room.service.RoomService;
import com.ll.hotel.domain.image.service.ImageService;
import com.ll.hotel.domain.image.type.ImageType;
import com.ll.hotel.domain.member.member.entity.Business;
import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.domain.member.member.entity.Role;
import com.ll.hotel.domain.member.member.repository.BusinessRepository;
import com.ll.hotel.domain.member.member.repository.MemberRepository;
import com.ll.hotel.domain.member.member.type.BusinessApprovalStatus;
import com.ll.hotel.domain.member.member.type.MemberStatus;
import com.ll.hotel.domain.review.review.entity.Review;
import com.ll.hotel.domain.review.review.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@Profile("test")
@RequiredArgsConstructor
public class TestInit {
    private final HotelService hotelService;
    private final RoomService roomService;
    private final MemberRepository memberRepository;
    private final BusinessRepository businessRepository;
    private final HotelOptionRepository hotelOptionRepository;
    private final RoomOptionRepository roomOptionRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final ImageService imageService;

    @Autowired
    @Lazy
    private TestInit self;

    @Bean
    public ApplicationRunner testInitApplicationRunner() {
        return args -> {
            self.createData();
        };
    }

    @Transactional
    public void createData() {
        if (memberRepository.count() > 0) {
            return;
        }

        // Create a single customer (memberId = 1)
        Member customer = memberRepository.save(Member.builder()
                .birthDate(LocalDate.now())
                .memberEmail("customer1@hotel.com")
                .memberName("customer1")
                .memberPhoneNumber("01012341234")
                .memberStatus(MemberStatus.ACTIVE)
                .role(Role.USER)
                .build());

        Member customer2 = memberRepository.save(Member.builder()
                .birthDate(LocalDate.now())
                .memberEmail("customer2@hotel.com")
                .memberName("customer2")
                .memberPhoneNumber("01012344321")
                .memberStatus(MemberStatus.ACTIVE)
                .role(Role.USER)
                .build());

        // Create a business member
        Member businessMember = memberRepository.save(Member.builder()
                .birthDate(LocalDate.now())
                .memberEmail("business1@hotel.com")
                .memberName("business1")
                .memberPhoneNumber("01043214321")
                .memberStatus(MemberStatus.ACTIVE)
                .role(Role.BUSINESS)
                .build());

        businessRepository.save(Business.builder()
                .businessRegistrationNumber("1000000001")
                .startDate(LocalDate.now())
                .ownerName("사장1")
                .approvalStatus(BusinessApprovalStatus.APPROVED)
                .member(businessMember)
                .hotel(null)
                .build());

        // Create hotel options
        List<HotelOption> hotelOptions = new ArrayList<>();
        hotelOptions.add(hotelOptionRepository.save(HotelOption.builder().name("무료 Wi-Fi").build()));
        hotelOptions.add(hotelOptionRepository.save(HotelOption.builder().name("프론트 데스크").build()));

        List<RoomOption> roomOptions = new ArrayList<>();
        roomOptions.add(roomOptionRepository.save(RoomOption.builder().name("객실 내 금고").build()));
        roomOptions.add(roomOptionRepository.save(RoomOption.builder().name("미니 냉장고").build()));


        // Create 1 hotel
        PostHotelRequest hotelRequest = new PostHotelRequest(
                "강남호텔",
                "gangnam@hotel.com",
                "02-123-4567",
                "서울시 강남구 호텔로 10",
                15000,
                3,
                LocalTime.of(15, 0),
                LocalTime.of(11, 0),
                "강남 중심에 위치한 호텔",
                null,
                Set.of("무료 Wi-Fi", "프론트 데스크")
        );
        PostHotelResponse hotelResponse = hotelService.createHotel(businessMember, hotelRequest);
        Hotel hotel = hotelRepository.findById(hotelResponse.hotelId()).get();

        // Create 1 room for the hotel
        PostRoomRequest roomRequest = new PostRoomRequest(
                "스탠다드룸",
                4,
                100000,
                2,
                4,
                Map.of("DOUBLE", 1),
                null,
                Set.of("객실 내 금고", "미니 냉장고")
        );

        PostRoomResponse roomResponse = roomService.createRoom(hotel.getId(), businessMember, roomRequest);
        Room room = roomRepository.findById(roomResponse.roomId()).get();
        // Create 1 booking
        LocalDate checkIn = LocalDate.of(2025, 3, 10);
        LocalDate checkOut = LocalDate.of(2025, 3, 12);
        int price = room.getBasePrice();
        long paidAtTimestamp = checkIn.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

        BookingRequest bookingRequest = new BookingRequest(
                room.getId(),
                hotel.getId(),
                checkIn,
                checkOut,
                "uid1001",
                price,
                paidAtTimestamp
        );

        BookingRequest bookingRequest2 = new BookingRequest(
                room.getId(),
                hotel.getId(),
                checkIn,
                checkOut,
                "uid1002",
                price,
                paidAtTimestamp
        );
        PaymentRequest paymentRequest = PaymentRequest.from(bookingRequest);
        PaymentRequest paymentRequest2 = PaymentRequest.from(bookingRequest2);

        Payment payment = paymentRepository.save(Payment.builder()
                .merchantUid(paymentRequest.merchantUid())
                .amount(price)
                .paidAt(LocalDateTime.ofInstant(Instant.ofEpochSecond(paymentRequest.paidAtTimestamp()), ZoneId.systemDefault()))
                .build());
        Payment payment2 = paymentRepository.save(Payment.builder()
                .merchantUid(paymentRequest2.merchantUid())
                .amount(price)
                .paidAt(LocalDateTime.ofInstant(Instant.ofEpochSecond(paymentRequest2.paidAtTimestamp()), ZoneId.systemDefault()))
                .build());


        Booking booking = Booking.builder()
                .room(room)
                .hotel(hotel)
                .member(customer) // Using memberId = 1
                .payment(payment)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .build();

        // 2번째 예약
        Booking booking2 = Booking.builder()
                .room(room)
                .hotel(hotel)
                .member(customer) // Using memberId = 1
                .payment(payment2)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .build();

        bookingRepository.save(booking);
        bookingRepository.save(booking2);

        Review review = reviewRepository.save(
           Review.builder()
                   .booking(booking)
                   .hotel(hotel)
                   .room(room)
                   .member(customer)
                   .rating(4)
                   .content("리뷰 1 생성합니다.")
                   .build()
        );

        Review review2 = reviewRepository.save(
                Review.builder()
                        .booking(booking2)
                        .hotel(hotel)
                        .room(room)
                        .member(customer)
                        .rating(5)
                        .content("리뷰 2 생성합니다.")
                        .build()
        );

        List<String> imageUrls = List.of(
                "https://test-bucket.s3.amazonaws.com/reviews/2/1.jpg",
                "https://test-bucket.s3.amazonaws.com/reviews/2/2.jpg");
        imageService.saveImages(ImageType.REVIEW, review.getId(), imageUrls);
    }

}