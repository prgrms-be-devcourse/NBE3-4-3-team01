import { PresignedUrlsResponse } from "../review/PresignedUrlsResponse";

export interface PutHotelResponse {
  businessId: number;
  hotelId: number;
  hotelName: string;
  hotelStatus: string;
  modifiedAt: string;
  urlsResponse: PresignedUrlsResponse;
}
