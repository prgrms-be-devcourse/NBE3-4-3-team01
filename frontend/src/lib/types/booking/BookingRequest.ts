export interface BookingRequest {
  roomId: number;
  hotelId: number;
  checkInDate: string;
  checkOutDate: string;

  // KakaoPaymentRequest 생성에 필요
  merchantUid: string;
  amount: number;
  paidAtTimestamp: number;
}
