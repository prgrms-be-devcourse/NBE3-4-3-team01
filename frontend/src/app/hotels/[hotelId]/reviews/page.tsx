"use client";

import Navigation from "@/components/navigation/Navigation";
import HotelReviews from "@/components/reviewwithcomment/HotelReviews";
import { Card } from "@/components/ui/card";
import { useSearchParams, useParams } from "next/navigation";

const BusinessHotelReviewsPage = () => {
  const searchParams = useSearchParams();
  const params = useParams();
  const hotelId: number = Number(params.hotelId);
  const page = Number(searchParams.get("page")) || 1;

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white">
      <Navigation />
      <div className="container mx-auto px-4 py-12">
        {/* 메인 컨텐츠 */}
        <div className="content-wrapper container mx-auto">
          <Card className="bg-white rounded-xl shadow-lg">
            <HotelReviews hotelId={hotelId} page={page} />
          </Card>
        </div>
      </div>
    </div>
  );
};

export default BusinessHotelReviewsPage;
