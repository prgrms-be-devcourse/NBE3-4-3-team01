"use client";

import Navigation from "@/components/navigation/Navigation";
import HotelReviews from "@/components/reviewwithcomment/HotelReviews";
import { useSearchParams, useParams } from "next/navigation";
import { Card } from "@/components/ui/card";

const HotelReviewsPage = () => {
  const searchParams = useSearchParams();
  const params = useParams();
  const hotelId: number = Number(params.hotelId);
  const page = Number(searchParams.get("page")) || 1;

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white">
      <Navigation />
      <div className="content-wrapper container mx-auto px-4 py-12">
        {/* 헤더 섹션 */}
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold text-gray-800 mb-2">
            호텔 리뷰 목록
          </h1>
          <div className="w-32 h-1 bg-blue-500 mx-auto rounded-full"></div>
        </div>

        {/* 메인 컨텐츠 */}
        <div className="max-w-4xl mx-auto">
          <Card className="bg-white rounded-xl shadow-lg">
            <HotelReviews hotelId={hotelId} page={page} isBusinessUser={true} />
          </Card>
        </div>
      </div>
    </div>
  );
};

export default HotelReviewsPage;
