package com.ll.hotel.domain.review.review.service

import com.ll.hotel.domain.booking.booking.entity.Booking
import com.ll.hotel.domain.hotel.hotel.entity.Hotel
import com.ll.hotel.domain.hotel.hotel.service.HotelService
import com.ll.hotel.domain.hotel.room.entity.Room
import com.ll.hotel.domain.image.dto.ImageDto
import com.ll.hotel.domain.image.repository.ImageRepository
import com.ll.hotel.domain.image.service.ImageService
import com.ll.hotel.domain.image.type.ImageType
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.review.review.dto.ReviewDto
import com.ll.hotel.domain.review.review.dto.request.PostReviewRequest
import com.ll.hotel.domain.review.review.dto.request.UpdateReviewRequest
import com.ll.hotel.domain.review.review.dto.response.*
import com.ll.hotel.domain.review.review.entity.Review
import com.ll.hotel.domain.review.review.repository.ReviewRepository
import com.ll.hotel.global.app.AppConfig
import com.ll.hotel.global.aws.s3.S3Service
import com.ll.hotel.global.exceptions.ErrorCode
import com.ll.hotel.standard.page.dto.PageDto
import jakarta.persistence.EntityManager
import org.springframework.data.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ReviewService(
    private val entityManager: EntityManager,
    private val reviewRepository: ReviewRepository,
    private val imageRepository: ImageRepository,
    private val hotelService: HotelService,
    private val imageService: ImageService,
    private val s3Service: S3Service,
    private val appConfig: AppConfig
) {

    fun createReviewAndPresignedUrls(
        hotelId: Long,
        roomId: Long,
        memberId: Long,
        bookingId: Long,
        postReviewRequest: PostReviewRequest
    ): PresignedUrlsResponse {
        val reviewId = createReview(
            hotelId, roomId, memberId, bookingId,
            postReviewRequest.content, postReviewRequest.rating
        )

        val extensions = postReviewRequest.imageExtensions ?: emptyList()
        val urls = s3Service.generatePresignedUrls(ImageType.REVIEW, reviewId, extensions)

        return PresignedUrlsResponse(reviewId, urls)
    }

    fun saveReviewImages(actor: Member, reviewId: Long, urls: List<String>) {
        if (!getReview(reviewId).isWrittenBy(actor)) {
            ErrorCode.REVIEW_IMAGE_REGISTRATION_FORBIDDEN.throwServiceException()
        }

        imageService.saveImages(ImageType.REVIEW, reviewId, urls)
    }

    fun updateReview(actor: Member, reviewId: Long, updateReviewRequest: UpdateReviewRequest): PresignedUrlsResponse {
        updateReviewContentAndRating(actor, reviewId, updateReviewRequest.content, updateReviewRequest.rating)

        val deleteImageUrls = updateReviewRequest.deleteImageUrls
        imageService.deleteImagesByIdAndUrls(ImageType.REVIEW, reviewId, deleteImageUrls)
        if (appConfig.mode != "TEST") {
            s3Service.deleteObjectsByUrls(deleteImageUrls)
        }

        val extensions = updateReviewRequest.newImageExtensions
        val urls = s3Service.generatePresignedUrls(ImageType.REVIEW, reviewId, extensions)

        return PresignedUrlsResponse(reviewId, urls)
    }

    fun deleteReviewWithImages(actor: Member, reviewId: Long) {
        deleteReview(actor, reviewId)
        val imageCount = imageService.deleteImages(ImageType.REVIEW, reviewId)
        if (imageCount > 0 && appConfig.mode != "TEST") {
            s3Service.deleteAllObjectsById(ImageType.REVIEW, reviewId)
        }
    }

    fun createReview(hotelId: Long, roomId: Long, memberId: Long, bookingId: Long, content: String, rating: Int): Long {
        val hotel = hotelService.getHotelById(hotelId)
        val member = entityManager.getReference(Member::class.java, memberId)
        val room = entityManager.getReference(Room::class.java, roomId)
        val booking = entityManager.getReference(Booking::class.java, bookingId)

        if (!booking.isReservedBy(member)) {
            ErrorCode.REVIEW_CREATION_FORBIDDEN.throwServiceException()
        }

        updateRatingOnReviewCreated(hotel, rating)

        val review = Review(hotel, room, booking, member, content, rating)
        return reviewRepository.save(review).id
    }

    fun updateReviewContentAndRating(actor: Member, reviewId: Long, content: String, rating: Int) {
        val review: Review = getReview(reviewId)

        if (!review.isWrittenBy(actor)) {
            ErrorCode.REVIEW_UPDATE_FORBIDDEN.throwServiceException()
        }

        updateRatingOnReviewModified(review.hotel, review.rating, rating)

        review.content = content
        review.rating = rating
    }

    fun deleteReview(actor: Member, reviewId: Long) {
        val review: Review = getReview(reviewId)

        if (!review.isWrittenBy(actor)) {
            ErrorCode.REVIEW_DELETE_FORBIDDEN.throwServiceException()
        }

        updateRatingOnReviewDeleted(review.hotel, review.rating)

        reviewRepository.delete(review)
    }

    fun getReviewResponse(actor: Member, reviewId: Long): GetReviewResponse {
        val review: Review = getReview(reviewId)

        if (!review.isWrittenBy(actor)) {
            ErrorCode.REVIEW_ACCESS_FORBIDDEN.throwServiceException()
        }

        val imageUrls = imageRepository.findByImageTypeAndReferenceId(ImageType.REVIEW, reviewId)
            .map { it.imageUrl }

        return GetReviewResponse(ReviewDto(review), imageUrls)
    }

    fun getMyReviewResponses(actor: Member, page: Int): Page<MyReviewResponse> {
        if (!actor.isUser) {
            ErrorCode.USER_REVIEW_ACCESS_FORBIDDEN.throwServiceException()
        }

        val size = 10
        val pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending())
        val myReviews = reviewRepository.findReviewsWithCommentByMemberId(actor.id, pageable)

        return getReviewsWithImages(
            myReviews,
            { it.reviewDto.reviewId },
            ::MyReviewResponse,
            pageable
        )
    }

    fun getHotelReviewListResponse(hotelId: Long, page: Int): HotelReviewListResponse {
        val hotel = hotelService.getHotelById(hotelId)

        val size = 10
        val pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending())
        val hotelReviews = reviewRepository.findReviewsWithCommentByHotelId(hotelId, pageable)

        val hotelReviewPage = getReviewsWithImages(
            hotelReviews,
            { it.reviewDto.reviewId },
            ::HotelReviewResponse,
            pageable
        )

        return HotelReviewListResponse(PageDto(hotelReviewPage), hotel.averageRating)
    }

    fun getReview(reviewId: Long): Review {
        return reviewRepository.findById(reviewId)
            .orElseThrow(ErrorCode.REVIEW_NOT_FOUND::throwServiceException);
    }

    private fun updateRatingOnReviewCreated(hotel: Hotel, rating: Int) {
        hotel.updateAverageRating(1, rating)
    }

    private fun updateRatingOnReviewModified(hotel: Hotel, beforeRating: Int, afterRating: Int) {
        hotel.updateAverageRating(0, afterRating - beforeRating)
    }

    private fun updateRatingOnReviewDeleted(hotel: Hotel, rating: Int) {
        hotel.updateAverageRating(-1, -rating)
    }

    private inline fun <T, R> getReviewsWithImages(
        reviews: Page<T>,
        getReviewId: (T) -> Long,
        crossinline mapToResponse: (T, List<String>) -> R,
        pageable: Pageable
    ): Page<R> {
        val reviewIds = reviews.content.map(getReviewId)

        val reviewImageUrls =
            imageRepository.findImageUrlsByReferenceIdsAndImageType(reviewIds, ImageType.REVIEW, pageable)
                .content
                .groupBy(ImageDto::referenceId) { it.imageUrl }

        val responseList = reviews.content.map { review ->
            mapToResponse(review, reviewImageUrls[getReviewId(review)] ?: emptyList())
        }

        return PageImpl(responseList, pageable, reviews.totalElements)
    }
}