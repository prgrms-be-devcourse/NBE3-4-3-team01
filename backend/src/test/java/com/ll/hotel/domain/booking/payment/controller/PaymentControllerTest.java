package com.ll.hotel.domain.booking.payment.controller;

import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.domain.member.member.repository.MemberRepository;
import com.ll.hotel.global.request.Rq;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
public class PaymentControllerTest {

    @Autowired
    private PaymentController paymentController;

    @Autowired
    private MemberRepository memberRepository;

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
        ReflectionTestUtils.setField(paymentController, "rq", mockRq);
    }

    // 인가 설정 후 초기화
    @AfterEach
    void clearMock() {
        Mockito.reset(mockRq);
    }

    @Test
    @DisplayName("결제 Uid 발급")
    void t1() throws Exception {
        // 인가 설정
        Member customer = memberRepository.findByMemberName("customer1")
                .orElseThrow(() -> new IllegalArgumentException("테스트 데이터가 잘못되었습니다."));
        setMock(customer);

        // UID 발급 요청
        ResultActions resultActions = mvc
                .perform(get("/api/bookings/payments/uid"))
                .andDo(print());

        // 검증
        resultActions
                .andExpect(handler().handlerType(PaymentController.class))
                .andExpect(handler().methodName("getUid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.merchantUid").exists())
                .andExpect(jsonPath("$.data.apiId").exists())
                .andExpect(jsonPath("$.data.channelKey").exists());
    }
}