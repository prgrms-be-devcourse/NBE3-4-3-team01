import { PresignedUrlsResponse } from "../review/PresignedUrlsResponse";

export interface PostHotelResponse {
  businessId: number;
  hotelId: number;
  hotelName: string;
  createdAt: string;
  urlsResponse: PresignedUrlsResponse;
}
