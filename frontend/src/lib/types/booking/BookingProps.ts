import { KakaoPaymentRequest } from "./payment/KakaoPaymentRequest";

export const enum View {
  All,
  User,
  Hotel,
}

export type BookingListProps = {
  view: View;
  page?: number;
  pageSize?: number;
};

export type BookingProps = {
  hotelId: number;
  roomId: number;
  checkInDate: string;
  checkOutDate: string;
};

export type PaymentProps = {
  buyerName: string;
  buyerEmail: string;
  productName: string;
  amount: number;
  onPaymentComplete: (kakapPaymentRequest: KakaoPaymentRequest) => void;
};
