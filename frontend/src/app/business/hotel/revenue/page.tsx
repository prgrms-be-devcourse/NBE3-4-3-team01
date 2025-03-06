"use client";

import HotelRevenue from "@/components/booking/revenue/Revenue";
import Navigation from "@/components/navigation/Navigation";
import { getRoleFromCookie } from "@/lib/utils/CookieUtil";

const RevenuePage = function () {
  const cookie = getRoleFromCookie();
  const hotelId = Number(cookie?.hotelId);
  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white">
      <Navigation />
      <div className="content-wrapper container mx-auto p-4">
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold text-gray-800 mb-2">호텔 매출</h1>
          <div className="w-32 h-1 bg-blue-500 mx-auto rounded-full"></div>
        </div>
        <HotelRevenue hotelId={hotelId} />
      </div>
    </div>
  );
};

export default RevenuePage;
