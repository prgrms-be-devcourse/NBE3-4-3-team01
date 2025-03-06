package com.ll.hotel.domain.review.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.hotel.domain.image.entity.Image;
import com.ll.hotel.domain.image.service.ImageService;
import com.ll.hotel.domain.image.type.ImageType;
import com.ll.hotel.domain.member.member.entity.Role;
import com.ll.hotel.domain.review.review.dto.request.PostReviewRequest;
import com.ll.hotel.domain.review.review.dto.request.UpdateReviewRequest;
import com.ll.hotel.domain.review.review.entity.Review;
import com.ll.hotel.domain.review.review.repository.ReviewRepository;
import com.ll.hotel.domain.review.review.service.ReviewService;
import com.ll.hotel.global.exceptions.ServiceException;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
public class ReviewControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ImageService imageService;

    void setUpAuthentication(Long userId, String name, String email, Role role) {
        SecurityUser securityUser = SecurityUser.of(userId, name, email, "ROLE_"+role.name());
        Authentication auth = new UsernamePasswordAuthenticationToken(
                securityUser, null, securityUser.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("정상 리뷰 생성")
    void 정상리뷰생성() throws Exception {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER);

        long bookingId = 1L;
        long hotelId = 1L;
        long roomId = 1L;

        PostReviewRequest postReviewRequest = new PostReviewRequest("좋은 호텔이네요", 4, List.of("jpg", "png"));

        ResultActions resultActions = mvc.perform(
                post("/api/reviews/{bookingId}?hotelId={hotelId}&roomId={roomId}", bookingId, hotelId, roomId)
                        .content(objectMapper.writeValueAsString(postReviewRequest))
                        .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)));

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("createReview"))
                .andExpect(status().isCreated()) // TODO : isCreated() 로 변경 필요
                .andExpect(jsonPath("$.data.presignedUrls").isArray())
                .andExpect(jsonPath("$.data.presignedUrls.size()").value(postReviewRequest.imageExtensions().size()))
                .andExpect(jsonPath("$.data.reviewId").exists());
    }

    @Test
    @DisplayName("비정상 리뷰 생성 - rating 범위 초과")
    void 비정상리뷰생성() throws Exception {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER);

        long bookingId = 1L;
        long hotelId = 1L;
        long roomId = 1L;

        PostReviewRequest postReviewRequest = new PostReviewRequest("좋은 호텔이네요", 100, List.of("jpg", "png"));

        ResultActions resultActions = mvc.perform(
                post("/api/reviews/{bookingId}?hotelId={hotelId}&roomId={roomId}", bookingId, hotelId, roomId)
                        .content(objectMapper.writeValueAsString(postReviewRequest))
                        .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)));

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("createReview"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("평점은 최대 5점이어야 합니다.")));
    }

    @Test
    @DisplayName("비정상 리뷰 생성 - 인증 안함")
    void 비정상리뷰생성2() throws Exception {

        long bookingId = 1L;
        long hotelId = 1L;
        long roomId = 1L;

        PostReviewRequest postReviewRequest = new PostReviewRequest("좋은 호텔이네요", 4, List.of("jpg", "png"));

        ResultActions resultActions = mvc.perform(
                post("/api/reviews/{bookingId}?hotelId={hotelId}&roomId={roomId}", bookingId, hotelId, roomId)
                        .content(objectMapper.writeValueAsString(postReviewRequest))
                        .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)));

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("createReview"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("로그인이 필요합니다."));
    }

    @Test
    @DisplayName("비정상 리뷰 생성 - 권한 실패")
    void 비정상리뷰생성3() throws Exception {
        setUpAuthentication(2L, "customer2", "customer2@hotel.com", Role.USER);

        long bookingId = 1L;
        long hotelId = 1L;
        long roomId = 1L;

        PostReviewRequest postReviewRequest = new PostReviewRequest("좋은 호텔이네요", 4, List.of("jpg", "png"));

        ResultActions resultActions = mvc.perform(
                post("/api/reviews/{bookingId}?hotelId={hotelId}&roomId={roomId}", bookingId, hotelId, roomId)
                        .content(objectMapper.writeValueAsString(postReviewRequest))
                        .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)));

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("createReview"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("리뷰 생성 권한이 없습니다"));
    }

    @Test
    @DisplayName("정상 사진 Url 저장")
    void 정상사진Url저장() throws Exception {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER);

        long reviewId = 1L;

        List<Image> beforeImages = imageService.findImagesById(ImageType.REVIEW, reviewId);

        List<String> urls = List.of(
                "https://test-bucket.s3.amazonaws.com/reviews/2/3.jpg",
                "https://test-bucket.s3.amazonaws.com/reviews/2/4.jpg");

        ResultActions resultActions = mvc.perform(
                post("/api/reviews/{reviewId}/urls", reviewId)
                        .content(objectMapper.writeValueAsString(urls))
                        .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)));

        List<Image> afterImages = imageService.findImagesById(ImageType.REVIEW, reviewId);

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("saveImageUrls"))
                .andExpect(status().isNoContent());

        assertThat(afterImages).hasSize(beforeImages.size() + urls.size());
    }

    @Test
    @DisplayName("비정상 사진 Url 저장 - 인증 X")
    void 비정상사진Url저장() throws Exception {
        long reviewId = 1L;

        List<String> urls = List.of(
                "https://test-bucket.s3.amazonaws.com/reviews/2/3.jpg",
                "https://test-bucket.s3.amazonaws.com/reviews/2/4.jpg");

        ResultActions resultActions = mvc.perform(
                post("/api/reviews/{reviewId}/urls", reviewId)
                        .content(objectMapper.writeValueAsString(urls))
                        .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)));

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("saveImageUrls"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("로그인이 필요합니다."));
    }

    @Test
    @DisplayName("비정상 사진 Url 저장 - 권한 X")
    void 비정상사진Url저장2() throws Exception {
        setUpAuthentication(2L, "customer2", "customer2@hotel.com", Role.USER);

        long reviewId = 1L;

        List<String> urls = List.of(
                "https://test-bucket.s3.amazonaws.com/reviews/2/3.jpg",
                "https://test-bucket.s3.amazonaws.com/reviews/2/4.jpg");

        ResultActions resultActions = mvc.perform(
                post("/api/reviews/{reviewId}/urls", reviewId)
                        .content(objectMapper.writeValueAsString(urls))
                        .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)));

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("saveImageUrls"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("리뷰 사진 저장 권한이 없습니다"));
    }

    @Test
    @DisplayName("정상 리뷰 단건 조회")
    void 정상리뷰단건조회() throws Exception {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER);

        long reviewId = 1L;
        Review review = reviewService.getReview(reviewId);
        List<Image> images = imageService.findImagesById(ImageType.REVIEW, reviewId);

        ResultActions resultActions = mvc.perform(
                get("/api/reviews/{reviewId}", reviewId));

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("getReview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewDto.reviewId").value(review.getId()))
                .andExpect(jsonPath("$.data.reviewDto.rating").value(review.getRating()))
                .andExpect(jsonPath("$.data.reviewDto.content").value(review.getContent()))
                .andExpect(jsonPath("$.data.imageUrls.size()").value(images.size()));
    }


    @Test
    @DisplayName("비정상 리뷰 단건 조회 - 인증 X")
    void 비정상리뷰단건조회() throws Exception {
        long reviewId = 1L;

        ResultActions resultActions = mvc.perform(
                get("/api/reviews/{reviewId}", reviewId));

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("getReview"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("로그인이 필요합니다."));
    }

    @Test
    @DisplayName("비정상 리뷰 단건 조회 - 권한 X")
    void 비정상리뷰단건조회2() throws Exception {
        setUpAuthentication(2L, "customer2", "customer2@hotel.com", Role.USER);

        long reviewId = 1L;

        ResultActions resultActions = mvc.perform(
                get("/api/reviews/{reviewId}", reviewId));

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("getReview"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("리뷰 조회 권한이 없습니다"));
    }

    @Test
    @DisplayName("비정상 리뷰 단건 조회 - 리뷰 존재 X")
    void 비정상리뷰단건조회3() throws Exception {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER);

        long reviewId = 10000L;

        ResultActions resultActions = mvc.perform(
                get("/api/reviews/{reviewId}", reviewId));

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("getReview"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("리뷰가 존재하지 않습니다"));
    }

    @Test
    @DisplayName("정상 리뷰 수정")
    void 정상리뷰수정() throws Exception {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER);

        long reviewId = 1L;

        List<Image> beforeImages = imageService.findImagesById(ImageType.REVIEW, reviewId);

        UpdateReviewRequest updateReviewRequest = new UpdateReviewRequest("리뷰 1 내용 수정", 3,
                List.of("https://test-bucket.s3.amazonaws.com/reviews/2/1.jpg", "https://test-bucket.s3.amazonaws.com/reviews/2/2.jpg"),
                List.of("jpg"));

        ResultActions resultActions = mvc.perform(
                put("/api/reviews/{reviewId}", reviewId)
                        .content(objectMapper.writeValueAsString(updateReviewRequest))
                        .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)))
                .andDo(print());

        Review review = reviewService.getReview(reviewId);
        List<Image> afterImages = imageService.findImagesById(ImageType.REVIEW, reviewId);

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("updateReview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.presignedUrls.size()").value(updateReviewRequest.newImageExtensions().size()));

        // 수정된 리뷰 내용
        assertThat(updateReviewRequest.content()).isEqualTo(review.getContent());
        assertThat(updateReviewRequest.rating()).isEqualTo(review.getRating());
        assertThat(afterImages).hasSize(beforeImages.size() - updateReviewRequest.deleteImageUrls().size());
    }

    @Test
    @DisplayName("정상 리뷰 수정 - 인증 X")
    void 비정상리뷰수정() throws Exception {

        long reviewId = 1L;

        UpdateReviewRequest updateReviewRequest = new UpdateReviewRequest("리뷰 1 내용 수정", 3,
                List.of("https://test-bucket.s3.amazonaws.com/reviews/2/1.jpg", "https://test-bucket.s3.amazonaws.com/reviews/2/2.jpg"),
                List.of("jpg"));

        ResultActions resultActions = mvc.perform(
                        put("/api/reviews/{reviewId}", reviewId)
                                .content(objectMapper.writeValueAsString(updateReviewRequest))
                                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("updateReview"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("로그인이 필요합니다."));
    }

    @Test
    @DisplayName("정상 리뷰 수정 - 권한 X")
    void 비정상리뷰수정2() throws Exception {
        setUpAuthentication(2L, "customer2", "customer2@hotel.com", Role.USER);

        long reviewId = 1L;

        UpdateReviewRequest updateReviewRequest = new UpdateReviewRequest("리뷰 1 내용 수정", 3,
                List.of("https://test-bucket.s3.amazonaws.com/reviews/2/1.jpg", "https://test-bucket.s3.amazonaws.com/reviews/2/2.jpg"),
                List.of("jpg"));

        ResultActions resultActions = mvc.perform(
                        put("/api/reviews/{reviewId}", reviewId)
                                .content(objectMapper.writeValueAsString(updateReviewRequest))
                                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("updateReview"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("리뷰 수정 권한이 없습니다"));
    }

    @Test
    @DisplayName("정상 리뷰 삭제")
    void 정상리뷰삭제() throws Exception {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER);

        long reviewId = 1L;

        ResultActions resultActions = mvc.perform(
                delete("/api/reviews/{reviewId}", reviewId));


        assertThrows(ServiceException.class, () -> {reviewService.getReview(reviewId);});

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("deleteReview"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("비정상 리뷰 삭제 - 인증 X")
    void 비정상리뷰삭제() throws Exception {
        long reviewId = 1L;

        ResultActions resultActions = mvc.perform(
                delete("/api/reviews/{reviewId}", reviewId));

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("deleteReview"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("로그인이 필요합니다."));
    }

    @Test
    @DisplayName("비정상 리뷰 삭제 - 권한 X")
    void 비정상리뷰삭제2() throws Exception {
        setUpAuthentication(2L, "customer2", "customer2@hotel.com", Role.USER);

        long reviewId = 1L;

        ResultActions resultActions = mvc.perform(
                delete("/api/reviews/{reviewId}", reviewId));

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("deleteReview"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("리뷰 삭제 권한이 없습니다"));
    }

    @Test
    @DisplayName("비정상 리뷰 삭제 - 리뷰 존재 X")
    void 비정상리뷰삭제3() throws Exception {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER);

        long reviewId = 10000L;

        ResultActions resultActions = mvc.perform(
                delete("/api/reviews/{reviewId}", reviewId));

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("deleteReview"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("리뷰가 존재하지 않습니다"));
    }

    @Test
    @DisplayName("정상 내 리뷰 목록 조회")
    void 정상_내_리뷰_목록_조회() throws Exception {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER);

        int pageNum = 1;

        ResultActions resultActions = mvc.perform(
                get("/api/reviews/me?page={pageNum}", pageNum));

        List<Review> reviews = reviewRepository.findByMemberId(1L);

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("getMyReviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentPageNumber").value(pageNum))
                .andExpect(jsonPath("$.data.items.size()").value(reviews.size()))
                .andExpect(jsonPath("$.data.items[0].myReviewWithCommentDto.hotelName").value("강남호텔"))
                .andExpect(jsonPath("$.data.items[0].myReviewWithCommentDto.roomTypeName").value("스탠다드룸"))
                .andExpect(jsonPath("$.data.items[0].myReviewWithCommentDto.reviewDto.reviewId").value(reviews.get(0).getId()))
                .andExpect(jsonPath("$.data.items[1].myReviewWithCommentDto.hotelName").value("강남호텔"))
                .andExpect(jsonPath("$.data.items[1].myReviewWithCommentDto.roomTypeName").value("스탠다드룸"))
                .andExpect(jsonPath("$.data.items[1].myReviewWithCommentDto.reviewDto.reviewId").value(reviews.get(1).getId()));
    }

    @Test
    @DisplayName("비정상 내 리뷰 목록 조회 - 인증 X")
    void 비정상_내_리뷰_목록_조회() throws Exception {

        int pageNum = 1;

        ResultActions resultActions = mvc.perform(
                get("/api/reviews/me?page={pageNum}", pageNum));

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("getMyReviews"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("로그인이 필요합니다."));
    }

    @Test
    @DisplayName("비정상 내 리뷰 목록 조회 - 권한 X")
    void 비정상_내_리뷰_목록_조회2() throws Exception {
        setUpAuthentication(1L, "business1", "business1@hotel.com", Role.BUSINESS);

        int pageNum = 1;

        ResultActions resultActions = mvc.perform(
                get("/api/reviews/me?page={pageNum}", pageNum));

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("getMyReviews"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("내 리뷰 목록 조회는 손님만 가능합니다"));
    }

    @Test
    @DisplayName("정상 호텔 리뷰 목록 조회")
    void 정상_호텔_리뷰_목록_조회() throws Exception {
        long hotelId = 1L;
        int pageNum = 1;

        ResultActions resultActions = mvc.perform(
                get("/api/reviews/hotels/{hotelId}?page={pageNum}", hotelId, pageNum));

        List<Review> reviews = reviewRepository.findByHotelId(hotelId);

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("getHotelReviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hotelReviewPage.currentPageNumber").value(pageNum))
                .andExpect(jsonPath("$.data.hotelReviewPage.items.size()").value(reviews.size()))
                .andExpect(jsonPath("$.data.hotelReviewPage.items[0].hotelReviewWithCommentDto.roomTypeName").value("스탠다드룸"))
                .andExpect(jsonPath("$.data.hotelReviewPage.items[0].hotelReviewWithCommentDto.reviewDto.reviewId").value(reviews.get(0).getId()))
                .andExpect(jsonPath("$.data.hotelReviewPage.items[1].hotelReviewWithCommentDto.roomTypeName").value("스탠다드룸"))
                .andExpect(jsonPath("$.data.hotelReviewPage.items[1].hotelReviewWithCommentDto.reviewDto.reviewId").value(reviews.get(1).getId()))
                .andExpect(jsonPath("$.data.averageRating").value(reviews.getFirst().getHotel().getAverageRating()));
    }

    @Test
    @DisplayName("비정상 호텔 리뷰 목록 조회 - 호텔 X")
    void 비정상_호텔_리뷰_목록_조회() throws Exception {
        long hotelId = 100L;
        int pageNum = 1;

        ResultActions resultActions = mvc.perform(
                get("/api/reviews/hotels/{hotelId}?page={pageNum}", hotelId, pageNum));

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("getHotelReviews"))
                .andExpect(status().isNotFound());

    }
}
