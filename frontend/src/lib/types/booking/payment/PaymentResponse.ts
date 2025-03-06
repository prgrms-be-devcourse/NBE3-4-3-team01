export interface PaymentResponse {
  paymentId: number;
  merchantUid: number;
  amount: number;
  paymentStatus: string;
  paidAt: string;
}
