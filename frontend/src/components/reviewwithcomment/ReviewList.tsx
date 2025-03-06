import React from "react";
import MyReviewWithComment from "./MyReviewWithComment";
import HotelReviewWithComment from "./HotelReviewWithComment";
import { ReviewDto } from "@/lib/types/review/ReviewDto";
import { HotelReviewResponse } from "@/lib/types/review/HotelReviewResponse";
import { MyReviewResponse } from "@/lib/types/review/MyReviewResponse";

export type ReviewResponseType = HotelReviewResponse | MyReviewResponse;

// Type guard to check if the review is HotelReviewResponse
export const isHotelReview = (
  review: ReviewResponseType
): review is HotelReviewResponse => {
  return "hotelReviewWithCommentDto" in review;
};

interface ReviewListProps {
  reviews: ReviewResponseType[];
  isBusinessUser?: boolean;
  onCommentUpdate?: () => void;
  onReviewDelete?: () => void;
}

const ReviewList: React.FC<ReviewListProps> = ({
  reviews,
  isBusinessUser = false,
  onCommentUpdate,
  onReviewDelete,
}) => {
  console.log(reviews);
  if (reviews.length === 0) {
    return (
      <div className="max-w-3xl mx-auto p-4 text-center text-gray-500">
        작성된 리뷰가 없습니다.
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto p-4">
      {reviews.map((review) => {
        let reviewDto: ReviewDto;
        if (isHotelReview(review)) {
          reviewDto = review.hotelReviewWithCommentDto.reviewDto;
          return (
            <HotelReviewWithComment
              key={reviewDto.reviewId}
              review={review as HotelReviewResponse}
              isBusinessUser={isBusinessUser}
              onCommentUpdate={onCommentUpdate}
            />
          );
        } else {
          reviewDto = review.myReviewWithCommentDto.reviewDto;
          return (
            <MyReviewWithComment
              key={reviewDto.reviewId}
              review={review as MyReviewResponse}
              onReviewDelete={onReviewDelete}
            />
          );
        }
      })}
    </div>
  );
};

export default ReviewList;
