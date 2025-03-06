import { PresignedUrlsResponse } from "@/lib/types/review/PresignedUrlsResponse";

export interface PutRoomResponse {
  hotelId: number;
  roomId: number;
  roomName: string;
  roomStatus: string;
  modifiedAt: string;
  urlResponse: PresignedUrlsResponse;
}
