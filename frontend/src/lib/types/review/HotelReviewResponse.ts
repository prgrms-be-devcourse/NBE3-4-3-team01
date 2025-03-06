import { HotelReviewWithCommentDto } from "./HotelReviewWithCommentDto";

export interface HotelReviewResponse {
    hotelReviewWithCommentDto: HotelReviewWithCommentDto;
    imageUrls: string[];
}