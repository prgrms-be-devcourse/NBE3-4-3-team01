"use client";

import Navigation from "@/components/navigation/Navigation";
import SearchComponent from "@/components/search/SearchComponent";
import { Building2, MapPin, Star, Waves } from "lucide-react";

export default function Page() {
  return (
    <div className="relative min-h-screen bg-background overflow-x-hidden">
      {/* Background gradient */}
      <div className="fixed inset-0 bg-gradient-to-b from-blue-100 to-white" />

      {/* Decorative circles */}
      <div className="fixed top-10 right-10 md:top-20 md:right-20 w-32 h-32 md:w-64 md:h-64 bg-blue-200 rounded-full blur-3xl opacity-20" />
      <div className="fixed bottom-10 left-10 md:bottom-20 md:left-20 w-48 h-48 md:w-96 md:h-96 bg-blue-300 rounded-full blur-3xl opacity-10" />

      <div className="relative z-10 w-full">
        <Navigation />

        <div className="content-wrapper container mx-auto px-4 max-w-7xl pb-20">
          {/* Hero Section */}
          <div className="text-center mt-8 sm:mt-12 md:mt-10 mb-6 sm:mb-8 md:mb-12">
            <h1 className="text-2xl sm:text-3xl md:text-4xl font-bold text-gray-800 mb-2 sm:mb-3 md:mb-4 px-4">
              서울에서 완벽한 호텔을 찾아보세요
            </h1>
            <p className="text-base sm:text-lg text-gray-600 mb-4 sm:mb-6 md:mb-8 px-4">
              최고의 위치, 최상의 서비스로 특별한 경험을 제공합니다
            </p>
          </div>

          {/* Search Component */}
          <div className="relative w-full max-w-[90%] sm:max-w-[85%] md:max-w-[80rem] mx-auto">
            <SearchComponent />
          </div>

          {/* Features Section */}
          <div
            className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 
                        gap-4 sm:gap-6 md:gap-8 
                        mt-16 sm:mt-24 md:mt-32 
                        mb-8 sm:mb-12 md:mb-16 
                        px-4"
          >
            {[
              {
                icon: Building2,
                title: "다양한 호텔",
                desc: "1성급부터 5성급까지 다양한 등급",
              },
              {
                icon: MapPin,
                title: "최적의 위치",
                desc: "서울 곳곳의 편리한 위치",
              },
              {
                icon: Star,
                title: "검증된 평점",
                desc: "실제 이용객의 솔직한 리뷰",
              },
            ].map((feature, idx) => (
              <div
                key={idx}
                className="flex flex-col items-center text-center p-4 sm:p-5 md:p-6 
                          bg-white/50 rounded-lg shadow-sm"
              >
                <feature.icon className="w-8 h-8 sm:w-10 sm:h-10 md:w-12 md:h-12 text-blue-500 mb-3 md:mb-4" />
                <h3 className="text-base sm:text-lg font-semibold text-gray-800 mb-1 sm:mb-2">
                  {feature.title}
                </h3>
                <p className="text-sm sm:text-base text-gray-600">
                  {feature.desc}
                </p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
