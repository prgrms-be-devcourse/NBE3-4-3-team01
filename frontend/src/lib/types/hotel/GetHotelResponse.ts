export interface GetHotelResponse {
  // 호텔 파트 데이터 사용할 때 사용하세요
  hotelId: number;
  hotelName: string;
  hotelGrade: number;
  streetAddress: string;
  checkInTime: string;
  averageRating: number;
  totalReviewCount: number;
  thumbnailUrl: string;
  price: number;
}
