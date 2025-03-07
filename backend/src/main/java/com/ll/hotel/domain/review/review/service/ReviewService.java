package com.ll.hotel.domain.review.review.service;

import com.ll.hotel.domain.booking.booking.entity.Booking;
import com.ll.hotel.domain.hotel.hotel.entity.Hotel;
import com.ll.hotel.domain.hotel.hotel.service.HotelService;
import com.ll.hotel.domain.hotel.room.entity.Room;
import com.ll.hotel.domain.image.dto.ImageDto;
import com.ll.hotel.domain.image.entity.Image;
import com.ll.hotel.domain.image.repository.ImageRepository;
import com.ll.hotel.domain.image.service.ImageService;
import com.ll.hotel.domain.image.type.ImageType;
import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.domain.review.review.dto.ReviewDto;
import com.ll.hotel.domain.review.review.dto.request.PostReviewRequest;
import com.ll.hotel.domain.review.review.dto.request.UpdateReviewRequest;
import com.ll.hotel.domain.review.review.dto.response.*;
import com.ll.hotel.domain.review.review.entity.Review;
import com.ll.hotel.domain.review.review.repository.ReviewRepository;
import com.ll.hotel.global.app.AppConfig;
import com.ll.hotel.global.aws.s3.S3Service;
import com.ll.hotel.global.exceptions.ErrorCode;
import com.ll.hotel.standard.page.dto.PageDto;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final EntityManager entityManager;
    private final ReviewRepository reviewRepository;
    private final ImageRepository imageRepository;
    private final HotelService hotelService;
    private final ImageService imageService;
    private final S3Service s3Service;
    private final AppConfig appConfig;

    public PresignedUrlsResponse createReviewAndPresignedUrls(Long hotelId, Long roomId, Long memberId, Long bookingId,
                                                              PostReviewRequest postReviewRequest) {
        long reviewId = createReview(hotelId, roomId, memberId, bookingId, postReviewRequest.getContent(), postReviewRequest.getRating());

        List<String> extensions = Optional
                .of(postReviewRequest.getImageExtensions())
                .orElse(Collections.emptyList());

        List<URL> urls = s3Service.generatePresignedUrls(ImageType.REVIEW, reviewId, extensions);

        return new PresignedUrlsResponse(reviewId, urls);
    }

    public void saveReviewImages(Member actor, long reviewId, List<String> urls) {
        // 권한 체크 (리뷰 작성자인가?)
        if (!getReview(reviewId).isWrittenBy(actor)) {
            ErrorCode.REVIEW_IMAGE_REGISTRATION_FORBIDDEN.throwServiceException();
        }

        imageService.saveImages(ImageType.REVIEW, reviewId, urls);
    }

    public PresignedUrlsResponse updateReview(Member actor, long reviewId, UpdateReviewRequest updateReviewRequest) {

        updateReviewContentAndRating(actor, reviewId, updateReviewRequest.getContent(), updateReviewRequest.getRating());

        List<String> deleteImageUrls = Optional.of(updateReviewRequest.getDeleteImageUrls())
                .orElse(Collections.emptyList());

        // DB 사진 삭제
        imageService.deleteImagesByIdAndUrls(ImageType.REVIEW, reviewId, deleteImageUrls);
        // S3 사진 삭제
        if(!appConfig.getMode().equals("TEST")) {
            s3Service.deleteObjectsByUrls(deleteImageUrls);
        }

        List<String> extensions = Optional.of(updateReviewRequest.getNewImageExtensions())
                .orElse(Collections.emptyList());

        // 새로운 사진의 Presigned URL 반환
        List<URL> urls = s3Service.generatePresignedUrls(ImageType.REVIEW, reviewId, extensions);

        return new PresignedUrlsResponse(reviewId, urls);
    }

    public void deleteReviewWithImages(Member actor, long reviewId) {
        // 리뷰 삭제 (+권한 체크)
        deleteReview(actor, reviewId);
        // DB 의 사진 URL 정보 삭제
        long imageCount = imageService.deleteImages(ImageType.REVIEW, reviewId);
        // 테스트 모드가 아니면 S3 의 사진 삭제
        if(imageCount > 0 && !appConfig.getMode().equals("TEST")) {
            s3Service.deleteAllObjectsById(ImageType.REVIEW, reviewId);
        }
    }

    // 리뷰 생성
    public long createReview(Long hotelId, Long roomId, Long memberId, Long bookingId, String content, int rating) {
        Hotel hotel = hotelService.getHotelById(hotelId);
        Member member = entityManager.getReference(Member.class, memberId);
        Room room = entityManager.getReference(Room.class, roomId);
        Booking booking = entityManager.getReference(Booking.class, bookingId);

        if(!booking.isReservedBy(member)) {
            ErrorCode.REVIEW_CREATION_FORBIDDEN.throwServiceException();
        }

        // 호텔 평균 리뷰 수정
        updateRatingOnReviewCreated(hotel, rating);

        Review review = new Review(hotel, room, booking, member, content, rating);

        Review savedReview = reviewRepository.save(review);
        return savedReview.getId();
    }

    // 리뷰의 content, rating 수정
    public void updateReviewContentAndRating(Member actor, long reviewId, String content, int rating){
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ErrorCode.REVIEW_NOT_FOUND::throwServiceException);

        if(!review.isWrittenBy(actor)) {
            ErrorCode.REVIEW_UPDATE_FORBIDDEN.throwServiceException();
        }

        // 호텔 평균 리뷰 수정
        updateRatingOnReviewModified(review.getHotel(), review.getRating(), rating);

        review.setContent(content);
        review.setRating(rating);
    }

    // 리뷰 삭제
    public void deleteReview(Member actor, long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ErrorCode.REVIEW_NOT_FOUND::throwServiceException);

        if(!review.isWrittenBy(actor)) {
            ErrorCode.REVIEW_DELETE_FORBIDDEN.throwServiceException();
        }

        // 호텔 평균 리뷰 수정
        updateRatingOnReviewDeleted(review.getHotel(), review.getRating());

        reviewRepository.delete(review);
    }

    // 리뷰 단건 조회
    public GetReviewResponse getReviewResponse(Member actor, long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(ErrorCode.REVIEW_NOT_FOUND::throwServiceException);

        if(!review.isWrittenBy(actor)) {
            ErrorCode.REVIEW_ACCESS_FORBIDDEN.throwServiceException();
        }

        List<String> imageUrls = imageRepository.findByImageTypeAndReferenceId(ImageType.REVIEW, reviewId)
                .stream()
                .map(Image::getImageUrl)
                .toList();

        return new GetReviewResponse(new ReviewDto(review), imageUrls);
    }

    // 현재 접속한 유저가 작성한 모든 리뷰 조회 (답변, 이미지 포함)
    public Page<MyReviewResponse> getMyReviewResponses(Member actor, int page) {

        if (!actor.isUser()) {
            ErrorCode.USER_REVIEW_ACCESS_FORBIDDEN.throwServiceException();
        }

        int size = 10;
        Pageable pageable = PageRequest.of(page-1, size, Sort.by("createdAt").descending());
        Page<MyReviewWithCommentDto> myReviews = reviewRepository.findReviewsWithCommentByMemberId(actor.getId(), pageable);

        return getReviewsWithImages(
                myReviews,
                myReview -> myReview.getReviewDto().getReviewId(),
                MyReviewResponse::new,
                pageable
        );
    }

    // 호텔의 모든 리뷰 조회 (답변, 이미지 포함)
    public HotelReviewListResponse getHotelReviewListResponse(long hotelId, int page) {
        Hotel hotel = hotelService.getHotelById(hotelId);

        int size = 10;
        Pageable pageable = PageRequest.of(page-1, size, Sort.by("createdAt").descending());
        Page<HotelReviewWithCommentDto> hotelReviews = reviewRepository.findReviewsWithCommentByHotelId(hotelId, pageable);

        Page<HotelReviewResponse> hotelReviewPage = getReviewsWithImages(
                hotelReviews,
                hotelReview -> hotelReview.getReviewDto().getReviewId(),
                HotelReviewResponse::new,
                pageable
        );

        return new HotelReviewListResponse(
                new PageDto<>(hotelReviewPage),
                hotel.getAverageRating());
    }

    public Review getReview(long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(ErrorCode.REVIEW_NOT_FOUND::throwServiceException);
    }

    // 리뷰 생성의 평균 리뷰 수정
    private void updateRatingOnReviewCreated(Hotel hotel, int rating) {
        hotel.updateAverageRating(1, rating);
    }

    // 리뷰 수정의 평균 리뷰 수정
    private void updateRatingOnReviewModified(Hotel hotel, int beforeRating, int afterRating) {
        hotel.updateAverageRating(0, afterRating - beforeRating);
    }

    // 리뷰 삭제의 평균 리뷰 수정
    private void updateRatingOnReviewDeleted(Hotel hotel, int rating) {
        hotel.updateAverageRating(-1, -rating);
    }

    // 리뷰 목록을 받아 (리뷰 + 사진) 목록으로 반환
    private <T, R> Page<R> getReviewsWithImages(Page<T> reviews, Function<T, Long> getReviewId, BiFunction<T, List<String>, R> mapToResponse, Pageable pageable) {
        // 리뷰 아이디 추출
        List<Long> reviewIds = reviews.getContent().stream()
                .map(getReviewId)
                .toList();

        // 이미지 URL 매핑
        Map<Long, List<String>> reviewImageUrls = imageRepository.findImageUrlsByReferenceIdsAndImageType(reviewIds, ImageType.REVIEW, pageable)
                .getContent()
                .stream()
                .collect(Collectors.groupingBy(
                        ImageDto::referenceId,
                        Collectors.mapping(ImageDto::imageUrl, Collectors.toList())));

        // 응답 객체 생성
        List<R> responseList = reviews.getContent().stream()
                .map(review -> mapToResponse.apply(review, reviewImageUrls.getOrDefault(getReviewId.apply(review), List.of())))
                .toList();

        return new PageImpl<>(responseList, pageable, reviews.getTotalElements());
    }

}
