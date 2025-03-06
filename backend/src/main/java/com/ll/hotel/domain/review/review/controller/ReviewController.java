package com.ll.hotel.domain.review.review.controller;

import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.domain.review.review.dto.request.PostReviewRequest;
import com.ll.hotel.domain.review.review.dto.request.UpdateReviewRequest;
import com.ll.hotel.domain.review.review.dto.response.GetReviewResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "ReviewController")
public class ReviewController {
    private final ReviewService reviewService;
    private final Rq rq;
    private final AppConfig appConfig;

    @PostMapping("/{bookingId}")
    @Operation(summary = "리뷰 생성")
    public RsData<PresignedUrlsResponse> createReview(
            @PathVariable("bookingId") Long bookingId,
            @RequestParam("hotelId") Long hotelId,
            @RequestParam("roomId") Long roomId,
            @RequestBody @Valid PostReviewRequest postReviewRequest) {
        Member actor = rq.getActor();

        PresignedUrlsResponse presignedUrlsResponse = reviewService.createReviewAndPresignedUrls(
                hotelId, roomId, actor.getId(), bookingId, postReviewRequest);

        return RsData.success(HttpStatus.CREATED, presignedUrlsResponse);
    }

    @PostMapping("/{reviewId}/urls")
    @Operation(summary = "사진 URL 리스트 저장")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveImageUrls(
            @PathVariable("reviewId") long reviewId,
            @RequestBody List<String> urls) {
        Member actor = rq.getActor();

        reviewService.saveReviewImages(actor, reviewId, urls);
    }

    @PutMapping("/{reviewId}")
    @Operation(summary = "리뷰 수정")
    public RsData<PresignedUrlsResponse> updateReview(
            @PathVariable("reviewId") long reviewId,
            @RequestBody @Valid UpdateReviewRequest updateReviewRequest
    ) {
        Member actor = rq.getActor();

        PresignedUrlsResponse presignedUrlsResponse = reviewService.updateReview(actor, reviewId, updateReviewRequest);

        return RsData.success(HttpStatus.OK, presignedUrlsResponse);
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "리뷰 삭제")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(
            @PathVariable("reviewId") long reviewId
    ) {
        Member actor = rq.getActor();

        reviewService.deleteReviewWithImages(actor, reviewId);
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "리뷰 수정하기 전 기존 정보 제공")
    public RsData<GetReviewResponse> getReview(
            @PathVariable("reviewId") long reviewId
    ) {
        Member actor = rq.getActor();

        return RsData.success(HttpStatus.OK,reviewService.getReviewResponse(actor, reviewId));
    }

    @GetMapping("/me")
    @Operation(summary = "내 리뷰 목록 조회")
    public RsData<PageDto<MyReviewResponse>> getMyReviews(
            @RequestParam(value = "page", defaultValue = "1") int page
    ) {
        Member actor = rq.getActor();

        Page<MyReviewResponse> myReviewPage = reviewService.getMyReviewResponses(actor, page);

        return RsData.success(HttpStatus.OK, new PageDto<>(myReviewPage));
    }

    @GetMapping("/hotels/{hotelId}")
    @Operation(summary = "호텔 리뷰 목록 조회")
    public RsData<HotelReviewListResponse> getHotelReviews(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @PathVariable("hotelId") long hotelId
    ) {
        HotelReviewListResponse hotelReviewListResponse = reviewService.getHotelReviewListResponse(hotelId, page);

        return RsData.success(HttpStatus.OK, hotelReviewListResponse);
    }

}
