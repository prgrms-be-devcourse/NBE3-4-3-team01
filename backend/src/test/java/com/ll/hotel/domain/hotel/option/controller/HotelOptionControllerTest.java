package com.ll.hotel.domain.hotel.option.controller;

import com.ll.hotel.domain.hotel.option.cotroller.HotelOptionController;
import com.ll.hotel.domain.hotel.option.entity.HotelOption;
import com.ll.hotel.domain.hotel.option.repository.HotelOptionRepository;
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

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "admin", roles = {"ADMIN"})
public class HotelOptionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HotelOptionRepository hotelOptionRepository;

    private Long testId;

    @BeforeEach
    void setUp() {
        HotelOption hotelOption = hotelOptionRepository.save(HotelOption
                .builder()
                .name("호텔 옵션")
                .build()
        );
        testId = hotelOption.getId();
    }

    @Test
    @DisplayName("호텔 옵션 추가")
    void addHotelOptionTest() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(
                        post("/api/admin/hotel-options")
                                .content("""
                                        {
                                            "name": "추가 테스트"
                                        }
                                        """.stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(HotelOptionController.class))
                .andExpect(handler().methodName("add"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value(HttpStatus.CREATED.name()));
    }

    @Test
    @DisplayName("호텔 옵션 전체 조회")
    void getAllHotelOptionsTest() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(
                        get("/api/admin/hotel-options")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(HotelOptionController.class))
                .andExpect(handler().methodName("getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(HttpStatus.OK.name()));
    }

    @Test
    @DisplayName("호텔 옵션 수정")
    void modifyHotelOptionTest() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(
                        patch("/api/admin/hotel-options/{id}", testId)
                                .content("""
                                        {
                                            "name": "수정됨"
                                        }
                                        """.stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(HotelOptionController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(HttpStatus.OK.name()));
    }

    @Test
    @DisplayName("호텔 옵션 삭제")
    void deleteHotelOptionTest() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(
                        delete("/api/admin/hotel-options/{id}", testId)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(HotelOptionController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isNoContent());
    }
}
