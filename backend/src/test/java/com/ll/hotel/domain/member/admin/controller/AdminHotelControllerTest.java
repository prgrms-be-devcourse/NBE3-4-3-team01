package com.ll.hotel.domain.member.admin.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ll.hotel.domain.hotel.hotel.entity.Hotel;
import com.ll.hotel.domain.hotel.hotel.repository.HotelRepository;
import com.ll.hotel.domain.hotel.hotel.type.HotelStatus;
import com.ll.hotel.domain.member.member.entity.Business;
import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.domain.member.member.entity.Role;
import com.ll.hotel.domain.member.member.repository.BusinessRepository;
import com.ll.hotel.domain.member.member.repository.MemberRepository;
import com.ll.hotel.domain.member.member.type.BusinessApprovalStatus;
import com.ll.hotel.domain.member.member.type.MemberStatus;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "admin", roles = {"ADMIN"})
public class AdminHotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    BusinessRepository businessRepository;

    @Autowired
    MemberRepository memberRepository;

    private Long testHotelId;

    @BeforeEach
    void setUp() {
        Member member = Member
                .builder()
                .birthDate(LocalDate.now())
                .memberEmail("member@gmail.com")
                .memberName("member")
                .memberPhoneNumber("01012345678")
                .memberStatus(MemberStatus.ACTIVE)
                .role(Role.BUSINESS)
                .build();
        memberRepository.save(member);

        Business business = Business
                .builder()
                .businessRegistrationNumber("1234567890")
                .startDate(LocalDate.now())
                .ownerName("김사장")
                .approvalStatus(BusinessApprovalStatus.PENDING)
                .member(member)
                .hotel(null)
                .build();
        businessRepository.save(business);

        Hotel hotel = Hotel
                .builder()
                .hotelName("호텔")
                .hotelExplainContent("대충 좋은 최상급 호텔")
                .hotelEmail("hotel@gmail.com")
                .zipCode(02153)
                .streetAddress("어쩌구로 15번길")
                .hotelGrade(5)
                .hotelStatus(HotelStatus.PENDING)
                .checkInTime(LocalTime.of(15, 0))
                .checkOutTime(LocalTime.of(11, 0))
                .hotelPhoneNumber("01012345678")
                .averageRating(5.6)
                .totalReviewCount(3L)
                .totalReviewRatingSum(5L)
                .business(business)
                .build();

        hotelRepository.save(hotel);

        testHotelId = hotel.getId();
    }

    @Test
    @DisplayName("호텔 페이지 조회")
    void findAllPagedTest1() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(
                        get("/api/admin/hotels")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(AdminHotelController.class))
                .andExpect(handler().methodName("getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(HttpStatus.OK.name()));
    }

    @Test
    @DisplayName("호텔 페이지 조회 - 잘못된 페이지를 요청한 경우")
    void findAllPaged2() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(
                        get("/api/admin/hotels?page=20")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(AdminHotelController.class))
                .andExpect(handler().methodName("getAll"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("호텔 조회")
    void getByIdTest1() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(
                        get("/api/admin/hotels/{id}", testHotelId)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(AdminHotelController.class))
                .andExpect(handler().methodName("getById"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(HttpStatus.OK.name()));
    }

    @Test
    @DisplayName("호텔 조회 - 요청이 잘못된 경우")
    void getByIdTest2() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(
                        get("/api/admin/hotels/25")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(AdminHotelController.class))
                .andExpect(handler().methodName("getById"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("사업자 승인")
    void approveTest() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(
                        patch("/api/admin/hotels/{id}", testHotelId)
                                .content("""
                                        {
                                            "hotelStatus": "AVAILABLE"
                                        }
                                        """.stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(AdminHotelController.class))
                .andExpect(handler().methodName("approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(HttpStatus.OK.name()));
    }
}
