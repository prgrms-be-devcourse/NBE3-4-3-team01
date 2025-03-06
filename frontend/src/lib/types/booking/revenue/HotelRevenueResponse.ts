import { RoomRevenueResponse } from "./RoomRevenueResponse";

export interface HotelRevenueResponse {
  roomRevenueResponse: Array<RoomRevenueResponse>;
  revenue: number;
}
