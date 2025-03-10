package com.ll.hotel.domain.review.comment.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ll.hotel.domain.member.member.entity.Role
import com.ll.hotel.domain.review.comment.dto.request.ReviewCommentContentRequest
import com.ll.hotel.domain.review.comment.entity.ReviewComment
import com.ll.hotel.domain.review.comment.repository.ReviewCommentRepository
import com.ll.hotel.domain.review.review.entity.Review
import com.ll.hotel.domain.review.review.repository.ReviewRepository
import com.ll.hotel.global.security.oauth2.dto.SecurityUser
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import org.assertj.core.api.Assertions.assertThat
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
class ReviewCommentControllerTest {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var reviewRepository: ReviewRepository

    @Autowired
    lateinit var reviewCommentRepository: ReviewCommentRepository

    @Autowired
    lateinit var mvc: MockMvc

    private fun setUpAuthentication(userId: Long, name: String, email: String, role: Role) {
        val securityUser = SecurityUser.of(userId, name, email, "ROLE_${role.name}")
        val auth: Authentication = UsernamePasswordAuthenticationToken(
            securityUser, null, securityUser.authorities
        )
        SecurityContextHolder.getContext().authentication = auth
    }

    private fun setReviewComment(reviewId: Long): ReviewComment {
        val review = reviewRepository.findById(reviewId).get()
        return reviewCommentRepository.save(ReviewComment(review, "리뷰1에 대한 답변"))
    }

    @Test
    @DisplayName("정상 답변 생성")
    fun 정상답변생성() {
        setUpAuthentication(1L, "business1", "business1@hotel.com", Role.BUSINESS)

        val reviewId = 1L
        val request = ReviewCommentContentRequest("좋은 리뷰 감사합니다")

        val resultActions: ResultActions = mvc.perform(
            post("/api/reviews/{reviewId}/comments", reviewId)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions
            .andExpect(handler().handlerType(ReviewCommentController::class.java))
            .andExpect(handler().methodName("createReviewComment"))
            .andExpect(status().isNoContent) // TODO: isCreated()로 변경 필요
    }

    @Test
    @DisplayName("비정상 답변 생성 - 인증 X")
    fun 비정상답변생성() {
        val reviewId = 1L
        val request = ReviewCommentContentRequest("좋은 리뷰 감사합니다")

        val resultActions: ResultActions = mvc.perform(
            post("/api/reviews/{reviewId}/comments", reviewId)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions
            .andExpect(handler().handlerType(ReviewCommentController::class.java))
            .andExpect(handler().methodName("createReviewComment"))
            .andExpect(status().isUnauthorized)
            .andExpect(content().string("로그인이 필요합니다."))
    }

    @Test
    @DisplayName("비정상 답변 생성 - 권한 X")
    fun 비정상답변생성2() {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER)

        val reviewId = 1L
        val request = ReviewCommentContentRequest("좋은 리뷰 감사합니다")

        val resultActions: ResultActions = mvc.perform(
            post("/api/reviews/{reviewId}/comments", reviewId)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions
            .andExpect(handler().handlerType(ReviewCommentController::class.java))
            .andExpect(handler().methodName("createReviewComment"))
            .andExpect(status().isForbidden)
            .andExpect(content().string("리뷰 답변 생성 권한이 없습니다"))
    }

    @Test
    @DisplayName("비정상 답변 생성 - 리뷰 존재 X")
    fun 비정상답변생성3() {
        setUpAuthentication(1L, "business1", "business1@hotel.com", Role.BUSINESS)

        val reviewId = 10000L
        val request = ReviewCommentContentRequest("좋은 리뷰 감사합니다")

        val resultActions: ResultActions = mvc.perform(
            post("/api/reviews/{reviewId}/comments", reviewId)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions
            .andExpect(handler().handlerType(ReviewCommentController::class.java))
            .andExpect(handler().methodName("createReviewComment"))
            .andExpect(status().isNotFound)
            .andExpect(content().string("리뷰가 존재하지 않습니다"))
    }

    @Test
    @DisplayName("정상 답변 수정")
    fun 정상답변수정() {
        setUpAuthentication(1L, "business1", "business1@hotel.com", Role.BUSINESS)

        val reviewId = 1L
        val commentId = setReviewComment(reviewId).id

        val request = ReviewCommentContentRequest("리뷰 1 수정하기")

        val resultActions: ResultActions = mvc.perform(
            put("/api/reviews/{reviewId}/comments/{commentId}", reviewId, commentId)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        val reviewComment = reviewCommentRepository.findById(commentId).get()

        resultActions
            .andExpect(handler().handlerType(ReviewCommentController::class.java))
            .andExpect(handler().methodName("updateReviewComment"))
            .andExpect(status().isNoContent)

        assertThat(reviewComment.content).isEqualTo(request.content)
    }

    @Test
    @DisplayName("비정상 답변 수정 - 인증 X")
    fun 비정상답변수정() {
        val reviewId = 1L
        val commentId = setReviewComment(reviewId).id

        val request = ReviewCommentContentRequest("리뷰 1 수정하기")

        val resultActions: ResultActions = mvc.perform(
            put("/api/reviews/{reviewId}/comments/{commentId}", reviewId, commentId)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions
            .andExpect(handler().handlerType(ReviewCommentController::class.java))
            .andExpect(handler().methodName("updateReviewComment"))
            .andExpect(status().isUnauthorized)
            .andExpect(content().string("로그인이 필요합니다."))
    }

    @Test
    @DisplayName("비정상 답변 수정 - 권한 X")
    fun 비정상답변수정2() {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER)

        val reviewId = 1L
        val commentId = setReviewComment(reviewId).id

        val request = ReviewCommentContentRequest("리뷰 1 수정하기")

        val resultActions: ResultActions = mvc.perform(
            put("/api/reviews/{reviewId}/comments/{commentId}", reviewId, commentId)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions
            .andExpect(handler().handlerType(ReviewCommentController::class.java))
            .andExpect(handler().methodName("updateReviewComment"))
            .andExpect(status().isForbidden)
            .andExpect(content().string("리뷰 답변 수정 권한이 없습니다"))
    }

    @Test
    @DisplayName("비정상 답변 수정 - 답변 존재 X")
    fun 비정상답변수정3() {
        setUpAuthentication(1L, "business1", "business1@hotel.com", Role.BUSINESS)

        val reviewId = 1L
        val commentId = 0L

        val request = ReviewCommentContentRequest("리뷰 1 수정하기")

        val resultActions: ResultActions = mvc.perform(
            put("/api/reviews/{reviewId}/comments/{commentId}", reviewId, commentId)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions
            .andExpect(handler().handlerType(ReviewCommentController::class.java))
            .andExpect(handler().methodName("updateReviewComment"))
            .andExpect(status().isNotFound)
            .andExpect(content().string("리뷰 답변이 존재하지 않습니다"))
    }

    @Test
    @DisplayName("정상 답변 삭제")
    fun 정상답변삭제() {
        setUpAuthentication(1L, "business1", "business1@hotel.com", Role.BUSINESS)

        val reviewId = 1L
        val commentId = setReviewComment(reviewId).id

        val resultActions: ResultActions = mvc.perform(
            delete("/api/reviews/{reviewId}/comments/{commentId}", reviewId, commentId)
        ).andDo(print())

        assertThat(reviewRepository.findById(reviewId).get().reviewComment).isNull()

        resultActions
            .andExpect(handler().handlerType(ReviewCommentController::class.java))
            .andExpect(handler().methodName("deleteReviewComment"))
            .andExpect(status().isNoContent)
    }

    @Test
    @DisplayName("비정상 답변 삭제 - 인증 X")
    fun 비정상답변삭제() {
        val reviewId = 1L
        val commentId = setReviewComment(reviewId).id

        val resultActions: ResultActions = mvc.perform(
            delete("/api/reviews/{reviewId}/comments/{commentId}", reviewId, commentId)
        ).andDo(print())

        resultActions
            .andExpect(handler().handlerType(ReviewCommentController::class.java))
            .andExpect(handler().methodName("deleteReviewComment"))
            .andExpect(status().isUnauthorized)
            .andExpect(content().string("로그인이 필요합니다."))
    }

    @Test
    @DisplayName("비정상 답변 삭제 - 권한 X")
    fun 비정상답변삭제2() {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER)

        val reviewId = 1L
        val commentId = setReviewComment(reviewId).id

        val resultActions: ResultActions = mvc.perform(
            delete("/api/reviews/{reviewId}/comments/{commentId}", reviewId, commentId)
        ).andDo(print())

        resultActions
            .andExpect(handler().handlerType(ReviewCommentController::class.java))
            .andExpect(handler().methodName("deleteReviewComment"))
            .andExpect(status().isForbidden)
            .andExpect(content().string("리뷰 답변 삭제 권한이 없습니다"))
    }

    @Test
    @DisplayName("비정상 답변 삭제 - 답변 존재 X")
    fun 비정상답변삭제3() {
        setUpAuthentication(1L, "business1", "business1@hotel.com", Role.BUSINESS)

        val reviewId = 1L
        val commentId = 0L

        val resultActions: ResultActions = mvc.perform(
            delete("/api/reviews/{reviewId}/comments/{commentId}", reviewId, commentId)
        ).andDo(print())

        resultActions
            .andExpect(handler().handlerType(ReviewCommentController::class.java))
            .andExpect(handler().methodName("deleteReviewComment"))
            .andExpect(status().isNotFound)
            .andExpect(content().string("리뷰 답변이 존재하지 않습니다"))
    }
}