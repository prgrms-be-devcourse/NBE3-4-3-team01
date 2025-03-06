import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  CardFooter,
} from "@/components/ui/card";
import Payment from "./payment/Payment";
import { KakaoPaymentRequest } from "@/lib/types/booking/payment/KakaoPaymentRequest";
import { book } from "@/lib/api/booking/BookingApi";
import { BookingRequest } from "@/lib/types/booking/BookingRequest";
import { useRouter } from "next/navigation";
import { BookingFormResponse } from "@/lib/types/booking/BookingFormResponse";

const BookingFormRight = function ({
  checkInDate,
  checkOutDate,
  bookingFormData,
}: {
  checkInDate: string;
  checkOutDate: string;
  bookingFormData: BookingFormResponse;
}) {
  const hotelDetails = bookingFormData.hotel;
  const roomDetails = bookingFormData.room;
  const memberDetails = bookingFormData.member;
  const router = useRouter();

  const createBookingAndRedirect = async (
    kakaoPaymentRequest: KakaoPaymentRequest
  ) => {
    try {
      // KakaoPaymentRequest와 나머지 정보를 합쳐서 BookingRequest 생성
      const bookingRequest: BookingRequest = {
        hotelId: hotelDetails.hotelId,
        roomId: roomDetails.id,
        checkInDate: checkInDate,
        checkOutDate: checkOutDate,
        merchantUid: kakaoPaymentRequest.merchantUid,
        amount: kakaoPaymentRequest.amount,
        paidAtTimestamp: kakaoPaymentRequest.paidAtTimestamp,
      };

      // 예약 API 호출
      await book(bookingRequest);
      new Promise((resolve) => {
        alert("예약에 성공했습니다.");
        resolve(true);
      }).then(() => {
        router.push("/me/orders");
      });
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <Card className="self-start">
      <CardHeader>
        <CardTitle>예약 정보</CardTitle>
        <CardDescription>예약 정보를 확인해주세요.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        <div>
          <h3 className="text-sm font-medium text-gray-500">호텔명</h3>
          <p className="text-lg font-medium mt-1">{hotelDetails.hotelName}</p>
        </div>

        <div>
          <h3 className="text-sm font-medium text-gray-500">객실 유형</h3>
          <p className="text-lg font-medium mt-1">{roomDetails.roomName}</p>
        </div>

        <div>
          <h3 className="text-sm font-medium text-gray-500">체크인 날짜</h3>
          <p className="text-lg font-medium mt-1">{checkInDate}</p>
        </div>

        <div>
          <h3 className="text-sm font-medium text-gray-500">체크아웃 날짜</h3>
          <p className="text-lg font-medium mt-1">{checkOutDate}</p>
        </div>

        <div>
          <h3 className="text-sm font-medium text-gray-500">투숙객 이름</h3>
          <p className="text-lg font-medium mt-1">{memberDetails.memberName}</p>
        </div>

        <div>
          <h3 className="text-sm font-medium text-gray-500">투숙객 이메일</h3>
          <p className="text-lg font-medium mt-1">
            {memberDetails.memberEmail}
          </p>
        </div>

        <div className="pt-4 border-t">
          <div className="flex justify-between items-center">
            <span className="text-base font-medium">총 결제 금액</span>
            <span className="text-xl font-bold text-blue-600">
              {roomDetails.basePrice.toLocaleString()}원
            </span>
          </div>
        </div>
      </CardContent>
      <CardFooter>
        <Payment
          buyerEmail={memberDetails.memberEmail}
          buyerName={memberDetails.memberName}
          amount={roomDetails.basePrice}
          productName={roomDetails.roomName}
          onPaymentComplete={createBookingAndRedirect}
        ></Payment>
      </CardFooter>
    </Card>
  );
};

export default BookingFormRight;
