"use client";

import { useEffect, useState } from "react";
import { getBookingDetails } from "@/lib/api/booking/BookingApi";
import { BookingResponseDetails } from "@/lib/types/booking/BookingResponseDetails";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableRow } from "@/components/ui/table";
import { Building2, Calendar, CreditCard } from "lucide-react";
import { Button } from "@/components/ui/button";

const BookingDetails = function ({ bookingId }: { bookingId: number }) {
  const [bookingDetails, setBookingDetails] =
    useState<BookingResponseDetails | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchBooking = async () => {
      setIsLoading(true);
      let data: BookingResponseDetails | null = null;
      try {
        data = await getBookingDetails(bookingId);
      } catch (error) {
        alert(error);
        console.error(error);
      } finally {
        setBookingDetails(data);
        setIsLoading(false);
      }
    };
    fetchBooking();
  }, []);

  const handleClose = () => {
    window.close();
  };

  if (isLoading) {
    return <div>예약을 불러오는 중입니다...</div>;
  }

  if (!bookingDetails) {
    return <div>예약을 불러올 수 없습니다.</div>;
  }

  return (
    <div className="space-y-6 pb-6">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 w-full max-w-7xl mx-auto">
        <Card className="w-full">
          <CardHeader>
            <div className="flex items-center space-x-2">
              <Building2 className="w-5 h-5 text-primary" />
              <CardTitle>예약 상세 정보</CardTitle>
            </div>
          </CardHeader>
          <CardContent>
            <Table>
              <TableBody>
                <TableRow>
                  <TableCell className="font-medium">예약 ID</TableCell>
                  <TableCell className="text-right">
                    {bookingDetails.bookingId}
                  </TableCell>
                </TableRow>
                <TableRow>
                  <TableCell className="font-medium">호텔 이름</TableCell>
                  <TableCell className="text-right">
                    {bookingDetails.hotel.hotelName}
                  </TableCell>
                </TableRow>
                <TableRow>
                  <TableCell className="font-medium">호텔 이메일</TableCell>
                  <TableCell className="text-right">
                    {bookingDetails.hotel.hotelEmail}
                  </TableCell>
                </TableRow>
                <TableRow>
                  <TableCell className="font-medium">호텔 주소</TableCell>
                  <TableCell className="text-right">
                    {bookingDetails.hotel.streetAddress} (
                    {bookingDetails.hotel.zipCode})
                  </TableCell>
                </TableRow>
                <TableRow>
                  <TableCell className="font-medium">호텔 연락처</TableCell>
                  <TableCell className="text-right">
                    {bookingDetails.hotel.hotelPhoneNumber}
                  </TableCell>
                </TableRow>
                <TableRow>
                  <TableCell className="font-medium">투숙객 이름</TableCell>
                  <TableCell className="text-right">
                    {bookingDetails.member.memberName}
                  </TableCell>
                </TableRow>
                <TableRow>
                  <TableCell className="font-medium">투숙객 이메일</TableCell>
                  <TableCell className="text-right">
                    {bookingDetails.member.memberEmail}
                  </TableCell>
                </TableRow>
                <TableRow>
                  <TableCell className="font-medium">객실 유형</TableCell>
                  <TableCell className="text-right">
                    {bookingDetails.room.roomName}
                  </TableCell>
                </TableRow>
                <TableRow>
                  <TableCell className="font-medium">예약 상태</TableCell>
                  <TableCell className="text-right">
                    <span
                      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                        bookingDetails.bookingStatus === "COMPLETED"
                          ? "bg-green-100 text-green-800"
                          : bookingDetails.bookingStatus === "CANCELLED"
                          ? "bg-red-100 text-red-800"
                          : "bg-blue-100 text-blue-800"
                      }`}
                    >
                      {bookingDetails.bookingStatus === "COMPLETED"
                        ? "완료"
                        : bookingDetails.bookingStatus === "CANCELLED"
                        ? "취소"
                        : "확정"}
                    </span>
                  </TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </CardContent>
        </Card>

        <Card className="w-full">
          <CardHeader>
            <div className="flex items-center space-x-2">
              <Calendar className="w-5 h-5 text-primary" />
              <CardTitle>날짜 정보</CardTitle>
            </div>
          </CardHeader>
          <CardContent>
            <Table>
              <TableBody>
                <TableRow>
                  <TableCell className="font-medium">예약 생성일</TableCell>
                  <TableCell className="text-right">
                    {bookingDetails.createdAt.substring(0, 10)}{" "}
                    {bookingDetails.createdAt.substring(11, 16)}
                  </TableCell>
                </TableRow>
                <TableRow>
                  <TableCell className="font-medium">체크인 날짜</TableCell>
                  <TableCell className="text-right">
                    {bookingDetails.checkInDate}
                  </TableCell>
                </TableRow>
                <TableRow>
                  <TableCell className="font-medium">체크아웃 날짜</TableCell>
                  <TableCell className="text-right">
                    {bookingDetails.checkOutDate}
                  </TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </CardContent>
        </Card>

        <Card className="w-full">
          <CardHeader>
            <div className="flex items-center space-x-2">
              <CreditCard className="w-5 h-5 text-primary" />
              <CardTitle>결제 정보</CardTitle>
            </div>
          </CardHeader>
          <CardContent>
            <Table>
              <TableBody>
                <TableRow>
                  <TableCell className="font-medium">결제 ID</TableCell>
                  <TableCell className="text-right">
                    {bookingDetails.payment.paymentId}
                  </TableCell>
                </TableRow>
                <TableRow>
                  <TableCell className="font-medium">거래 UID</TableCell>
                  <TableCell className="text-right">
                    {bookingDetails.payment.merchantUid}
                  </TableCell>
                </TableRow>
                <TableRow>
                  <TableCell className="font-medium">결제 금액</TableCell>
                  <TableCell className="text-right">
                    {bookingDetails.payment.amount.toLocaleString()}원
                  </TableCell>
                </TableRow>
                <TableRow>
                  <TableCell className="font-medium">결제 상태</TableCell>
                  <TableCell className="text-right">
                    <span
                      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                        bookingDetails.payment.paymentStatus === "PAID"
                          ? "bg-green-100 text-green-800"
                          : bookingDetails.payment.paymentStatus === "CANCELLED"
                          ? "bg-red-100 text-red-800"
                          : "bg-blue-100 text-blue-800"
                      }`}
                    >
                      {bookingDetails.payment.paymentStatus === "PAID"
                        ? "완료"
                        : bookingDetails.payment.paymentStatus === "CANCELLED"
                        ? "취소"
                        : bookingDetails.payment.paymentStatus}
                    </span>
                  </TableCell>
                </TableRow>
                <TableRow>
                  <TableCell className="font-medium">결제 일시</TableCell>
                  <TableCell className="text-right">
                    {bookingDetails.payment.paidAt.substring(0, 10)}{" "}
                    {bookingDetails.payment.paidAt.substring(11, 16)}
                  </TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      </div>

      <div className="flex justify-center w-full max-w-7xl mx-auto">
        <Button onClick={handleClose} variant="outline">
          닫기
        </Button>
      </div>
    </div>
  );
};

export default BookingDetails;
