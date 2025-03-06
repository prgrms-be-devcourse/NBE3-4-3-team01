"use client";

import HotelDetail from "@/components/business/hotel/HotelDetail";
import { Card, CardContent } from "@/components/ui/card";
import { findHotelDetailWithAvailableRooms } from "@/lib/api/hotel/BusinessHotelApi";
import { GetHotelDetailResponse } from "@/lib/types/hotel/GetHotelDetailResponse";
import { useParams, useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";
import RoomList from "@/components/business/rooms/RoomList";
import HotelImages from "@/components/business/hotel/HotelImages";
import Navigation from "@/components/navigation/Navigation";

const HotelDetailPage: React.FC = () => {
  const { hotelId } = useParams();
  const [hotelDetail, setHotelDetail] = useState<GetHotelDetailResponse | null>(
    null
  );
  const [isLoading, setIsLoading] = useState(true);
  const searchParams = useSearchParams();
  const checkInDate = searchParams.get("checkInDate") || "";
  const checkoutDate = searchParams.get("checkoutDate") || "";
  const personal = searchParams.get("personal") || "";

  useEffect(() => {
    console.log("체크인 - 체크아웃 : ", checkInDate, checkoutDate);
    const fetchHotelDetail = async () => {
      try {
        const response = await findHotelDetailWithAvailableRooms(
          Number(hotelId),
          checkInDate,
          checkoutDate,
          personal
        );
        setHotelDetail(response);
      } catch (error) {
        throw error;
      } finally {
        setIsLoading(false);
      }
    };

    fetchHotelDetail();
  }, [hotelId]);

  if (isLoading) {
    return <div className="text-center text-lg font-semibold">로딩 중...</div>;
  }

  if (!hotelDetail) {
    return (
      <div className="text-center pt-[100px]">
        <Navigation />
        <p className="text-gray-500 text-center">
          호텔 정보를 불러올 수가 없습니다.
        </p>
      </div>
    );
  }

  return (
    <div className="p-4 pt-[100px]">
      <Navigation />
      <Card>
        <CardContent>
          <HotelImages images={hotelDetail.hotelImageUrls} />
          <HotelDetail hotel={hotelDetail.hotelDetailDto} />
          <RoomList
            rooms={hotelDetail.hotelDetailDto.rooms}
            checkInDate={checkInDate}
            checkoutDate={checkoutDate}
          />
        </CardContent>
      </Card>
    </div>
  );
};

export default HotelDetailPage;
