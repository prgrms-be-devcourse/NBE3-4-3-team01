package com.ll.hotel.domain.review.review.controller;

import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.domain.review.review.dto.request.PostReviewRequest;
import com.ll.hotel.domain.review.review.dto.request.UpdateReviewRequest
import com.ll.hotel.domain.review.review.dto.response.GetReviewResponse
import com.ll.hotel.domain.review.review.dto.response.HotelReviewListResponse;
import com.ll.hotel.domain.review.review.dto.response.MyReviewResponse;
import com.ll.hotel.domain.review.review.dto.response.PresignedUrlsResponse;
import com.ll.hotel.domain.review.review.service.ReviewService;
import com.ll.hotel.global.app.AppConfig;
import com.ll.hotel.global.request.Rq;
import com.ll.hotel.global.response.RsData;
import com.ll.hotel.standard.page.dto.PageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/reviews")
@Tag(name = "ReviewController")
class ReviewController(
    private val reviewService: ReviewService,
    private val rq: Rq,
    private val appConfig: AppConfig
) {
    @PostMapping("/{bookingId}")
    @Operation(summary = "리뷰 생성")
    fun createReview(
        @PathVariable("bookingId") bookingId: Long,
        @RequestParam("hotelId") hotelId: Long,
        @RequestParam("roomId") roomId: Long,
        @RequestBody @Valid postReviewRequest: PostReviewRequest
    ): RsData<PresignedUrlsResponse> {
        val actor: Member = rq.getActor();

        val presignedUrlsResponse: PresignedUrlsResponse = reviewService.createReviewAndPresignedUrls(
            hotelId, roomId, actor.id, bookingId, postReviewRequest
        );

        return RsData.success(HttpStatus.CREATED, presignedUrlsResponse);
    }

    @PostMapping("/{reviewId}/urls")
    @Operation(summary = "사진 URL 리스트 저장")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun saveImageUrls(
        @PathVariable("reviewId") reviewId: Long,
        @RequestBody urls: List<String>
    ) {
        val actor: Member = rq.getActor();

        reviewService.saveReviewImages(actor, reviewId, urls);
    }

    @PutMapping("/{reviewId}")
    @Operation(summary = "리뷰 수정")
    fun updateReview(
        @PathVariable("reviewId") reviewId: Long,
        @RequestBody @Valid updateReviewRequest: UpdateReviewRequest
    ): RsData<PresignedUrlsResponse> {
        val actor: Member = rq.getActor();

        val presignedUrlsResponse: PresignedUrlsResponse =
            reviewService.updateReview(actor, reviewId, updateReviewRequest);

        return RsData.success(HttpStatus.OK, presignedUrlsResponse);
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "리뷰 삭제")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteReview(
        @PathVariable("reviewId") reviewId: Long
    ) {
        val actor: Member = rq.getActor();

        reviewService.deleteReviewWithImages(actor, reviewId);
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "리뷰 수정하기 전 기존 정보 제공")
    fun getReview(
        @PathVariable("reviewId") reviewId: Long
    ): RsData<GetReviewResponse> {
        val actor: Member = rq.getActor();

        return RsData.success(HttpStatus.OK, reviewService.getReviewResponse(actor, reviewId));
    }

    @GetMapping("/me")
    @Operation(summary = "내 리뷰 목록 조회")
    fun getMyReviews(
        @RequestParam(value = "page", defaultValue = "1") page: Int
    ): RsData<PageDto<MyReviewResponse>> {
        val actor: Member = rq.getActor();

        val myReviewPage: Page<MyReviewResponse> = reviewService.getMyReviewResponses(actor, page);

        return RsData.success(HttpStatus.OK, PageDto<MyReviewResponse>(myReviewPage));
    }

    @GetMapping("/hotels/{hotelId}")
    @Operation(summary = "호텔 리뷰 목록 조회")
    fun getHotelReviews(
        @RequestParam(value = "page", defaultValue = "1") page: Int,
        @PathVariable("hotelId") hotelId: Long
    ): RsData<HotelReviewListResponse> {
        val hotelReviewListResponse: HotelReviewListResponse = reviewService.getHotelReviewListResponse(hotelId, page);

        return RsData.success(HttpStatus.OK, hotelReviewListResponse);
    }

}
