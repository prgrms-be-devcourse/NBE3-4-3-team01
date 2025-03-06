"use client";

import RoomDetail from "@/components/business/rooms/RoomDetail";
import { Card, CardContent } from "@/components/ui/card";
import { findRoomDetail } from "@/lib/api/hotel/room/BusinessRoomApi";
import { GetRoomDetailResponse } from "@/lib/types/room/GetRoomDetailResponse";
import { useParams, useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";
import RoomImages from "@/components/business/rooms/RoomImages";
import Navigation from "@/components/navigation/Navigation";

const RoomDetailPage: React.FC = () => {
  const params = useParams();
  const [roomDetail, setRoomDetail] = useState<GetRoomDetailResponse | null>(
    null
  );
  const [isLoading, setIsLoading] = useState(true);
  const hotelId = params.hotelId;
  const roomId = params.roomId;
  const searchParams = useSearchParams();
  const checkInDate = searchParams.get("checkInDate") || "";
  const checkoutDate = searchParams.get("checkoutDate") || "";

  useEffect(() => {
    const fetchRoomDetail = async () => {
      try {
        console.log("페이지 호텔 Id : ", Number(hotelId));
        console.log("페이지 객실 Id : ", Number(roomId));
        const response = await findRoomDetail(Number(hotelId), Number(roomId));
        setRoomDetail(response);
      } catch (error) {
        throw error;
      } finally {
        setIsLoading(false);
      }
    };

    fetchRoomDetail();
  }, [hotelId, roomId]);

  if (isLoading) {
    return <div className="text-center">로딩 중...</div>;
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
          <RoomDetail
            room={roomDetail}
            checkInDate={checkInDate}
            checkoutDate={checkoutDate}
          />
        </CardContent>
      </Card>
    </div>
  );
};

export default RoomDetailPage;
