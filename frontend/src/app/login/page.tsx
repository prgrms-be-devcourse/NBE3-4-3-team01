"use client";
import Image from "next/image";

export default function LoginPage() {
  const handleOAuthLogin = (provider: string) => {
    window.location.href = `http://localhost:8080/oauth2/authorization/${provider}`;
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-[460px] w-full space-y-8 p-10 bg-white rounded-xl shadow-lg">
        <div className="text-center">
          <h2 className="mt-6 text-4xl font-bold text-gray-900">로그인</h2>
          <p className="mt-3 text-base text-gray-600">
            소셜 계정으로 간편하게 로그인하세요
          </p>
        </div>

        <div className="mt-10 space-y-5">
          <button
            onClick={() => handleOAuthLogin("kakao")}
            className="w-full flex items-center justify-center h-[52px] border border-transparent rounded-xl text-base font-medium text-gray-800 bg-[#FEE500] hover:bg-[#FEE500]/90"
          >
            <Image
              src="/images/kakao.png"
              alt="Kakao Login"
              width={28}
              height={28}
              className="mr-3"
            />
            카카오로 시작하기
          </button>

          <button
            onClick={() => handleOAuthLogin("naver")}
            className="w-full flex items-center justify-center h-[52px] border border-transparent rounded-xl text-base font-medium text-white bg-[#03C75A] hover:bg-[#03C75A]/90"
          >
            <Image
              src="/images/naver.png"
              alt="Naver Login"
              width={28}
              height={28}
              className="mr-3"
            />
            네이버로 시작하기
          </button>

          <button
            onClick={() => handleOAuthLogin("google")}
            className="w-full flex items-center justify-center h-[52px] border border-gray-300 rounded-xl text-base font-medium text-gray-700 bg-white hover:bg-gray-50"
          >
            <Image
              src="/images/google.png"
              alt="Google Login"
              width={28}
              height={28}
              className="mr-3"
            />
            Google로 시작하기
          </button>
        </div>
      </div>
    </div>
  );
}
