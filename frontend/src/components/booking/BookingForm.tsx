"use client";

import { BookingProps } from "@/lib/types/booking/BookingProps";
import BookingFormLeft from "./BookingFormLeft";
import BookingFormRight from "./BookingFormRight";
import { useEffect, useState } from "react";
import { BookingFormResponse } from "@/lib/types/booking/BookingFormResponse";
import { preBook } from "@/lib/api/booking/BookingApi";

const BookingForm = ({
  hotelId,
  roomId,
  checkInDate,
  checkOutDate,
}: BookingProps) => {
  const [bookingFormData, setBookingFormData] =
    useState<BookingFormResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchBooking = async () => {
      setIsLoading(true);
      let data: BookingFormResponse | null = null;
      try {
        data = await preBook(hotelId, roomId);
      } catch (error) {
        alert(error);
        console.error(error);
      } finally {
        setBookingFormData(data);
        setIsLoading(false);
      }
    };
    fetchBooking();
  }, []);

  if (isLoading) {
    return <div>예약 페이지 정보를 불러오는 중입니다...</div>;
  }

  if (!bookingFormData) {
    return <div>예약 페이지 정보를 불러올 수 없습니다.</div>;
  }

  return (
    <div className="w-full max-w-6xl mx-auto grid grid-cols-1 md:grid-cols-[1.5fr,1fr] gap-6">
      <BookingFormLeft bookingFormData={bookingFormData} />
      <BookingFormRight
        bookingFormData={bookingFormData}
        checkInDate={checkInDate}
        checkOutDate={checkOutDate}
      />
    </div>
  );
};

export default BookingForm;
