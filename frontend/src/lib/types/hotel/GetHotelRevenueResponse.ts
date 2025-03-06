import { GetRoomRevenueResponse } from "@/lib/types/room/GetRoomRevenueResponse";

export interface GetHotelRevenueResponse {
  roomRevenueResponse: GetRoomRevenueResponse[];
  revenue: number;
}
