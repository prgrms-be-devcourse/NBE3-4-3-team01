"use client";

import Navigation from "@/components/navigation/Navigation";

export default function AdminMainPage() {
  return (
    <>
      <Navigation />
      <main className="flex flex-col items-center justify-center min-h-[80vh] bg-gradient-to-b from-blue-50 to-white text-center">
        <div className="bg-white shadow-lg rounded-2xl p-8 max-w-lg w-full border border-gray-200 animate-fade-in">
          <h1 className="text-4xl font-extrabold text-gray-800">
            관리자 페이지
          </h1>
          <p className="text-lg text-gray-600 mt-3">
            이곳은{" "}
            <span className="font-semibold text-blue-600">관리자 전용</span>{" "}
            페이지입니다. <br />
            상단 메뉴를 이용해 원하는 기능을 사용하세요.
          </p>
        </div>
      </main>
    </>
  );
}
