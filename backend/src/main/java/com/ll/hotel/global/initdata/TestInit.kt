package com.ll.hotel.global.initdata

import com.ll.hotel.domain.booking.booking.dto.BookingRequest;
import com.ll.hotel.domain.booking.booking.entity.Booking;
import com.ll.hotel.domain.booking.booking.repository.BookingRepository;
import com.ll.hotel.domain.booking.payment.dto.PaymentRequest;
import com.ll.hotel.domain.booking.payment.entity.Payment;
import com.ll.hotel.domain.booking.payment.repository.PaymentRepository;
import com.ll.hotel.domain.hotel.hotel.dto.PostHotelRequest;
import com.ll.hotel.domain.hotel.hotel.dto.PostHotelResponse;
import com.ll.hotel.domain.hotel.hotel.repository.HotelRepository;
import com.ll.hotel.domain.hotel.hotel.service.HotelService;
import com.ll.hotel.domain.hotel.option.entity.HotelOption;
import com.ll.hotel.domain.hotel.option.entity.RoomOption;
import com.ll.hotel.domain.hotel.option.repository.HotelOptionRepository;
import com.ll.hotel.domain.hotel.option.repository.RoomOptionRepository;
import com.ll.hotel.domain.hotel.room.dto.PostRoomRequest;
import com.ll.hotel.domain.hotel.room.dto.PostRoomResponse;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

import java.time.*;

@Configuration
@Profile("test")
class TestInit(
        private val hotelService: HotelService,
        private val roomService: RoomService,
        private val memberRepository: MemberRepository,
        private val businessRepository: BusinessRepository,
        private val hotelOptionRepository: HotelOptionRepository,
        private val roomOptionRepository: RoomOptionRepository,
        private val hotelRepository: HotelRepository,
        private val roomRepository: RoomRepository,
        private val paymentRepository: PaymentRepository,
        private val bookingRepository: BookingRepository,
        private val reviewRepository: ReviewRepository,
        private val imageService: ImageService
) {
    @Autowired
    @Lazy
    lateinit var self: TestInit

    @Bean
    fun testInitApplicationRunner(): ApplicationRunner {
        return ApplicationRunner { self.createData() }
    }

    @Transactional
    fun createData() {
        if (memberRepository.count() > 0) {
            return
        }

        // Create a single customer (memberId = 1)
        val customer = memberRepository.save(
                Member(
                        birthDate = LocalDate.now(),
                        memberEmail = "customer1@hotel.com",
                        memberName = "customer1",
                        memberPhoneNumber = "01012341234",
                        memberStatus = MemberStatus.ACTIVE,
                        role = Role.USER
                )
        )

        val customer2 = memberRepository.save(
                Member(
                        birthDate = LocalDate.now(),
                        memberEmail = "customer2@hotel.com",
                        memberName = "customer2",
                        memberPhoneNumber = "01012344321",
                        memberStatus = MemberStatus.ACTIVE,
                        role = Role.USER
                )
        )

        // Create a business member
        val businessMember = memberRepository.save(
                Member(
                        birthDate = LocalDate.now(),
                        memberEmail = "business1@hotel.com",
                        memberName = "business1",
                        memberPhoneNumber = "01043214321",
                        memberStatus = MemberStatus.ACTIVE,
                        role = Role.BUSINESS
                )
        )

        businessRepository.save(
                Business(
                        businessRegistrationNumber = "1000000001",
                        startDate = LocalDate.now(),
                        ownerName = "사장1",
                        approvalStatus = BusinessApprovalStatus.APPROVED,
                        member = businessMember,
                        hotel = null
                )
        )

        // Create hotel options
        val hotelOptions = mutableListOf<HotelOption>().apply {
            add(hotelOptionRepository.save(HotelOption(name = "무료 Wi-Fi")))
            add(hotelOptionRepository.save(HotelOption(name = "프론트 데스크")))
        }

        val roomOptions = mutableListOf<RoomOption>().apply {
            add(roomOptionRepository.save(RoomOption(name = "객실 내 금고")))
            add(roomOptionRepository.save(RoomOption(name = "미니 냉장고")))
        }

        // Create 1 hotel
        val hotelRequest = PostHotelRequest(
                hotelName = "강남호텔",
                hotelEmail = "gangnam@hotel.com",
                hotelPhoneNumber = "02-123-4567",
                streetAddress = "서울시 강남구 호텔로 10",
                zipCode = 15000,
                hotelGrade = 3,
                checkInTime = LocalTime.of(15, 0),
                checkOutTime = LocalTime.of(11, 0),
                hotelExplainContent = "강남 중심에 위치한 호텔",
                hotelOptions = setOf("무료 Wi-Fi", "프론트 데스크")
        )
        val hotelResponse: PostHotelResponse = hotelService.createHotel(businessMember, hotelRequest)
        val hotel = hotelRepository.findById(hotelResponse.hotelId).get()

        // Create 1 room for the hotel
        val roomRequest = PostRoomRequest(
                roomName = "스탠다드룸",
                roomNumber = 4,
                basePrice = 100000,
                standardNumber = 2,
                maxNumber = 4,
                bedTypeNumber = mapOf("DOUBLE" to 1),
                roomOptions = setOf("객실 내 금고", "미니 냉장고")
        )

        val roomResponse: PostRoomResponse = roomService.createRoom(hotel.id, businessMember, roomRequest)
        val room = roomRepository.findById(roomResponse.roomId).get()

        // Create 1 booking
        val checkIn = LocalDate.of(2025, 3, 10)
        val checkOut = LocalDate.of(2025, 3, 12)
        val price = room.basePrice
        val paidAtTimestamp = checkIn.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond()

        val bookingRequest = BookingRequest(
                roomId = room.id,
                hotelId = hotel.id,
                checkInDate = checkIn,
                checkOutDate = checkOut,
                merchantUid = "uid1001",
                amount = price,
                paidAtTimestamp = paidAtTimestamp
        )

        val bookingRequest2 = BookingRequest(
                roomId = room.id,
                hotelId = hotel.id,
                checkInDate = checkIn,
                checkOutDate = checkOut,
                merchantUid = "uid1002",
                amount = price,
                paidAtTimestamp = paidAtTimestamp
        )

        val paymentRequest = PaymentRequest.from(bookingRequest)
        val paymentRequest2 = PaymentRequest.from(bookingRequest2)

        val payment = paymentRepository.save(
                Payment(
                        merchantUid = paymentRequest.merchantUid,
                        amount = price,
                        paidAt = LocalDateTime.ofInstant(Instant.ofEpochSecond(paymentRequest.paidAtTimestamp), ZoneId.systemDefault())
                )
        )

        val payment2 = paymentRepository.save(
                Payment(
                        merchantUid = paymentRequest2.merchantUid,
                        amount = price,
                        paidAt = LocalDateTime.ofInstant(Instant.ofEpochSecond(paymentRequest2.paidAtTimestamp), ZoneId.systemDefault())
                )
        )

        val booking = Booking(
                room = room,
                hotel = hotel,
                member = customer,
                payment = payment,
                checkInDate = checkIn,
                checkOutDate = checkOut
        )

        val booking2 = Booking(
                room = room,
                hotel = hotel,
                member = customer,
                payment = payment2,
                checkInDate = checkIn,
                checkOutDate = checkOut
        )

        bookingRepository.save(booking)
        bookingRepository.save(booking2)

        val review = reviewRepository.save(
                Review(hotel, room, booking, customer, "리뷰 1 생성합니다.", 4)
        )

        val review2 = reviewRepository.save(
                Review(hotel, room, booking2, customer, "리뷰 2 생성합니다.", 5)
        )

        val imageUrls = listOf(
                "https://test-bucket.s3.amazonaws.com/reviews/2/1.jpg",
                "https://test-bucket.s3.amazonaws.com/reviews/2/2.jpg"
        )
        imageService.saveImages(ImageType.REVIEW, review.id, imageUrls)
    }
}