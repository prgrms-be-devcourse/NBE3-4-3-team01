"use client";

import { Button } from "@/components/ui/button";
import { useRouter } from "next/navigation";
import { FaHotel, FaArrowRight } from "react-icons/fa";

const EmptyHotelState = () => {
  const router = useRouter();

  return (
    <div className="min-h-[80vh] flex items-center justify-center">
      <div className="bg-white p-12 rounded-3xl shadow-xl max-w-2xl w-full mx-4 text-center">
        <div className="mb-8">
          <FaHotel className="text-8xl text-sky-500 mx-auto mb-6" />
          <h1 className="text-4xl font-bold text-gray-800 mb-4">
            호텔 등록이 필요합니다
          </h1>
          <p className="text-xl text-gray-600 mb-8">
            사업자님만의 특별한 호텔을 등록하고 관리를 시작해보세요!
          </p>
          <div className="space-y-4 text-gray-500">
            <p>✓ 호텔 정보 등록 및 관리</p>
            <p>✓ 객실 등록 및 관리</p>
            <p>✓ 예약 현황 확인</p>
            <p>✓ 리뷰 관리</p>
          </div>
        </div>

        <Button
          onClick={() => router.push("/business/hotel/")}
          className="group bg-gradient-to-r from-sky-500 to-blue-600 text-white text-lg px-8 py-6 rounded-xl hover:shadow-lg transition-all duration-300 hover:scale-105"
        >
          <span className="flex items-center gap-3">
            호텔 등록하기
            <FaArrowRight className="group-hover:translate-x-1 transition-transform" />
          </span>
        </Button>
      </div>
    </div>
  );
};

export default EmptyHotelState;
