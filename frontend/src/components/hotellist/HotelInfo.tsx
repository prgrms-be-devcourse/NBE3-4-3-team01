"use client";

import Image from "next/image";
import { Card, CardContent } from "@/components/ui/card";
import type { GetHotelResponse } from "@/lib/types/hotel/GetHotelResponse";
import { useRouter } from "next/navigation";
import { Clock } from "lucide-react";

interface HotelInfoProps extends GetHotelResponse {
  checkInDate: string;
  checkoutDate: string;
  personal: string;
}

export default function HotelInfo({
  hotelId,
  hotelName,
  hotelGrade,
  streetAddress,
  averageRating,
  totalReviewCount,
  thumbnailUrl,
  checkInTime,
  price,
  checkInDate,
  checkoutDate,
  personal,
}: HotelInfoProps) {
  const router = useRouter();

  const handleClick = () => {
    const params = new URLSearchParams();
    params.set("checkInDate", checkInDate);
    params.set("checkoutDate", checkoutDate);
    params.set("personal", personal);
    router.push(`/hotels/${hotelId}?${params.toString()}`);
  };

  // 초 제거 (HH:mm:ss → HH:mm)
  const formattedCheckInTime = checkInTime.split(":").slice(0, 2).join(":");

  return (
    <Card
      onClick={handleClick}
      className="relative w-full overflow-hidden bg-white cursor-pointer hover:shadow-lg transition-shadow"
    >
      <CardContent className="p-0">
        <div className="flex flex-col sm:flex-row w-full">
          <div className="relative w-full sm:w-64 h-48">
            <Image
              src={thumbnailUrl || "/api/placeholder/400/320"}
              alt={hotelName}
              fill
              sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
              className="object-cover"
            />
          </div>

          <div className="flex-1 p-4">
            <div className="flex justify-between items-start">
              <div>
                <h3 className="text-xl font-semibold mb-1">{hotelName}</h3>
                <p className="text-gray-600 text-sm mb-2">{streetAddress}</p>
              </div>
              <div className="text-right">
                <div className="bg-blue-100 text-blue-800 px-2 py-1 rounded-lg inline-flex items-center gap-1">
                  <span className="font-bold">{averageRating}</span>
                  <span className="text-sm">/5</span>
                </div>
                <p className="text-sm text-gray-600 mt-1">
                  후기 {totalReviewCount && totalReviewCount.toLocaleString()}개
                </p>
              </div>
            </div>

            <div className="flex items-center gap-4 mt-2 mb-4">
              <span className="text-gray-700">{`${hotelGrade}성급`}</span>
            </div>
            <div className="flex items-center text-gray-600 text-sm">
              <Clock className="w-4 h-4 mr-1" />
              <span>체크인 {formattedCheckInTime}</span>
            </div>

            <div className="mt-auto text-right">
              <p className="text-2xl font-bold text-blue-600">
                {price && price.toLocaleString()}원~
              </p>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
