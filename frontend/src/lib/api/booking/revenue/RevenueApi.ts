import { HotelRevenueResponse } from "@/lib/types/booking/revenue/HotelRevenueResponse";

// 예약 상세 조회
export const getHotelRevenue = async function (
  hotelId: number
): Promise<HotelRevenueResponse> {
  try {
    const response = await fetch(
      `http://localhost:8080/api/hotels/${hotelId}/revenue`,
      {
        credentials: "include",
      }
    );

    if (!response.ok) {
      const body = await response.text();
      throw new Error(body);
    }

    const rsData = await response.json();
    return rsData.data;
  } catch (error) {
    throw error;
  }
};
