import { HotelDetailDto } from "./HotelDetailDto";

export interface GetHotelDetailResponse {
  hotelDetailDto: HotelDetailDto;
  hotelImageUrls: string[];
}
