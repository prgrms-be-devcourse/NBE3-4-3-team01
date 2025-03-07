package com.ll.hotel.domain.review.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.hotel.domain.member.member.entity.Role;
import com.ll.hotel.domain.review.comment.dto.request.ReviewCommentContentRequest;
import com.ll.hotel.domain.review.comment.entity.ReviewComment;
import com.ll.hotel.domain.review.comment.repository.ReviewCommentRepository;
import com.ll.hotel.domain.review.review.entity.Review;
import com.ll.hotel.domain.review.review.repository.ReviewRepository;
import com.ll.hotel.global.security.oauth2.dto.SecurityUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
public class ReviewCommentControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    ReviewCommentRepository reviewCommentRepository;

    @Autowired
    private MockMvc mvc;

    void setUpAuthentication(Long userId, String name, String email, Role role) {
        SecurityUser securityUser = SecurityUser.of(userId, name, email, "ROLE_"+role.name());
        Authentication auth = new UsernamePasswordAuthenticationToken(
                securityUser, null, securityUser.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    ReviewComment setReviewComment(long reviewId) {
        Review review = reviewRepository.findById(reviewId).get();
        return reviewCommentRepository.save(new ReviewComment(review, "리뷰1에 대한 답변"));
    }

    @Test
    @DisplayName("정상 답변 생성")
    void 정상답변생성() throws Exception {
        setUpAuthentication(1L, "business1", "business1@hotel.com", Role.BUSINESS);

        long reviewId = 1L;

        ReviewCommentContentRequest request = new ReviewCommentContentRequest("좋은 리뷰 감사합니다");

        ResultActions resultActions = mvc.perform(
                post("/api/reviews/{reviewId}/comments", reviewId)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ReviewCommentController.class))
                .andExpect(handler().methodName("createReviewComment"))
                .andExpect(status().isNoContent()); // TODO: isCreated() 로 변경 필요
    }

    @Test
    @DisplayName("비정상 답변 생성 - 인증 X")
    void 비정상답변생성() throws Exception {

        long reviewId = 1L;

        ReviewCommentContentRequest request = new ReviewCommentContentRequest("좋은 리뷰 감사합니다");

        ResultActions resultActions = mvc.perform(
                        post("/api/reviews/{reviewId}/comments", reviewId)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ReviewCommentController.class))
                .andExpect(handler().methodName("createReviewComment"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("로그인이 필요합니다."));
    }

    @Test
    @DisplayName("비정상 답변 생성 - 권한 X")
    void 비정상답변생성2() throws Exception {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER);

        long reviewId = 1L;

        ReviewCommentContentRequest request = new ReviewCommentContentRequest("좋은 리뷰 감사합니다");

        ResultActions resultActions = mvc.perform(
                        post("/api/reviews/{reviewId}/comments", reviewId)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ReviewCommentController.class))
                .andExpect(handler().methodName("createReviewComment"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("리뷰 답변 생성 권한이 없습니다"));
    }

    @Test
    @DisplayName("비정상 답변 생성 - 리뷰 존재 X")
    void 비정상답변생성3() throws Exception {
        setUpAuthentication(1L, "business1", "business1@hotel.com", Role.BUSINESS);

        long reviewId = 10000L;

        ReviewCommentContentRequest request = new ReviewCommentContentRequest("좋은 리뷰 감사합니다");

        ResultActions resultActions = mvc.perform(
                        post("/api/reviews/{reviewId}/comments", reviewId)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ReviewCommentController.class))
                .andExpect(handler().methodName("createReviewComment"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("리뷰가 존재하지 않습니다"));
    }

    @Test
    @DisplayName("정상 답변 수정")
    void 정상답변수정() throws Exception {
        setUpAuthentication(1L, "business1", "business1@hotel.com", Role.BUSINESS);

        long reviewId = 1L;
        long commentId = setReviewComment(reviewId).getId();

        ReviewCommentContentRequest request = new ReviewCommentContentRequest("리뷰 1 수정하기");

        ResultActions resultActions = mvc.perform(
                        put("/api/reviews/{reviewId}/comments/{commentId}", reviewId, commentId)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)))
                .andDo(print());

        ReviewComment reviewComment = reviewCommentRepository.findById(commentId).get();

        resultActions
                .andExpect(handler().handlerType(ReviewCommentController.class))
                .andExpect(handler().methodName("updateReviewComment"))
                .andExpect(status().isNoContent());

        assertThat(reviewComment.getContent()).isEqualTo(request.content());
    }

    @Test
    @DisplayName("비정상 답변 수정 - 인증 X")
    void 비정상답변수정() throws Exception {
        long reviewId = 1L;
        long commentId = setReviewComment(reviewId).getId();

        ReviewCommentContentRequest request = new ReviewCommentContentRequest("리뷰 1 수정하기");

        ResultActions resultActions = mvc.perform(
                        put("/api/reviews/{reviewId}/comments/{commentId}", reviewId, commentId)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ReviewCommentController.class))
                .andExpect(handler().methodName("updateReviewComment"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("로그인이 필요합니다."));
    }

    @Test
    @DisplayName("비정상 답변 수정 - 권한 X")
    void 비정상답변수정2() throws Exception {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER);

        long reviewId = 1L;
        long commentId = setReviewComment(reviewId).getId();

        ReviewCommentContentRequest request = new ReviewCommentContentRequest("리뷰 1 수정하기");

        ResultActions resultActions = mvc.perform(
                        put("/api/reviews/{reviewId}/comments/{commentId}", reviewId, commentId)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ReviewCommentController.class))
                .andExpect(handler().methodName("updateReviewComment"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("리뷰 답변 수정 권한이 없습니다"));

    }

    @Test
    @DisplayName("비정상 답변 수정 - 답변 존재 X")
    void 비정상답변수정3() throws Exception {
        setUpAuthentication(1L, "business1", "business1@hotel.com", Role.BUSINESS);

        long reviewId = 1L;
        long commentId = 0L;

        ReviewCommentContentRequest request = new ReviewCommentContentRequest("리뷰 1 수정하기");

        ResultActions resultActions = mvc.perform(
                        put("/api/reviews/{reviewId}/comments/{commentId}", reviewId, commentId)
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ReviewCommentController.class))
                .andExpect(handler().methodName("updateReviewComment"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("리뷰 답변이 존재하지 않습니다"));
    }

    @Test
    @DisplayName("정상 답변 삭제")
    void 정상답변삭제() throws Exception {
        setUpAuthentication(1L, "business1", "business1@hotel.com", Role.BUSINESS);

        long reviewId = 1L;
        long commentId = setReviewComment(reviewId).getId();

        ResultActions resultActions = mvc.perform(
                        delete("/api/reviews/{reviewId}/comments/{commentId}", reviewId, commentId)
                )
                .andDo(print());

        assertThat(reviewRepository.findById(reviewId).get().getReviewComment()).isNull();

        resultActions
                .andExpect(handler().handlerType(ReviewCommentController.class))
                .andExpect(handler().methodName("deleteReviewComment"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("비정상 답변 삭제 - 인증 X")
    void 비정상답변삭제() throws Exception {

        long reviewId = 1L;
        long commentId = setReviewComment(reviewId).getId();

        ResultActions resultActions = mvc.perform(
                        delete("/api/reviews/{reviewId}/comments/{commentId}", reviewId, commentId)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ReviewCommentController.class))
                .andExpect(handler().methodName("deleteReviewComment"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("로그인이 필요합니다."));
    }

    @Test
    @DisplayName("비정상 답변 삭제 - 권한 X")
    void 비정상답변삭제2() throws Exception {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER);

        long reviewId = 1L;
        long commentId = setReviewComment(reviewId).getId();

        ResultActions resultActions = mvc.perform(
                        delete("/api/reviews/{reviewId}/comments/{commentId}", reviewId, commentId)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ReviewCommentController.class))
                .andExpect(handler().methodName("deleteReviewComment"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("리뷰 답변 삭제 권한이 없습니다"));
    }

    @Test
    @DisplayName("비정상 답변 삭제 - 답변 존재 X")
    void 비정상답변삭제3() throws Exception {
        setUpAuthentication(1L, "business1", "business1@hotel.com", Role.BUSINESS);

        long reviewId = 1L;
        long commentId = 0L;

        ResultActions resultActions = mvc.perform(
                        delete("/api/reviews/{reviewId}/comments/{commentId}", reviewId, commentId)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ReviewCommentController.class))
                .andExpect(handler().methodName("deleteReviewComment"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("리뷰 답변이 존재하지 않습니다"));
    }
}
