export interface UpdateReviewRequest {
    content: string;
    rating: number;
    deleteImageUrls: string[];
    newImageExtensions: string[];
}