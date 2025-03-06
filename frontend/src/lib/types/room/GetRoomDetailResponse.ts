import { RoomDto } from "./RoomDto";

export interface GetRoomDetailResponse {
  roomDto: RoomDto;
  roomImageUrls: string[];
}
