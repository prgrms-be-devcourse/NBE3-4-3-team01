"use client";

import Loading from "@/components/hotellist/Loading";
import Navigation from "@/components/navigation/Navigation";
import { Button } from "@/components/ui/button";
import { getBusiness, modifyBusiness } from "@/lib/api/admin/AdminBusinessApi";
import { AdminBusinessRequest } from "@/lib/types/admin/request/AdminBusinessRequest";
import { AdminBusinessDetailResponse } from "@/lib/types/admin/response/AdminBusinessResponse";
import { BusinessApprovalStatus } from "@/lib/types/business/BusinessApprovalStatus";
import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";

export default function AdminBusinessDetailPage() {
  const { businessId } = useParams();
  const [business, setBusiness] = useState<AdminBusinessDetailResponse | null>(
    null
  );
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [status, setStatus] = useState<BusinessApprovalStatus | null>(null);

  useEffect(() => {
    if (!businessId) return;

    const fetchBusiness = async () => {
      try {
        const data = await getBusiness(Number(businessId));
        setBusiness(data);
        setStatus(data.approvalStatus as BusinessApprovalStatus);
      } catch (err) {
        setError((err as Error).message);
      } finally {
        setLoading(false);
      }
    };

    fetchBusiness();
  }, [businessId]);

  const handleSave = async () => {
    if (!businessId || !status) return;

    try {
      const updatedData: AdminBusinessRequest = {
        businessApprovalStatus: status,
      };

      await modifyBusiness(Number(businessId), updatedData);

      setBusiness((prev) =>
        prev ? { ...prev, approvalStatus: status } : prev
      );
      alert("승인 상태가 저장되었습니다.");
    } catch (err) {
      setError("사업자 상태를 저장하는 중 오류가 발생했습니다.");
    }
  };

  if (!businessId)
    return (
      <p className="text-red-500 text-center">
        Error: Business ID is missing from the URL.
      </p>
    );

  if (loading) return <Loading />;
  if (error) return <p className="text-center text-red-500">Error: {error}</p>;
  if (!business)
    return <p className="text-center text-gray-500">No business found</p>;

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
            <h1 className="text-4xl font-bold text-gray-800">
              사업자 상세 정보
            </h1>
            <p className="text-lg text-gray-600 mt-2">
              사업자의 정보를 확인하고 승인 상태를 변경하세요.
            </p>
          </div>

          {/* 사업자 정보 박스 */}
          <div className="w-full max-w-[75rem] mx-auto space-y-6">
            {/* 저장 및 목록으로 돌아가기 버튼 */}
            <div className="w-full flex justify-end space-x-4 mb-2">
              <Button
                onClick={handleSave}
                className="bg-blue-500 hover:bg-blue-600 text-white px-5 py-2 text-lg rounded"
              >
                저장
              </Button>
              <Link href="/admin/business">
                <Button className="bg-gray-300 hover:bg-gray-400 text-gray-800 px-5 py-2 text-lg rounded">
                  목록으로 돌아가기
                </Button>
              </Link>
            </div>
            {/* 사업자 등록 정보 */}
            <div className="bg-white/50 p-6 rounded-lg shadow-sm">
              <h2 className="text-xl font-semibold text-gray-800 mb-4">
                사업자 등록 정보
              </h2>
              <div className="grid grid-cols-2 gap-4">
                <p className="text-base text-gray-700 font-semibold">
                  사업자 ID
                </p>
                <p className="text-base text-gray-700">{business.businessId}</p>
                <p className="text-base text-gray-700 font-semibold">
                  사업자 등록번호
                </p>
                <p className="text-base text-gray-700">
                  {business.businessRegistrationNumber}
                </p>
                <p className="text-base text-gray-700 font-semibold">
                  대표자명
                </p>
                <p className="text-base text-gray-700">{business.ownerName}</p>
                <p className="text-base text-gray-700 font-semibold">
                  사업 개시일
                </p>
                <p className="text-base text-gray-700">{business.startDate}</p>
              </div>
            </div>

            {/* 승인 상태 변경 */}
            <div className="bg-white/50 p-6 rounded-lg shadow-sm flex items-center justify-between">
              <p className="text-base text-gray-700 font-semibold">승인 상태</p>
              <select
                value={status ?? business.approvalStatus}
                onChange={(e) =>
                  setStatus(e.target.value as BusinessApprovalStatus)
                }
                className="border p-2 rounded bg-white text-base w-60"
              >
                <option value="PENDING">대기</option>
                <option value="APPROVED">승인</option>
                <option value="REJECTED">거절</option>
              </select>
            </div>

            {/* 회원 정보 */}
            <div className="bg-white/50 p-6 rounded-lg shadow-sm">
              <h2 className="text-xl font-semibold text-gray-800 mb-4">
                회원 정보
              </h2>
              <div className="grid grid-cols-2 gap-4">
                <p className="text-base text-gray-700 font-semibold">
                  회원 이메일
                </p>
                <p className="text-base text-gray-700">
                  {business.memberEmail}
                </p>
                <p className="text-base text-gray-700 font-semibold">
                  회원 이름
                </p>
                <p className="text-base text-gray-700">{business.memberName}</p>
                <p className="text-base text-gray-700 font-semibold">
                  회원 연락처
                </p>
                <p className="text-base text-gray-700">
                  {business.memberPhoneNumber}
                </p>
                <p className="text-base text-gray-700 font-semibold">
                  회원 상태
                </p>
                <p className="text-base text-gray-700">
                  {business.memberStatus}
                </p>
              </div>
            </div>

            {/* 운영 호텔 정보 */}
            {business.hotelName && (
              <div className="bg-white/50 p-6 rounded-lg shadow-sm">
                <h2 className="text-xl font-semibold text-gray-800 mb-4">
                  운영 호텔 정보
                </h2>
                <div className="grid grid-cols-2 gap-4">
                  <p className="text-base text-gray-700 font-semibold">
                    호텔명
                  </p>
                  <p className="text-base text-gray-700">
                    {business.hotelName}
                  </p>
                  <p className="text-base text-gray-700 font-semibold">
                    호텔 상태
                  </p>
                  <p className="text-base text-gray-700">
                    {business.hotelStatus}
                  </p>
                </div>
                <div className="flex justify-end mt-4">
                  <Link href={`/admin/hotels/${business.hotelId}`}>
                    <Button className="bg-blue-500 hover:bg-blue-600 text-white px-5 py-2 text-lg rounded w-52">
                      호텔 상세보기
                    </Button>
                  </Link>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
