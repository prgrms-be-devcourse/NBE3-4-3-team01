import { MyReviewWithCommentDto } from './MyReviewWithCommentDto';

export interface MyReviewResponse {
    myReviewWithCommentDto: MyReviewWithCommentDto;
    imageUrls: string[];
}