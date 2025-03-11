"use client";

import Loading from "@/components/hotellist/Loading";
import Navigation from "@/components/navigation/Navigation";
import Pagination from "@/components/pagination/Pagination";
import { Button } from "@/components/ui/button";
import { getAllHotelsForAdmin } from "@/lib/api/admin/AdminHotelApi";
import { AdminHotelSummaryReponse } from "@/lib/types/admin/response/AdminHotelResponse";
import { HotelStatus } from "@/lib/types/hotel/HotelStatus";
import { PageDto } from "@/lib/types/PageDto";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";

export default function AdminHotelsPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const pageParam = searchParams.get("page");
  const statusParam = searchParams.get("status");
  const selectedStatus = statusParam as HotelStatus | null;
  const currentPage =
    pageParam !== null && !isNaN(Number(pageParam)) ? Number(pageParam) - 1 : 0;
  const [hotels, setHotels] = useState<PageDto<AdminHotelSummaryReponse> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchHotels = async () => {
      try {
        const data = await getAllHotelsForAdmin(currentPage, selectedStatus);
        setHotels(data);
      } catch (err) {
        setError((err as Error).message);
      } finally {
        setLoading(false);
      }
    };
    fetchHotels();
  }, [currentPage, selectedStatus]);

    const handleFilterChange = (status: HotelStatus | null) => {
      const queryParams = new URLSearchParams();
      queryParams.set("page", "1");
  
      if (status) queryParams.set("status", status);
  
      router.push(`/admin/hotels?${queryParams.toString()}`);
    };

  if (loading) return <Loading />;
  if (error) return <p className="text-center text-red-500">Error: {error}</p>;

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
          {/* Hero Section */}
          <div className="text-center mt-20 mb-12">
            <h1 className="text-4xl font-bold text-gray-800 mb-4">
              호텔 관리
            </h1>
            <p className="text-lg text-gray-600 mb-8">
              등록된 호텔 정보를 확인하고 관리하세요
            </p>
          </div>
          {/* ✅ 승인 상태 필터 버튼 */}
          <div className="flex justify-center space-x-2 mb-6">
            <Button
              variant={selectedStatus === null ? "default" : "outline"}
              onClick={() => handleFilterChange(null)}
            >
              전체
            </Button>
            <Button
              variant={selectedStatus === "PENDING" ? "default" : "outline"}
              onClick={() => handleFilterChange(HotelStatus.PENDING)}
            >
              승인 대기
            </Button>
            <Button
              variant={selectedStatus === "AVAILABLE" ? "default" : "outline"}
              onClick={() => handleFilterChange(HotelStatus.AVAILABLE)}
            >
              이용 가능
            </Button>
            <Button
              variant={selectedStatus === "UNAVAILABLE" ? "default" : "outline"}
              onClick={() => handleFilterChange(HotelStatus.UNAVAILABLE)}
            >
              이용 불가
            </Button>
          </div>
          {/* 호텔 리스트 */}
          <div className="w-full max-w-[75rem] mx-auto">
            <div className="grid grid-cols-1 gap-4">
              {hotels?.items.length === 0 ? (
                <div className="bg-white/50 p-6 rounded-lg shadow-sm text-center">
                  <p className="text-gray-600">등록된 호텔이 없습니다.</p>
                </div>
              ) : (
                hotels?.items.map((hotel) => (
                  <div
                    key={hotel.hotelId}
                    className="flex items-center justify-between p-4 bg-white/50 rounded-lg shadow-sm"
                  >
                    {/* 호텔 핵심 정보 */}
                    <div className="flex items-center space-x-4">
                      <div className="w-20 text-sm text-gray-500">
                        {hotel.hotelId}
                      </div>
                      <div className="w-40">
                        <h3 className="text-lg font-semibold text-gray-800 truncate">
                          {hotel.name}
                        </h3>
                      </div>
                      <div className="w-32 text-sm text-gray-500 truncate">
                        {hotel.streetAddress}
                      </div>
                      <div className="w-24 text-sm text-gray-500 truncate">
                        {hotel.ownerName}
                      </div>
                    </div>
                    {/* 액션 버튼 */}
                    <div className="flex items-center space-x-2">
                      <div className="text-sm text-gray-600 mr-4">
                        {hotel.status}
                      </div>
                      <Link href={`/admin/hotels/${hotel.hotelId}`}>
                        <Button
                          variant="default"
                          className="bg-blue-500 hover:bg-blue-600"
                        >
                          관리
                        </Button>
                      </Link>
                    </div>
                  </div>
                ))
              )}
            </div>
            {/* 페이지네이션 */}
            {hotels && hotels.totalPages > 1 && (
              <div className="mt-12 flex justify-center">
                <Pagination
                  currentPage={currentPage + 1}
                  totalPages={hotels.totalPages}
                  basePath="/admin/hotels"
                />
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
