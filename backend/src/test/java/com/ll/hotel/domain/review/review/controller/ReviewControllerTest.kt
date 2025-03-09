package com.ll.hotel.domain.review.review.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ll.hotel.domain.image.entity.Image
import com.ll.hotel.domain.image.service.ImageService
import com.ll.hotel.domain.image.type.ImageType
import com.ll.hotel.domain.member.member.entity.Role
import com.ll.hotel.domain.review.review.dto.request.PostReviewRequest
import com.ll.hotel.domain.review.review.dto.request.UpdateReviewRequest
import com.ll.hotel.domain.review.review.entity.Review
import com.ll.hotel.domain.review.review.repository.ReviewRepository
import com.ll.hotel.domain.review.review.service.ReviewService
import com.ll.hotel.global.exceptions.ServiceException
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
import kotlin.test.assertFailsWith
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsString
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
class ReviewControllerTest {

    @Autowired
    lateinit var mvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var reviewService: ReviewService

    @Autowired
    lateinit var reviewRepository: ReviewRepository

    @Autowired
    lateinit var imageService: ImageService

    private fun setUpAuthentication(userId: Long, name: String, email: String, role: Role) {
        val securityUser = SecurityUser.of(userId, name, email, "ROLE_${role.name}")
        val auth: Authentication = UsernamePasswordAuthenticationToken(
                securityUser, null, securityUser.authorities
        )
        SecurityContextHolder.getContext().authentication = auth
    }

    @Test
    @DisplayName("정상 리뷰 생성")
    fun 정상리뷰생성() {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER)

        val bookingId = 1L
        val hotelId = 1L
        val roomId = 1L

        val postReviewRequest = PostReviewRequest("좋은 호텔이네요", 4, listOf("jpg", "png"))

        val resultActions: ResultActions = mvc.perform(
                post("/api/reviews/{bookingId}?hotelId={hotelId}&roomId={roomId}", bookingId, hotelId, roomId)
                        .content(objectMapper.writeValueAsString(postReviewRequest))
                        .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        )

        resultActions
                .andExpect(handler().handlerType(ReviewController::class.java))
            .andExpect(handler().methodName("createReview"))
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.data.presignedUrls").isArray)
                .andExpect(jsonPath("$.data.presignedUrls.size()").value(postReviewRequest.imageExtensions.size))
                .andExpect(jsonPath("$.data.reviewId").exists())
    }

    @Test
    @DisplayName("비정상 리뷰 생성 - rating 범위 초과")
    fun 비정상리뷰생성() {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER)

        val bookingId = 1L
        val hotelId = 1L
        val roomId = 1L

        val postReviewRequest = PostReviewRequest("좋은 호텔이네요", 100, listOf("jpg", "png"))

        val resultActions: ResultActions = mvc.perform(
                post("/api/reviews/{bookingId}?hotelId={hotelId}&roomId={roomId}", bookingId, hotelId, roomId)
                        .content(objectMapper.writeValueAsString(postReviewRequest))
                        .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        )

        resultActions
                .andExpect(handler().handlerType(ReviewController::class.java))
            .andExpect(handler().methodName("createReview"))
                .andExpect(status().isBadRequest)
                .andExpect(content().string(containsString("평점은 최대 5점이어야 합니다.")))
    }

    @Test
    @DisplayName("비정상 리뷰 생성 - 인증 안함")
    fun 비정상리뷰생성2() {
        val bookingId = 1L
        val hotelId = 1L
        val roomId = 1L

        val postReviewRequest = PostReviewRequest("좋은 호텔이네요", 4, listOf("jpg", "png"))

        val resultActions: ResultActions = mvc.perform(
                post("/api/reviews/{bookingId}?hotelId={hotelId}&roomId={roomId}", bookingId, hotelId, roomId)
                        .content(objectMapper.writeValueAsString(postReviewRequest))
                        .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        )

        resultActions
                .andExpect(handler().handlerType(ReviewController::class.java))
            .andExpect(handler().methodName("createReview"))
                .andExpect(status().isUnauthorized)
                .andExpect(content().string("로그인이 필요합니다."))
    }

    @Test
    @DisplayName("비정상 리뷰 생성 - 권한 실패")
    fun 비정상리뷰생성3() {
        setUpAuthentication(2L, "customer2", "customer2@hotel.com", Role.USER)

        val bookingId = 1L
        val hotelId = 1L
        val roomId = 1L

        val postReviewRequest = PostReviewRequest("좋은 호텔이네요", 4, listOf("jpg", "png"))

        val resultActions: ResultActions = mvc.perform(
                post("/api/reviews/{bookingId}?hotelId={hotelId}&roomId={roomId}", bookingId, hotelId, roomId)
                        .content(objectMapper.writeValueAsString(postReviewRequest))
                        .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        )

        resultActions
                .andExpect(handler().handlerType(ReviewController::class.java))
            .andExpect(handler().methodName("createReview"))
                .andExpect(status().isForbidden)
                .andExpect(content().string("리뷰 생성 권한이 없습니다"))
    }

    @Test
    @DisplayName("정상 사진 Url 저장")
    fun 정상사진Url저장() {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER)

        val reviewId = 1L

        val beforeImages = imageService.findImagesById(ImageType.REVIEW, reviewId)

        val urls = listOf(
                "https://test-bucket.s3.amazonaws.com/reviews/2/3.jpg",
                "https://test-bucket.s3.amazonaws.com/reviews/2/4.jpg"
        )

        val resultActions: ResultActions = mvc.perform(
                post("/api/reviews/{reviewId}/urls", reviewId)
                        .content(objectMapper.writeValueAsString(urls))
                        .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        )

        val afterImages = imageService.findImagesById(ImageType.REVIEW, reviewId)

        resultActions
                .andExpect(handler().handlerType(ReviewController::class.java))
            .andExpect(handler().methodName("saveImageUrls"))
                .andExpect(status().isNoContent)

        assertThat(afterImages).hasSize(beforeImages.size + urls.size)
    }

    @Test
    @DisplayName("비정상 사진 Url 저장 - 인증 X")
    fun 비정상사진Url저장() {
        val reviewId = 1L

        val urls = listOf(
                "https://test-bucket.s3.amazonaws.com/reviews/2/3.jpg",
                "https://test-bucket.s3.amazonaws.com/reviews/2/4.jpg"
        )

        val resultActions: ResultActions = mvc.perform(
                post("/api/reviews/{reviewId}/urls", reviewId)
                        .content(objectMapper.writeValueAsString(urls))
                        .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        )

        resultActions
                .andExpect(handler().handlerType(ReviewController::class.java))
            .andExpect(handler().methodName("saveImageUrls"))
                .andExpect(status().isUnauthorized)
                .andExpect(content().string("로그인이 필요합니다."))
    }

    @Test
    @DisplayName("비정상 사진 Url 저장 - 권한 X")
    fun 비정상사진Url저장2() {
        setUpAuthentication(2L, "customer2", "customer2@hotel.com", Role.USER)

        val reviewId = 1L

        val urls = listOf(
                "https://test-bucket.s3.amazonaws.com/reviews/2/3.jpg",
                "https://test-bucket.s3.amazonaws.com/reviews/2/4.jpg"
        )

        val resultActions: ResultActions = mvc.perform(
                post("/api/reviews/{reviewId}/urls", reviewId)
                        .content(objectMapper.writeValueAsString(urls))
                        .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        )

        resultActions
                .andExpect(handler().handlerType(ReviewController::class.java))
            .andExpect(handler().methodName("saveImageUrls"))
                .andExpect(status().isForbidden)
                .andExpect(content().string("리뷰 사진 저장 권한이 없습니다"))
    }

    @Test
    @DisplayName("정상 리뷰 단건 조회")
    fun 정상리뷰단건조회() {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER)

        val reviewId = 1L
        val review = reviewService.getReview(reviewId)
        val images = imageService.findImagesById(ImageType.REVIEW, reviewId)

        val resultActions: ResultActions = mvc.perform(
                get("/api/reviews/{reviewId}", reviewId)
        )

        resultActions
                .andExpect(handler().handlerType(ReviewController::class.java))
            .andExpect(handler().methodName("getReview"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.reviewDto.reviewId").value(review.id))
                .andExpect(jsonPath("$.data.reviewDto.rating").value(review.rating))
                .andExpect(jsonPath("$.data.reviewDto.content").value(review.content))
                .andExpect(jsonPath("$.data.imageUrls.size()").value(images.size))
    }

    @Test
    @DisplayName("비정상 리뷰 단건 조회 - 인증 X")
    fun 비정상리뷰단건조회() {
        val reviewId = 1L

        val resultActions: ResultActions = mvc.perform(
                get("/api/reviews/{reviewId}", reviewId)
        )

        resultActions
                .andExpect(handler().handlerType(ReviewController::class.java))
            .andExpect(handler().methodName("getReview"))
                .andExpect(status().isUnauthorized)
                .andExpect(content().string("로그인이 필요합니다."))
    }

    @Test
    @DisplayName("비정상 리뷰 단건 조회 - 권한 X")
    fun 비정상리뷰단건조회2() {
        setUpAuthentication(2L, "customer2", "customer2@hotel.com", Role.USER)

        val reviewId = 1L

        val resultActions: ResultActions = mvc.perform(
                get("/api/reviews/{reviewId}", reviewId)
        )

        resultActions
                .andExpect(handler().handlerType(ReviewController::class.java))
            .andExpect(handler().methodName("getReview"))
                .andExpect(status().isForbidden)
                .andExpect(content().string("리뷰 조회 권한이 없습니다"))
    }

    @Test
    @DisplayName("비정상 리뷰 단건 조회 - 리뷰 존재 X")
    fun 비정상리뷰단건조회3() {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER)

        val reviewId = 10000L

        val resultActions: ResultActions = mvc.perform(
                get("/api/reviews/{reviewId}", reviewId)
        )

        resultActions
                .andExpect(handler().handlerType(ReviewController::class.java))
            .andExpect(handler().methodName("getReview"))
                .andExpect(status().isNotFound)
                .andExpect(content().string("리뷰가 존재하지 않습니다"))
    }

    @Test
    @DisplayName("정상 리뷰 수정")
    fun 정상리뷰수정() {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER)

        val reviewId = 1L

        val beforeImages = imageService.findImagesById(ImageType.REVIEW, reviewId)

        val updateReviewRequest = UpdateReviewRequest(
                "리뷰 1 내용 수정", 3,
                listOf(
                        "https://test-bucket.s3.amazonaws.com/reviews/2/1.jpg",
                        "https://test-bucket.s3.amazonaws.com/reviews/2/2.jpg"
                ),
                listOf("jpg")
        )

        val resultActions: ResultActions = mvc.perform(
                put("/api/reviews/{reviewId}", reviewId)
                        .content(objectMapper.writeValueAsString(updateReviewRequest))
                        .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        val review = reviewService.getReview(reviewId)
        val afterImages = imageService.findImagesById(ImageType.REVIEW, reviewId)

        resultActions
                .andExpect(handler().handlerType(ReviewController::class.java))
            .andExpect(handler().methodName("updateReview"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.presignedUrls.size()").value(updateReviewRequest.newImageExtensions.size))

        assertThat(updateReviewRequest.content).isEqualTo(review.content)
        assertThat(updateReviewRequest.rating).isEqualTo(review.rating)
        assertThat(afterImages).hasSize(beforeImages.size - updateReviewRequest.deleteImageUrls.size)
    }

    @Test
    @DisplayName("정상 리뷰 수정 - 인증 X")
    fun 비정상리뷰수정() {
        val reviewId = 1L

        val updateReviewRequest = UpdateReviewRequest(
                "리뷰 1 내용 수정", 3,
                listOf(
                        "https://test-bucket.s3.amazonaws.com/reviews/2/1.jpg",
                        "https://test-bucket.s3.amazonaws.com/reviews/2/2.jpg"
                ),
                listOf("jpg")
        )

        val resultActions: ResultActions = mvc.perform(
                put("/api/reviews/{reviewId}", reviewId)
                        .content(objectMapper.writeValueAsString(updateReviewRequest))
                        .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions
                .andExpect(handler().handlerType(ReviewController::class.java))
            .andExpect(handler().methodName("updateReview"))
                .andExpect(status().isUnauthorized)
                .andExpect(content().string("로그인이 필요합니다."))
    }

    @Test
    @DisplayName("정상 리뷰 수정 - 권한 X")
    fun 비정상리뷰수정2() {
        setUpAuthentication(2L, "customer2", "customer2@hotel.com", Role.USER)

        val reviewId = 1L

        val updateReviewRequest = UpdateReviewRequest(
                "리뷰 1 내용 수정", 3,
                listOf(
                        "https://test-bucket.s3.amazonaws.com/reviews/2/1.jpg",
                        "https://test-bucket.s3.amazonaws.com/reviews/2/2.jpg"
                ),
                listOf("jpg")
        )

        val resultActions: ResultActions = mvc.perform(
                put("/api/reviews/{reviewId}", reviewId)
                        .content(objectMapper.writeValueAsString(updateReviewRequest))
                        .contentType(MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andDo(print())

        resultActions
                .andExpect(handler().handlerType(ReviewController::class.java))
            .andExpect(handler().methodName("updateReview"))
                .andExpect(status().isForbidden)
                .andExpect(content().string("리뷰 수정 권한이 없습니다"))
    }

    @Test
    @DisplayName("정상 리뷰 삭제")
    fun 정상리뷰삭제() {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER)

        val reviewId = 1L

        val resultActions: ResultActions = mvc.perform(
                delete("/api/reviews/{reviewId}", reviewId)
        )

        assertFailsWith<ServiceException> { reviewService.getReview(reviewId) }

        resultActions
                .andExpect(handler().handlerType(ReviewController::class.java))
            .andExpect(handler().methodName("deleteReview"))
                .andExpect(status().isNoContent)
    }

    @Test
    @DisplayName("비정상 리뷰 삭제 - 인증 X")
    fun 비정상리뷰삭제() {
        val reviewId = 1L

        val resultActions: ResultActions = mvc.perform(
                delete("/api/reviews/{reviewId}", reviewId)
        )

        resultActions
                .andExpect(handler().handlerType(ReviewController::class.java))
            .andExpect(handler().methodName("deleteReview"))
                .andExpect(status().isUnauthorized)
                .andExpect(content().string("로그인이 필요합니다."))
    }

    @Test
    @DisplayName("비정상 리뷰 삭제 - 권한 X")
    fun 비정상리뷰삭제2() {
        setUpAuthentication(2L, "customer2", "customer2@hotel.com", Role.USER)

        val reviewId = 1L

        val resultActions: ResultActions = mvc.perform(
                delete("/api/reviews/{reviewId}", reviewId)
        )

        resultActions
                .andExpect(handler().handlerType(ReviewController::class.java))
            .andExpect(handler().methodName("deleteReview"))
                .andExpect(status().isForbidden)
                .andExpect(content().string("리뷰 삭제 권한이 없습니다"))
    }

    @Test
    @DisplayName("비정상 리뷰 삭제 - 리뷰 존재 X")
    fun 비정상리뷰삭제3() {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER)

        val reviewId = 10000L

        val resultActions: ResultActions = mvc.perform(
                delete("/api/reviews/{reviewId}", reviewId)
        )

        resultActions
                .andExpect(handler().handlerType(ReviewController::class.java))
            .andExpect(handler().methodName("deleteReview"))
                .andExpect(status().isNotFound)
                .andExpect(content().string("리뷰가 존재하지 않습니다"))
    }

    @Test
    @DisplayName("정상 내 리뷰 목록 조회")
    fun 정상_내_리뷰_목록_조회() {
        setUpAuthentication(1L, "customer1", "customer1@hotel.com", Role.USER)

        val pageNum = 1

        val resultActions: ResultActions = mvc.perform(
                get("/api/reviews/me?page={pageNum}", pageNum)
        )

        val reviews = reviewRepository.findByMemberId(1L)

        resultActions
                .andExpect(handler().handlerType(ReviewController::class.java))
            .andExpect(handler().methodName("getMyReviews"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.currentPageNumber").value(pageNum))
                .andExpect(jsonPath("$.data.items.size()").value(reviews.size))
                .andExpect(jsonPath("$.data.items[0].myReviewWithCommentDto.hotelName").value("강남호텔"))
                .andExpect(jsonPath("$.data.items[0].myReviewWithCommentDto.roomTypeName").value("스탠다드룸"))
                .andExpect(jsonPath("$.data.items[0].myReviewWithCommentDto.reviewDto.reviewId").value(reviews[0].id))
                .andExpect(jsonPath("$.data.items[1].myReviewWithCommentDto.hotelName").value("강남호텔"))
                .andExpect(jsonPath("$.data.items[1].myReviewWithCommentDto.roomTypeName").value("스탠다드룸"))
                .andExpect(jsonPath("$.data.items[1].myReviewWithCommentDto.reviewDto.reviewId").value(reviews[1].id))
    }

    @Test
    @DisplayName("비정상 내 리뷰 목록 조회 - 인증 X")
    fun 비정상_내_리뷰_목록_조회() {
        val pageNum = 1

        val resultActions: ResultActions = mvc.perform(
                get("/api/reviews/me?page={pageNum}", pageNum)
        )

        resultActions
                .andExpect(handler().handlerType(ReviewController::class.java))
            .andExpect(handler().methodName("getMyReviews"))
                .andExpect(status().isUnauthorized)
                .andExpect(content().string("로그인이 필요합니다."))
    }

    @Test
    @DisplayName("비정상 내 리뷰 목록 조회 - 권한 X")
    fun 비정상_내_리뷰_목록_조회2() {
        setUpAuthentication(1L, "business1", "business1@hotel.com", Role.BUSINESS)

        val pageNum = 1

        val resultActions: ResultActions = mvc.perform(
                get("/api/reviews/me?page={pageNum}", pageNum)
        )

        resultActions
                .andExpect(handler().handlerType(ReviewController::class.java))
            .andExpect(handler().methodName("getMyReviews"))
                .andExpect(status().isForbidden)
                .andExpect(content().string("내 리뷰 목록 조회는 손님만 가능합니다"))
    }

    @Test
    @DisplayName("정상 호텔 리뷰 목록 조회")
    fun 정상_호텔_리뷰_목록_조회() {
        val hotelId = 1L
        val pageNum = 1

        val resultActions: ResultActions = mvc.perform(
                get("/api/reviews/hotels/{hotelId}?page={pageNum}", hotelId, pageNum)
        )

        val reviews = reviewRepository.findByHotelId(hotelId)

        resultActions
                .andExpect(handler().handlerType(ReviewController::class.java))
            .andExpect(handler().methodName("getHotelReviews"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.hotelReviewPage.currentPageNumber").value(pageNum))
                .andExpect(jsonPath("$.data.hotelReviewPage.items.size()").value(reviews.size))
                .andExpect(jsonPath("$.data.hotelReviewPage.items[0].hotelReviewWithCommentDto.roomTypeName").value("스탠다드룸"))
                .andExpect(jsonPath("$.data.hotelReviewPage.items[0].hotelReviewWithCommentDto.reviewDto.reviewId").value(reviews[0].id))
                .andExpect(jsonPath("$.data.hotelReviewPage.items[1].hotelReviewWithCommentDto.roomTypeName").value("스탠다드룸"))
                .andExpect(jsonPath("$.data.hotelReviewPage.items[1].hotelReviewWithCommentDto.reviewDto.reviewId").value(reviews[1].id))
                .andExpect(jsonPath("$.data.averageRating").value(reviews.first().hotel.averageRating))
    }

    @Test
    @DisplayName("비정상 호텔 리뷰 목록 조회 - 호텔 X")
    fun 비정상_호텔_리뷰_목록_조회() {
        val hotelId = 100L
        val pageNum = 1

        val resultActions: ResultActions = mvc.perform(
                get("/api/reviews/hotels/{hotelId}?page={pageNum}", hotelId, pageNum)
        )

        resultActions
                .andExpect(handler().handlerType(ReviewController::class.java))
            .andExpect(handler().methodName("getHotelReviews"))
                .andExpect(status().isNotFound)
    }
}