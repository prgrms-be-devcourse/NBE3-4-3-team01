"use client";
import { useEffect, useState } from "react";
import HotelBookingList from "./HotelBookingList";
import UserBookingList from "./UserBookingList";
import { BookingResponseSummary } from "@/lib/types/booking/BookingResponseSummary";
import { PageDto } from "@/lib/types/PageDto";
import { getHotelBookings, getMyBookings } from "@/lib/api/booking/BookingApi";
import { BookingListProps, View } from "@/lib/types/booking/BookingProps";
import Pagination from "../pagination/Pagination";

const BookingList = function ({ view, page = 1, pageSize }: BookingListProps) {
  const [bookings, setBookings] =
    useState<PageDto<BookingResponseSummary> | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchBookings = async () => {
      setIsLoading(true);
      let data: PageDto<BookingResponseSummary> | null = null;
      try {
        switch (view) {
          case View.User:
            data = await getMyBookings(page, pageSize);
            break;
          case View.Hotel:
            data = await getHotelBookings(page, pageSize);
            break;
          default:
            throw new Error("잘못된 Booking View 입니다.");
        }
      } catch (error) {
        alert(error);
        console.error(error);
      } finally {
        setBookings(data);
        setIsLoading(false);
      }
    };
    fetchBookings();
  }, [page, pageSize]);

  // 로딩 중일때
  if (isLoading) {
    return <div className="pb-24">예약을 불러오는 중입니다...</div>;
  }

  // data를 받아오지 못했을 때
  if (!bookings) {
    return <div className="pb-24">예약을 불러올 수 없습니다.</div>;
  }

  switch (view) {
    case View.User:
      return (
        <div className="pb-24">
          <UserBookingList bookings={bookings} />
          <Pagination
            currentPage={page}
            totalPages={bookings?.totalPages || 1}
            basePath="me/orders"
          />
        </div>
      );
    case View.Hotel:
      return (
        <div className="pb-24">
          <HotelBookingList bookings={bookings} />
          <Pagination
            currentPage={page}
            totalPages={bookings?.totalPages || 1}
            basePath="business/bookings"
          />
        </div>
      );
    default:
      return <div className="pb-24">예약을 불러올 수 없습니다.</div>;
  }
};

export default BookingList;
