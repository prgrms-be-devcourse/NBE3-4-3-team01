"use client";

import Loading from "@/components/hotellist/Loading";
import Navigation from "@/components/navigation/Navigation";
import Pagination from "@/components/pagination/Pagination";
import { Button } from "@/components/ui/button";
import { getAllBusinesses } from "@/lib/api/admin/AdminBusinessApi";
import { AdminBusinessSummaryReponse } from "@/lib/types/admin/response/AdminBusinessResponse";
import { PageDto } from "@/lib/types/PageDto";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";
export default function AdminBusinessesPage() {
  const searchParams = useSearchParams();
  const pageParam = searchParams.get("page");
  const currentPage =
    pageParam !== null && !isNaN(Number(pageParam)) ? Number(pageParam) - 1 : 0;

  const [businesses, setBusinesses] =
    useState<PageDto<AdminBusinessSummaryReponse> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchBusinesses = async () => {
      try {
        const data = await getAllBusinesses(currentPage);
        setBusinesses(data);
      } catch (err) {
        setError((err as Error).message);
      } finally {
        setLoading(false);
      }
    };

    fetchBusinesses();
  }, [currentPage]);

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
              사업자 관리
            </h1>
            <p className="text-lg text-gray-600 mb-8">
              등록된 사업자 정보를 확인하고 관리하세요
            </p>
          </div>
          {/* 사업자 리스트 */}
          <div className="w-full max-w-[75rem] mx-auto">
            <div className="grid grid-cols-1 gap-4">
              {businesses?.items.length === 0 ? (
                <div className="bg-white/50 p-6 rounded-lg shadow-sm text-center">
                  <p className="text-gray-600">등록된 사업자가 없습니다.</p>
                </div>
              ) : (
                businesses?.items.map((business) => (
                  <div
                    key={business.businessId}
                    className="flex items-center justify-between p-4 bg-white/50 rounded-lg shadow-sm"
                  >
                    {/* 사업자 핵심 정보 */}
                    <div className="flex items-center space-x-4">
                      <div className="w-20 text-sm text-gray-500">
                        {business.businessId}
                      </div>
                      <div className="w-40">
                        <h3 className="text-lg font-semibold text-gray-800 truncate">
                          {business.ownerName}
                        </h3>
                      </div>
                      <div className="w-32 text-sm text-gray-500 truncate">
                        {business.contact || "미등록"}
                      </div>
                      <div className="w-32 text-sm text-gray-500 truncate">
                        {business.hotelName || "미등록"}
                      </div>
                    </div>
                    {/* 액션 버튼 */}
                    <div className="flex items-center space-x-2">
                      <div className="text-sm text-gray-600 mr-4">
                        {business.approvalStatus}
                      </div>
                      <Link href={`/admin/business/${business.businessId}`}>
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
            {businesses && businesses.totalPages > 1 && (
              <div className="mt-12 flex justify-center">
                <Pagination
                  currentPage={currentPage + 1}
                  totalPages={businesses.totalPages}
                  basePath="/admin/business"
                />
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
