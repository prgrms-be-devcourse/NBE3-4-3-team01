package com.ll.hotel.domain.hotel.option.controller

import com.ll.hotel.domain.hotel.option.entity.RoomOption
import com.ll.hotel.domain.hotel.option.repository.RoomOptionRepository
import jakarta.transaction.Transactional
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "admin", roles = ["ADMIN"])
class RoomOptionControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val roomOptionRepository: RoomOptionRepository
) {
    private var testId: Long = 0L

    @BeforeEach
    fun setUp() {
        testId = roomOptionRepository.save(RoomOption("객실 옵션")).id
    }

    @Test
    @DisplayName("객실 옵션 추가")
    fun `should add a new room option`() {
        val resultActions: ResultActions = mockMvc.perform(
            post("/api/admin/room-options")
                .content(
                    """
                    {
                        "name": "추가 테스트"
                    }
                    """.trimIndent()
                )
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print())

        resultActions
            .andExpect(handler().handlerType(RoomOptionController::class.java))
            .andExpect(handler().methodName("add"))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.resultCode", equalTo(HttpStatus.CREATED.name)))
    }

    @Test
    @DisplayName("객실 옵션 전체 조회")
    fun `should get all room options`() {
        val resultActions: ResultActions = mockMvc.perform(
            get("/api/admin/room-options")
        ).andDo(print())

        resultActions
            .andExpect(handler().handlerType(RoomOptionController::class.java))
            .andExpect(handler().methodName("getAll"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode", equalTo(HttpStatus.OK.name)))
    }

    @Test
    @DisplayName("객실 옵션 수정")
    fun `should modify a room option`() {
        val resultActions: ResultActions = mockMvc.perform(
            patch("/api/admin/room-options/{id}", testId)
                .content(
                    """
                    {
                        "name": "수정됨"
                    }
                    """.trimIndent()
                )
                .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print())

        resultActions
            .andExpect(handler().handlerType(RoomOptionController::class.java))
            .andExpect(handler().methodName("modify"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode", equalTo(HttpStatus.OK.name)))
    }

    @Test
    @DisplayName("객실 옵션 삭제")
    fun `should delete a room option`() {
        val resultActions: ResultActions = mockMvc.perform(
            delete("/api/admin/room-options/{id}", testId)
        ).andDo(print())

        resultActions
            .andExpect(handler().handlerType(RoomOptionController::class.java))
            .andExpect(handler().methodName("delete"))
            .andExpect(status().isNoContent)
    }
}