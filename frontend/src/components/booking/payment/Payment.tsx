"use client";

import { useEffect } from "react";
import { Button } from "@/components/ui/button";
import { getUid } from "@/lib/api/booking/payment/PaymentApi";
import { KakaoPaymentRequest } from "@/lib/types/booking/payment/KakaoPaymentRequest";
import { PaymentProps } from "@/lib/types/booking/BookingProps";

declare global {
  interface Window {
    IMP: any;
  }
}

const Payment = ({
  buyerEmail,
  buyerName,
  amount,
  productName,
  onPaymentComplete,
}: PaymentProps) => {
  useEffect(() => {
    // iamport.js 스크립트 로드
    const script = document.createElement("script");
    script.src = "https://cdn.iamport.kr/v1/iamport.js";
    script.async = true;
    document.body.appendChild(script);

    return () => {
      document.body.removeChild(script);
    };
  }, []);

  const handlePayment = async () => {
    return new Promise<PaymentRequest>(async () => {
      try {
        // 결제 정보 가져오기
        const { apiId, channelKey, merchantUid } = await getUid();

        // IMP 초기화
        window.IMP.init(apiId);

        // 결제 요청
        window.IMP.request_pay(
          {
            channelKey: channelKey,
            merchant_uid: merchantUid,
            name: productName,
            amount: amount,
            buyer_email: buyerEmail,
            buyer_name: buyerName,
          },
          async function (rsp: any) {
            if (rsp.success) {
              //예약 데이터 생성
              const kakaoPaymentRequest: KakaoPaymentRequest = {
                merchantUid: merchantUid,
                amount: amount,
                paidAtTimestamp: rsp.paid_at,
              };

              // bookingRequest 데이터 반환
              onPaymentComplete(kakaoPaymentRequest);
            } else {
              throw new Error(rsp.error_msg);
            }
          }
        );
      } catch (error) {
        alert("결제에 실패했습니다.");
        console.error(error);
      }
    });
  };

  return (
    <Button onClick={handlePayment} size="lg" className="w-full">
      결제하기
    </Button>
  );
};

export default Payment;
