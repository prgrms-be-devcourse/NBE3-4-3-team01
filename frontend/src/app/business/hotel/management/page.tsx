"use client";

import HotelDetail from "@/components/business/hotel/HotelDetail";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { findHotelDetail } from "@/lib/api/hotel/BusinessHotelApi";
import { GetHotelDetailResponse } from "@/lib/types/hotel/GetHotelDetailResponse";
import { useEffect, useState } from "react";
import RoomList from "@/components/business/rooms/RoomList";
import HotelImages from "@/components/business/hotel/HotelImages";
import { getRoleFromCookie } from "@/lib/utils/CookieUtil";
import Navigation from "@/components/navigation/Navigation";

const HotelDetailPage: React.FC = () => {
  const cookie = getRoleFromCookie();
  const hotelId = Number(cookie?.hotelId);
  const [hotelDetail, setHotelDetail] = useState<GetHotelDetailResponse | null>(
    null
  );
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchHotelDetail = async () => {
      try {
        const response = await findHotelDetail(Number(hotelId));
        console.log("hotelId : ", hotelId);
        console.log("response : ", response);
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
    return <div className="text-center">로딩 중...</div>;
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
          <RoomList rooms={hotelDetail.hotelDetailDto.rooms} />
        </CardContent>
      </Card>
    </div>
  );
};

export default HotelDetailPage;
