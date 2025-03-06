import { HotelDetailDto } from "../hotel/HotelDetailDto";
import { RoomDto } from "../room/RoomDto";
import { BookingMemberDto } from "./BookingMemberDto";

export interface BookingFormResponse {
  hotel: HotelDetailDto;
  room: RoomDto;
  thumbnailUrls: string[];
  member: BookingMemberDto;
}
