import { ReviewDto } from "./ReviewDto";

export interface GetReviewResponse {
    reviewDto: ReviewDto;
    imageUrls: string[];
}