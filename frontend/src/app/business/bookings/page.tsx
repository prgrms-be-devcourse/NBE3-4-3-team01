"use client";

import BookingList from "@/components/booking/BookingList";
import Navigation from "@/components/navigation/Navigation";
import { View } from "@/lib/types/booking/BookingProps";
import { useSearchParams } from "next/navigation";

const HotelBookingsPage = () => {
  const searchParams = useSearchParams();
  const page = Number(searchParams.get("page")) || 1;

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white">
      <Navigation />
      <div className="content-wrapper container mx-auto p-4">
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold text-gray-800 mb-2">예약 관리</h1>
          <div className="w-32 h-1 bg-blue-500 mx-auto rounded-full"></div>
        </div>
        <BookingList view={View.Hotel} page={page} pageSize={10} />
      </div>
    </div>
  );
};

export default HotelBookingsPage;
