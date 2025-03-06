package com.ll.hotel.domain.booking.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.hotel.domain.booking.booking.dto.BookingRequest;
import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.domain.member.member.repository.MemberRepository;
import com.ll.hotel.global.request.Rq;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class BookingControllerTest {

    @Autowired
    private BookingController bookingController;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @Mock
    private Rq mockRq;

    // 인가 설정 전 초기화
    @BeforeEach
    void initMock() {
        MockitoAnnotations.openMocks(this);
    }

    // 인가 설정
    void setMock(Member member) {
        when(mockRq.getActor()).thenReturn(member);
        ReflectionTestUtils.setField(bookingController, "rq", mockRq);
    }

    // 인가 설정 후 초기화
    @AfterEach
    void clearMock() {
        Mockito.reset(mockRq);
    }

    @Test
    @DisplayName("1-1 / 정상) 예약 페이지 요청")
    void t1_1() throws Exception {
        // 인가 설정
        Member customer = memberRepository.findByMemberName("customer1")
                .orElseThrow(() -> new IllegalArgumentException("테스트 데이터가 잘못되었습니다."));
        setMock(customer);

        // 호텔, 객실 정보 요청
        ResultActions resultActions = mvc
                .perform(get("/api/bookings")
                        .param("hotelId", "1")
                        .param("roomId", "1"))
                .andDo(print());

        // 검증
        resultActions
                .andExpect(handler().handlerType(BookingController.class))
                .andExpect(handler().methodName("preBook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hotel.hotelName").value("강남호텔"))
                .andExpect(jsonPath("$.data.room.roomName").value("스탠다드룸"))
                .andExpect(jsonPath("$.data.thumbnailUrls").exists())
                .andExpect(jsonPath("$.data.member.memberName").value("customer1"));
    }

    @Test
    @DisplayName("1-2 / 비정상) 예약 페이지 요청: 존재하지 않는 호텔 ID")
    void t1_2() throws Exception {
        // 인가 설정
        Member customer = memberRepository.findByMemberName("customer1")
                .orElseThrow(() -> new IllegalArgumentException("테스트 데이터가 잘못되었습니다."));
        setMock(customer);

        // 잘못된 호텔 정보 요청
        ResultActions resultActions = mvc
                .perform(get("/api/bookings")
                        .param("hotelId", "99") // 존재하지 않는 호텔 ID
                        .param("roomId", "1"))
                .andDo(print());

        // 검증
        resultActions
                .andExpect(handler().handlerType(BookingController.class))
                .andExpect(handler().methodName("preBook"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("2-1 / 정상) 예약 요청")
    void t2_1() throws Exception {
        // 인가 설정
        Member customer = memberRepository.findByMemberName("customer1")
                .orElseThrow(() -> new IllegalArgumentException("테스트 데이터가 잘못되었습니다."));
        setMock(customer);

        // 예약 시도
        BookingRequest bookingRequest = new BookingRequest(
                1L, 1L, LocalDate.now(), LocalDate.now(),
                "1234567890", 1004, Instant.now().getEpochSecond());
        ResultActions resultActions = mvc
                .perform(post("/api/bookings")
                        .content(objectMapper.writeValueAsString(bookingRequest))
                        .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)))
                .andDo(print());

        // 검증
        resultActions
                .andExpect(handler().handlerType(BookingController.class))
                .andExpect(handler().methodName("book"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("2-2 / 비정상) 예약 요청, 존재하지 않는 객실 ID")
    void t2_2() throws Exception {
        // 인가 설정
        Member customer = memberRepository.findByMemberName("customer1")
                .orElseThrow(() -> new IllegalArgumentException("테스트 데이터가 잘못되었습니다."));
        setMock(customer);

        // 잘못된 예약 시도
        BookingRequest bookingRequest = new BookingRequest(
                99L, 99L, LocalDate.now(), LocalDate.now(), // 존재하지 않는 객실 ID
                "1234567890", 1004, Instant.now().getEpochSecond());
        ResultActions resultActions = mvc
                .perform(post("/api/bookings")
                        .content(objectMapper.writeValueAsString(bookingRequest))
                        .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)))
                .andDo(print());

        // 검증
        resultActions
                .andExpect(handler().handlerType(BookingController.class))
                .andExpect(handler().methodName("book"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("3-1 / 정상) 내 예약 조회")
    void t3_1() throws Exception {
        // 인가 설정
        Member customer = memberRepository.findByMemberName("customer1")
                .orElseThrow(() -> new IllegalArgumentException("테스트 데이터가 잘못되었습니다."));
        setMock(customer);

        // 내 예약 조회
        ResultActions resultActions = mvc
                .perform(get("/api/bookings/me"))
                .andDo(print());

        // 검증
        resultActions
                .andExpect(handler().handlerType(BookingController.class))
                .andExpect(handler().methodName("getMyBookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].bookingId").value(2))
                .andExpect(jsonPath("$.data.items[1].bookingId").value(1));
    }

    @Test
    @DisplayName("4-1 / 정상) 호텔측 예약 조회")
    void t4_1() throws Exception {
        // 인가 설정
        Member customer = memberRepository.findByMemberName("business1")
                .orElseThrow(() -> new IllegalArgumentException("테스트 데이터가 잘못되었습니다."));
        setMock(customer);

        // 호텔측 예약 조회
        ResultActions resultActions = mvc
                .perform(get("/api/bookings/myHotel"))
                .andDo(print());

        // 검증
        resultActions
                .andExpect(handler().handlerType(BookingController.class))
                .andExpect(handler().methodName("getHotelBookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].bookingId").value(2))
                .andExpect(jsonPath("$.data.items[1].bookingId").value(1));
    }

    @Test
    @DisplayName("4-2 / 비정상) 호텔측 예약 조회, 비인가자")
    void t4_2() throws Exception {
        // 잘못된 인가 설정
        Member customer = memberRepository.findByMemberName("customer1") // 비사업자, 호텔 비소유자는 조회 불가
                .orElseThrow(() -> new IllegalArgumentException("테스트 데이터가 잘못되었습니다."));
        setMock(customer);

        // 호텔측 예약 조회
        ResultActions resultActions = mvc
                .perform(get("/api/bookings/myHotel"))
                .andDo(print());

        // 검증
        resultActions
                .andExpect(handler().handlerType(BookingController.class))
                .andExpect(handler().methodName("getHotelBookings"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("5-1 / 정상) 예약 상세 조회")
    void t5_1() throws Exception {
        // 인가 설정
        Member customer = memberRepository.findByMemberName("customer1")
                .orElseThrow(() -> new IllegalArgumentException("테스트 데이터가 잘못되었습니다."));
        setMock(customer);

        // 예약 상세 조회
        ResultActions resultActions = mvc
                .perform(get("/api/bookings/{booking_id}", 1))
                .andDo(print());

        // 검증
        resultActions
                .andExpect(handler().handlerType(BookingController.class))
                .andExpect(handler().methodName("getBookingDetails"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookingId").value(1));
    }

    @Test
    @DisplayName("5-2 / 비정상) 예약 상세 조회, 비인가자")
    void t5_2() throws Exception {
        // 잘못된 인가 설정
        Member customer = memberRepository.findByMemberName("customer2") // 예약 비관계자는 조회 불가
                .orElseThrow(() -> new IllegalArgumentException("테스트 데이터가 잘못되었습니다."));
        setMock(customer);

        // 예약 상세 조회
        ResultActions resultActions = mvc
                .perform(get("/api/bookings/{booking_id}", 1))
                .andDo(print());

        // 검증
        resultActions
                .andExpect(handler().handlerType(BookingController.class))
                .andExpect(handler().methodName("getBookingDetails"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("5-3 / 비정상) 예약 상세 조회, 존재하지 않는 예약 ID")
    void t5_3() throws Exception {
        // 인가 설정
        Member customer = memberRepository.findByMemberName("customer1")
                .orElseThrow(() -> new IllegalArgumentException("테스트 데이터가 잘못되었습니다."));
        setMock(customer);

        // 잘못된 예약 상세 조회
        ResultActions resultActions = mvc
                .perform(get("/api/bookings/{booking_id}", 99)) // 존재하지 않는 예약 ID
                .andDo(print());

        // 검증
        resultActions
                .andExpect(handler().handlerType(BookingController.class))
                .andExpect(handler().methodName("getBookingDetails"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("6-1 / 정상) 예약 취소")
    void t6_1() throws Exception {
        // 인가 설정
        Member customer = memberRepository.findByMemberName("customer1")
                .orElseThrow(() -> new IllegalArgumentException("테스트 데이터가 잘못되었습니다."));
        setMock(customer);

        // 예약 취소
        ResultActions resultActions = mvc
                .perform(delete("/api/bookings/{booking_id}", 1))
                .andDo(print());

        // 검증
        resultActions
                .andExpect(handler().handlerType(BookingController.class))
                .andExpect(handler().methodName("cancel"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("6-2 / 비정상) 예약 취소, 비인가자")
    void t6_2() throws Exception {
        // 잘못된 인가 설정
        Member customer = memberRepository.findByMemberName("customer2") // 예약 비관계자는 취소 불가
                .orElseThrow(() -> new IllegalArgumentException("테스트 데이터가 잘못되었습니다."));
        setMock(customer);

        // 예약 취소
        ResultActions resultActions = mvc
                .perform(delete("/api/bookings/{booking_id}", 1))
                .andDo(print());

        // 검증
        resultActions
                .andExpect(handler().handlerType(BookingController.class))
                .andExpect(handler().methodName("cancel"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("6-3 / 비정상) 예약 취소, 중복 시도")
    void t6_3() throws Exception {
        // 인가 설정
        Member customer = memberRepository.findByMemberName("customer1")
                .orElseThrow(() -> new IllegalArgumentException("테스트 데이터가 잘못되었습니다."));
        setMock(customer);

        // 예약 중복 취소
        mvc.perform(delete("/api/bookings/{booking_id}", 1)) // 첫번째 취소
                .andDo(print());
        ResultActions resultActions = mvc
                .perform(delete("/api/bookings/{booking_id}", 1)) // 두번째 취소
                .andDo(print());

        // 검증
        resultActions
                .andExpect(handler().handlerType(BookingController.class))
                .andExpect(handler().methodName("cancel"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("6-4 / 비정상) 예약 취소, 사전에 완료 처리")
    void t6_4() throws Exception {
        // 인가 설정
        Member customer = memberRepository.findByMemberName("business1")
                .orElseThrow(() -> new IllegalArgumentException("테스트 데이터가 잘못되었습니다."));
        setMock(customer);

        // 예약 완료 처리 후 취소
        mvc.perform(patch("/api/bookings/{booking_id}", 1)) // 완료 처리
                .andDo(print());
        ResultActions resultActions = mvc
                .perform(delete("/api/bookings/{booking_id}", 1)) // 예약 취소
                .andDo(print());

        // 검증
        resultActions
                .andExpect(handler().handlerType(BookingController.class))
                .andExpect(handler().methodName("cancel"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("6-5 / 비정상) 예약 취소, 존재하지 않는 예약 ID")
    void t6_5() throws Exception {
        // 인가 설정
        Member customer = memberRepository.findByMemberName("customer1")
                .orElseThrow(() -> new IllegalArgumentException("테스트 데이터가 잘못되었습니다."));
        setMock(customer);

        // 잘못된 예약 취소
        ResultActions resultActions = mvc
                .perform(delete("/api/bookings/{booking_id}", 99)) // 존재하지 않는 예약 ID
                .andDo(print());

        // 검증
        resultActions
                .andExpect(handler().handlerType(BookingController.class))
                .andExpect(handler().methodName("cancel"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("7-1 / 정상) 예약 완료 처리")
    void t7_1() throws Exception {
        // 인가 설정
        Member customer = memberRepository.findByMemberName("business1")
                .orElseThrow(() -> new IllegalArgumentException("테스트 데이터가 잘못되었습니다."));
        setMock(customer);

        // 예약 완료 처리
        ResultActions resultActions = mvc
                .perform(patch("/api/bookings/{booking_id}", 1))
                .andDo(print());

        // 검증
        resultActions
                .andExpect(handler().handlerType(BookingController.class))
                .andExpect(handler().methodName("complete"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("7-2 / 비정상) 예약 완료 처리, 비인가자")
    void t7_2() throws Exception {
        // 잘못된 인가 설정
        Member customer = memberRepository.findByMemberName("customer1") // 예약을 소유한 사업자만 완료 처리 가능
                .orElseThrow(() -> new IllegalArgumentException("테스트 데이터가 잘못되었습니다."));
        setMock(customer);

        // 예약 완료 처리
        ResultActions resultActions = mvc
                .perform(patch("/api/bookings/{booking_id}", 1))
                .andDo(print());

        // 검증
        resultActions
                .andExpect(handler().handlerType(BookingController.class))
                .andExpect(handler().methodName("complete"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("7-3 / 비정상) 예약 완료 처리, 중복 시도")
    void t7_3() throws Exception {
        // 인가 설정
        Member customer = memberRepository.findByMemberName("business1")
                .orElseThrow(() -> new IllegalArgumentException("테스트 데이터가 잘못되었습니다."));
        setMock(customer);

        // 예약 중복 완료 처리
        mvc.perform(patch("/api/bookings/{booking_id}", 1))
                .andDo(print());
        ResultActions resultActions = mvc
                .perform(patch("/api/bookings/{booking_id}", 1))
                .andDo(print());

        // 검증
        resultActions
                .andExpect(handler().handlerType(BookingController.class))
                .andExpect(handler().methodName("complete"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("7-4 / 비정상) 예약 완료 처리, 사전에 취소")
    void t7_4() throws Exception {
        // 인가 설정
        Member customer = memberRepository.findByMemberName("business1")
                .orElseThrow(() -> new IllegalArgumentException("테스트 데이터가 잘못되었습니다."));
        setMock(customer);

        // 예약 취소 후 완료 처리
        mvc.perform(delete("/api/bookings/{booking_id}", 1)) // 예약 취소
                .andDo(print());
        ResultActions resultActions = mvc
                .perform(patch("/api/bookings/{booking_id}", 1)) // 완료 처리
                .andDo(print());

        // 검증
        resultActions
                .andExpect(handler().handlerType(BookingController.class))
                .andExpect(handler().methodName("complete"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("7-5 / 비정상) 예약 완료 처리, 존재하지 않는 예약 ID")
    void t7_5() throws Exception {
        // 인가 설정
        Member customer = memberRepository.findByMemberName("business1")
                .orElseThrow(() -> new IllegalArgumentException("테스트 데이터가 잘못되었습니다."));
        setMock(customer);

        // 잘못된 예약 완료 처리
        ResultActions resultActions = mvc
                .perform(patch("/api/bookings/{booking_id}", 99)) // 존재하지 않는 예약 ID
                .andDo(print());

        // 검증
        resultActions
                .andExpect(handler().handlerType(BookingController.class))
                .andExpect(handler().methodName("complete"))
                .andExpect(status().isNotFound());
    }
}