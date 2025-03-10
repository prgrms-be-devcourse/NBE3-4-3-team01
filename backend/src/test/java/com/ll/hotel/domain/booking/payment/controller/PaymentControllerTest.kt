package com.ll.hotel.domain.booking.payment.controller

import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.member.member.repository.MemberRepository
import com.ll.hotel.global.request.Rq
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import org.mockito.Mockito.`when`
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
class PaymentControllerTest {

    @Autowired
    private lateinit var paymentController: PaymentController

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var mvc: MockMvc

    @Mock
    private lateinit var mockRq: Rq

    // 인가 설정 전 초기화
    @BeforeEach
    fun initMock() {
        MockitoAnnotations.openMocks(this)
    }

    // 인가 설정
    fun setMock(member: Member) {
        `when`(mockRq.getActor()).thenReturn(member)
        ReflectionTestUtils.setField(paymentController, "rq", mockRq)
    }

    // 인가 설정 후 초기화
    @AfterEach
    fun clearMock() {
        Mockito.reset(mockRq)
    }

    @Test
    @DisplayName("결제 Uid 발급")
    fun t1() {
        // 인가 설정
        val customer = memberRepository.findByMemberName("customer1")
            .orElseThrow { IllegalArgumentException("테스트 데이터가 잘못되었습니다.") }
        setMock(customer)

        // UID 발급 요청
        val resultActions = mvc
            .perform(get("/api/bookings/payments/uid"))
            .andDo(print())

        // 검증
        resultActions
            .andExpect(handler().handlerType(PaymentController::class.java))
            .andExpect(handler().methodName("getUid"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.merchantUid").exists())
            .andExpect(jsonPath("$.data.apiId").exists())
            .andExpect(jsonPath("$.data.channelKey").exists())
    }
}