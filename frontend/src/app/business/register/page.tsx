"use client";

import { useForm } from "react-hook-form";
import { registerBusiness } from "@/lib/api/business/BusinessRegisterApi";
import { BusinessRegistrationForm } from "@/lib/types/business/BusinessRequest";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import Loading from "@/components/hotellist/Loading";
import Navigation from "@/components/navigation/Navigation";
import { Card, CardContent } from "@/components/ui/card";
import { useRouter } from "next/navigation";
import { useState } from "react";

export default function RegisterBusiness() {
  const router = useRouter();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<BusinessRegistrationForm>();
  const [error, setError] = useState<string | null>(null);

  const onSubmit = async (data: BusinessRegistrationForm) => {
    try {
      await registerBusiness(data);
      alert("사업자 등록이 완료되었습니다. 다시 로그인 해주세요");
      document.cookie =
        "token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
      router.push("/login");
    } catch (error: any) {
      const msg = error.response?.data?.msg;
      if (msg) {
        alert(msg);
      } else {
        setError("사업자 등록에 실패했습니다.");
      }
    }
  };

  if (error) return <p className="text-center text-red-500">Error: {error}</p>;

  const inputStyle =
    "bg-white h-[42px] px-10 text-lg placeholder:text-lg [&::-webkit-calendar-picker-indicator]:hidden w-full";

  return (
    <div className="relative min-h-screen bg-background">
      {/* Background gradient */}
      <div className="absolute inset-0 bg-gradient-to-b from-blue-100 to-white" />

      {/* Decorative circles */}
      <div className="absolute top-20 right-20 w-64 h-64 bg-blue-200 rounded-full blur-3xl opacity-20" />
      <div className="absolute bottom-20 left-20 w-96 h-96 bg-blue-300 rounded-full blur-3xl opacity-10" />

      <div className="relative z-10">
        <Navigation />

        <div className="container mx-auto px-4 pt-32">
          <div className="text-center mb-12">
            <h1 className="text-4xl font-bold text-gray-800 mb-4">
              사업자 등록
            </h1>
            <p className="text-xl text-gray-600">
              호텔 등록을 위한 첫 단계를 시작해보세요
            </p>
          </div>

          <div className="max-w-2xl mx-auto">
            <Card className="bg-white/50 shadow-lg">
              <CardContent className="p-8">
                {isSubmitting ? (
                  <div className="flex justify-center">
                    <Loading />
                  </div>
                ) : (
                  <form onSubmit={handleSubmit(onSubmit)} className="space-y-8">
                    {/* 사업자 등록번호 입력 */}
                    <div>
                      <label
                        htmlFor="businessRegistrationNumber"
                        className="block font-medium text-gray-700 text-lg mb-2"
                      >
                        사업자 등록번호
                      </label>
                      <Input
                        id="businessRegistrationNumber"
                        type="text"
                        {...register("businessRegistrationNumber", {
                          required: "사업자 등록 번호는 필수입니다.",
                          pattern: {
                            value: /^[0-9]{10}$/,
                            message:
                              "사업자 등록 번호는 10자리 숫자여야 합니다.",
                          },
                        })}
                        className={`${inputStyle} w-full h-12 ${
                          errors?.businessRegistrationNumber
                            ? "border-red-500"
                            : ""
                        }`}
                        placeholder="사업자 등록번호 (10자리)"
                      />
                      {errors.businessRegistrationNumber && (
                        <p
                          className="mt-2 text-sm text-red-600"
                          id="businessRegistrationNumber-error"
                        >
                          {errors.businessRegistrationNumber.message}
                        </p>
                      )}
                    </div>

                    {/* 개업 일자 입력 */}
                    <div>
                      <label
                        htmlFor="startDate"
                        className="block font-medium text-gray-700 text-lg mb-2"
                      >
                        개업 일자
                      </label>
                      <Input
                        id="startDate"
                        type="date"
                        {...register("startDate", {
                          required: "개업 일자는 필수입니다.",
                        })}
                        className={`${inputStyle} w-full h-12 ${
                          errors?.startDate ? "border-red-500" : ""
                        }`}
                      />
                      {errors.startDate && (
                        <p
                          className="mt-2 text-sm text-red-600"
                          id="startDate-error"
                        >
                          {errors.startDate.message}
                        </p>
                      )}
                    </div>

                    {/* 대표자명 입력 */}
                    <div>
                      <label
                        htmlFor="ownerName"
                        className="block font-medium text-gray-700 text-lg mb-2"
                      >
                        대표자명
                      </label>
                      <Input
                        id="ownerName"
                        type="text"
                        {...register("ownerName", {
                          required: "대표자명은 필수입니다.",
                          maxLength: {
                            value: 30,
                            message: "최대 30자까지 가능합니다.",
                          },
                        })}
                        className={`${inputStyle} w-full h-12 ${
                          errors?.ownerName ? "border-red-500" : ""
                        }`}
                        placeholder="대표자명을 입력해주세요"
                      />
                      {errors.ownerName && (
                        <p
                          className="mt-2 text-sm text-red-600"
                          id="ownerName-error"
                        >
                          {errors.ownerName.message}
                        </p>
                      )}
                    </div>

                    {/* 등록 버튼 */}
                    <div className="pt-4">
                      <Button
                        type="submit"
                        className="w-full h-14 bg-blue-500 hover:bg-blue-600 text-white rounded-lg text-lg font-semibold shadow-md focus:outline-none focus:ring-2 focus:ring-blue-400"
                        disabled={isSubmitting}
                      >
                        {isSubmitting ? "등록 중..." : "사업자 등록하기"}
                      </Button>
                    </div>
                  </form>
                )}
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}
