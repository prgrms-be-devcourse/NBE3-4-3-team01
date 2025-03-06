import { ReviewCommentDto } from "./ReviewCommentDto";
import { ReviewDto } from "./ReviewDto";

export interface MyReviewWithCommentDto {
    hotelName: string;
    roomTypeName: string;
    reviewDto: ReviewDto;
    reviewCommentDto: ReviewCommentDto;
    createdAt: string;
}