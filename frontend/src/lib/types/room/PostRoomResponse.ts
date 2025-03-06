import { PresignedUrlsResponse } from "@/lib/types/review/PresignedUrlsResponse";

export interface PostRoomResponse {
  roomId: number;
  hotelId: number;
  roomName: string;
  basePrice: number;
  standardNumber: number;
  maxNumber: number;
  createdAt: string;
  urlsResponse: PresignedUrlsResponse;
}
