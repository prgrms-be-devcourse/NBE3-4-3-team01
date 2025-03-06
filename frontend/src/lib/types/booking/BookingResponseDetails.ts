import { HotelDetailDto } from "../hotel/HotelDetailDto";
import { RoomDto } from "../room/RoomDto";
import { BookingMemberDto } from "./BookingMemberDto";
import { PaymentResponse } from "./payment/PaymentResponse";

export interface BookingResponseDetails {
  bookingId: number;
  hotel: HotelDetailDto;
  room: RoomDto;
  thumbnailUrls: string[];
  member: BookingMemberDto;
  payment: PaymentResponse;
  bookNumber: string;
  bookingStatus: string;
  createdAt: string;
  checkInDate: string;
  checkOutDate: string;
}
