"use client";
import BookingForm from "@/components/booking/BookingForm";
import Navigation from "@/components/navigation/Navigation";
import { useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";

const BookingDetailsPage = () => {
  const searchParams = useSearchParams();
  const [hotelId, setHotelId] = useState<number>(0);
  const [roomId, setRoomId] = useState<number>(0);
  const [checkInDate, setCheckInDate] = useState<string>("");
  const [checkOutDate, setCheckOutDate] = useState<string>("");
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const newHotelId = Number(searchParams.get("hotelId"));
    const newRoomId = Number(searchParams.get("roomId"));
    const newCheckInDate = searchParams.get("checkInDate") ?? "";
    const newCheckOutDate = searchParams.get("checkOutDate") ?? "";

    setHotelId(newHotelId);
    setRoomId(newRoomId);
    setCheckInDate(newCheckInDate);
    setCheckOutDate(newCheckOutDate);
    setIsLoading(false);
  }, [searchParams]);

  // 모든 필수 데이터가 있는지 확인
  const isDataComplete =
    !isLoading &&
    hotelId > 0 &&
    roomId > 0 &&
    checkInDate !== "" &&
    checkOutDate !== "";

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white">
      <Navigation />
      <div className="content-wrapper container mx-auto p-4">
        {!isDataComplete ? (
          <div>필요한 정보를 불러오는 중입니다...</div>
        ) : (
          <BookingForm
            hotelId={hotelId}
            roomId={roomId}
            checkInDate={checkInDate}
            checkOutDate={checkOutDate}
          />
        )}
      </div>
    </div>
  );
};

export default BookingDetailsPage;
