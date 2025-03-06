import { HotelReviewResponse } from "./HotelReviewResponse";
import { PageDto } from "../PageDto";

export interface HotelReviewListResponse {
    hotelReviewPage : PageDto<HotelReviewResponse>;
    averageRating : number;
}