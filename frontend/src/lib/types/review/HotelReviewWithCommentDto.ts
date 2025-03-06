import { ReviewCommentDto } from "./ReviewCommentDto";
import { ReviewDto } from "./ReviewDto";

export interface HotelReviewWithCommentDto {
    memberEmail: string;
    roomTypeName: string;
    reviewDto: ReviewDto;
    reviewCommentDto: ReviewCommentDto;
    createdAt: string;
}