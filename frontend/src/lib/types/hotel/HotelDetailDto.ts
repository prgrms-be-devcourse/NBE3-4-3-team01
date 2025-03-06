import { GetRoomResponse } from "@/lib/types/room/GetRoomResponse";

export interface HotelDetailDto {
  hotelId: number;
  hotelName: string;
  hotelEmail: string;
  hotelPhoneNumber: string;
  streetAddress: string;
  zipCode: number;
  hotelGrade: number;
  checkInTime: string;
  checkOutTime: string;
  hotelExplainContent: string;
  hotelStatus: string;
  rooms: GetRoomResponse[];
  hotelOptions: Set<string>;
}
