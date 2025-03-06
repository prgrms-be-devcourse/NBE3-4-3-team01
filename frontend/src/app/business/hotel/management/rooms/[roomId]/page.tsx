"use client";

import RoomDetail from "@/components/business/rooms/RoomDetail";
import { Card, CardContent } from "@/components/ui/card";
import { findRoomDetail } from "@/lib/api/hotel/room/BusinessRoomApi";
import { GetRoomDetailResponse } from "@/lib/types/room/GetRoomDetailResponse";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import RoomImages from "@/components/business/rooms/RoomImages";
import Navigation from "@/components/navigation/Navigation";
import { getRoleFromCookie } from "@/lib/utils/CookieUtil";

const RoomDetailPage: React.FC = () => {
  const cookie = getRoleFromCookie();
  const hotelId = Number(cookie?.hotelId);
  const [roomDetail, setRoomDetail] = useState<GetRoomDetailResponse | null>(
    null
  );
  const [isLoading, setIsLoading] = useState(true);
  const params = useParams();
  const roomsId = params.roomsId;

  useEffect(() => {
    const fetchRoomDetail = async () => {
      try {
        console.log("호텔 Id : ", Number(hotelId));
        console.log("객실 Id : ", Number(roomsId));
        const response = await findRoomDetail(Number(hotelId), Number(roomsId));
        setRoomDetail(response);
      } catch (error) {
        throw error;
      } finally {
        setIsLoading(false);
      }
    };

    fetchRoomDetail();
  }, [hotelId, roomsId]);

  if (isLoading) {
    return <div className="text-center text-lg font-semibold">로딩 중...</div>;
  }

  if (!roomDetail) {
    return <div className="text-center">객실 정보를 불러올 수가 없습니다.</div>;
  }

  return (
    <div className="p-4 pt-[100px]">
      <Navigation />
      <Card>
        <CardContent>
          <RoomImages images={roomDetail.roomImageUrls} />
          <RoomDetail room={roomDetail} />
        </CardContent>
      </Card>
    </div>
  );
};

export default RoomDetailPage;
