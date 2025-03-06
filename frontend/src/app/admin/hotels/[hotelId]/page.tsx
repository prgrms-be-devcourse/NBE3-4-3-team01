"use client";

import { approveHotel, getHotelForAdmin } from "@/lib/api/admin/AdminHotelApi";
import { AdminHotelRequest } from "@/lib/types/admin/request/AdminHotelRequest";
import { HotelStatus } from "@/lib/types/hotel/HotelStatus";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import Link from "next/link";
import {
  AdminHotelDetailResponse,
  HotelApprovalResult,
} from "@/lib/types/admin/response/AdminHotelResponse";
import { Button } from "@/components/ui/button";
import Navigation from "@/components/navigation/Navigation";
import Loading from "@/components/hotellist/Loading";

export default function AdminHotelDetailPage() {
  const { hotelId } = useParams();
  const [hotel, setHotel] = useState<AdminHotelDetailResponse | null>(null);
  const [status, setStatus] = useState<HotelStatus | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchHotel = async () => {
      if (!hotelId) return;
      try {
        const data = await getHotelForAdmin(Number(hotelId));
        setHotel(data);
        setStatus(data.hotelStatus as HotelStatus);
      } catch (error) {
        setError("호텔 데이터를 불러오는 중 오류가 발생했습니다.");
      } finally {
        setLoading(false);
      }
    };

    fetchHotel();
  }, [hotelId]);

  const handleSave = async () => {
    try {
      if (!hotelId || !status) return;

      const updatedData: AdminHotelRequest = {
        hotelStatus: status,
      };

      const response: HotelApprovalResult = await approveHotel(
        Number(hotelId),
        updatedData
      );
      alert(
        `호텔 상태가 저장되었습니다: ${response.name} - ${response.status}`
      );
    } catch (error) {
      setError("호텔 상태를 저장하는 중 오류가 발생했습니다.");
    }
  };

  if (!hotelId)
    return (
      <p className="text-red-500 text-center">
        Error: Hotel ID is missing from the URL.
      </p>
    );
  if (loading) return <Loading />;
  if (error) return <p className="text-center text-red-500">Error: {error}</p>;
  if (!hotel)
    return <p className="text-center text-gray-500">No hotel found</p>;

  return (
    <div className="relative min-h-screen bg-background">
      {/* Background gradient */}
      <div className="absolute inset-0 bg-gradient-to-b from-blue-100 to-white" />
      {/* Decorative circles */}
      <div className="absolute top-20 right-20 w-64 h-64 bg-blue-200 rounded-full blur-3xl opacity-20" />
      <div className="absolute bottom-20 left-20 w-96 h-96 bg-blue-300 rounded-full blur-3xl opacity-10" />

      <div className="relative z-10">
        <Navigation />
        <div className="content-wrapper container mx-auto px-4">
          {/* 헤더 */}
          <div className="text-center mt-20 mb-10">
            <h1 className="text-4xl font-bold text-gray-800">호텔 상세 정보</h1>
            <p className="text-lg text-gray-600 mt-2">
              호텔 정보를 확인하고 상태를 변경하세요.
            </p>
          </div>

          {/* 호텔 정보 박스 */}
          <div className="w-full max-w-[75rem] mx-auto space-y-6">
            {/* 저장 및 목록으로 돌아가기 버튼 */}
            <div className="w-full flex justify-end space-x-4 mb-2">
              <Button
                onClick={handleSave}
                className="bg-blue-500 hover:bg-blue-600 text-white px-5 py-2 text-lg rounded"
              >
                저장
              </Button>
              <Link href="/admin/hotels">
                <Button className="bg-gray-300 hover:bg-gray-400 text-gray-800 px-5 py-2 text-lg rounded">
                  목록으로 돌아가기
                </Button>
              </Link>
            </div>

            {/* 호텔 기본 정보 */}
            <div className="bg-white/50 p-6 rounded-lg shadow-sm">
              <h2 className="text-xl font-semibold text-gray-800 mb-4">
                호텔 기본 정보
              </h2>
              <div className="grid grid-cols-2 gap-4">
                <p className="text-base text-gray-700 font-semibold">이름</p>
                <p className="text-base text-gray-700">{hotel.hotelName}</p>
                <p className="text-base text-gray-700 font-semibold">주소</p>
                <p className="text-base text-gray-700">
                  {hotel.streetAddress}, {hotel.zipCode}
                </p>
                <p className="text-base text-gray-700 font-semibold">등급</p>
                <p className="text-base text-gray-700">
                  {hotel.hotelGrade}성급
                </p>
                <p className="text-base text-gray-700 font-semibold">체크인</p>
                <p className="text-base text-gray-700">{hotel.checkInTime}</p>
                <p className="text-base text-gray-700 font-semibold">
                  체크아웃
                </p>
                <p className="text-base text-gray-700">{hotel.checkOutTime}</p>
              </div>
            </div>
            {/* 호텔 상태 변경 */}
            <div className="bg-white/50 p-6 rounded-lg shadow-sm flex items-center justify-between">
              <p className="text-base text-gray-700 font-semibold">상태 변경</p>
              <select
                value={status || ""}
                onChange={(e) => setStatus(e.target.value as HotelStatus)}
                className="border p-2 rounded bg-white text-base w-60"
              >
                <option value="AVAILABLE">승인</option>
                <option value="PENDING">대기</option>
                <option value="UNAVAILABLE">거절</option>
              </select>
            </div>

            {/* 호텔 리뷰 정보 */}
            <div className="bg-white/50 p-6 rounded-lg shadow-sm">
              <h2 className="text-xl font-semibold text-gray-800 mb-4">
                호텔 리뷰 정보
              </h2>
              <div className="grid grid-cols-2 gap-4">
                <p className="text-base text-gray-700 font-semibold">
                  평균 평점
                </p>
                <p className="text-base text-gray-700">{hotel.averageRating}</p>
                <p className="text-base text-gray-700 font-semibold">
                  총 리뷰 수
                </p>
                <p className="text-base text-gray-700">
                  {hotel.totalReviewCount}
                </p>
              </div>
            </div>

            {/* 연락 정보 */}
            <div className="bg-white/50 p-6 rounded-lg shadow-sm">
              <h2 className="text-xl font-semibold text-gray-800 mb-4">
                연락 정보
              </h2>
              <div className="grid grid-cols-2 gap-4">
                <p className="text-base text-gray-700 font-semibold">이메일</p>
                <p className="text-base text-gray-700">{hotel.hotelEmail}</p>
                <p className="text-base text-gray-700 font-semibold">
                  전화번호
                </p>
                <p className="text-base text-gray-700">
                  {hotel.hotelPhoneNumber}
                </p>
              </div>
            </div>

            {/* 호텔 옵션 정보 */}
            {hotel.hotelOptions && hotel.hotelOptions.size > 0 && (
              <div className="bg-white/50 p-6 rounded-lg shadow-sm">
                <h2 className="text-xl font-semibold text-gray-800 mb-4">
                  호텔 옵션
                </h2>
                <ul className="text-base text-gray-700 list-disc list-inside">
                  {[...hotel.hotelOptions].map((option) => (
                    <li key={option}>{option}</li>
                  ))}
                </ul>
              </div>
            )}

            {/* 사업자 정보 */}
            <div className="bg-white/50 p-6 rounded-lg shadow-sm">
              <h2 className="text-xl font-semibold text-gray-800 mb-4">
                운영 사업자 정보
              </h2>
              <div className="grid grid-cols-2 gap-4">
                <p className="text-base text-gray-700 font-semibold">
                  대표자명
                </p>
                <p className="text-base text-gray-700">{hotel.ownerName}</p>
                <p className="text-base text-gray-700 font-semibold">
                  사업자 등록번호
                </p>
                <p className="text-base text-gray-700">
                  {hotel.businessRegistrationNumber}
                </p>
                <p className="text-base text-gray-700 font-semibold">
                  사업 개시일
                </p>
                <p className="text-base text-gray-700">{hotel.startDate}</p>
              </div>
              {/* 사업자 상세 정보 보기 버튼 */}
              <div className="flex justify-end mt-4">
                <Link href={`/admin/business/${hotel.ownerId}`}>
                  <Button className="bg-blue-500 hover:bg-blue-600 text-white px-5 py-2 text-lg rounded">
                    사업자 상세 정보 보기
                  </Button>
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
