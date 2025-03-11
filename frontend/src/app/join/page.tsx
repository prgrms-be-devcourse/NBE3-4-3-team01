"use client";
import { useState, useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { join, sendSmsVerification, verifySmsCode } from "@/lib/api/auth/AuthApi";
import { getRoleFromCookie } from "@/lib/utils/CookieUtil";

export default function JoinPage() {
  const router = useRouter();
  const searchParams = useSearchParams();

  const provider = decodeURIComponent(searchParams.get("provider") || "");
  const oauthId = searchParams.get("oauthId") || "";

  const [formData, setFormData] = useState({
    email: "",
    name: "",
    phoneNumber: "",
    provider,
    oauthId,
    role: "USER",
    birthDate: "",
  });

  const [smsCode, setSmsCode] = useState("");
  const [isSendingSms, setIsSendingSms] = useState(false);
  const [isVerifyingSms, setIsVerifyingSms] = useState(false);
  const [smsVerified, setSmsVerified] = useState(false);
  const [smsMessage, setSmsMessage] = useState("");
  const [showSmsInput, setShowSmsInput] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  
  const [toast, setToast] = useState({ show: false, message: "", type: "" });

  const showToast = (message: string, type: string = "error") => {
    setToast({ show: true, message, type });
    setTimeout(() => {
      setToast({ show: false, message: "", type: "" });
    }, 3000);
  };

  // SMS 인증번호 발송
  const handleSendSms = async () => {
    if (!formData.phoneNumber) {
      showToast("전화번호를 입력해주세요.");
      return;
    }

    setIsSendingSms(true);
    setSmsMessage("");
    setErrorMessage("");
    setShowSmsInput(false);
    
    const result = await sendSmsVerification(formData.phoneNumber);
    
    setIsSendingSms(false);
    
    if (result.isSuccess && result.data?.resultCode === 'OK') {
      setShowSmsInput(true);
      setSmsMessage("인증번호가 발송되었습니다. 3분 내에 입력해주세요.");
      showToast("인증번호가 발송되었습니다.", "success");
    } else {
      const message = result.data?.message || "인증번호 발송에 실패했습니다.";
      setErrorMessage(message);
      showToast(message);
      setShowSmsInput(false);
    }
  };

  // SMS 인증번호 확인
  const handleVerifySms = async () => {
    if (!formData.phoneNumber || !smsCode) {
      alert("전화번호와 인증번호를 모두 입력해주세요.");
      return;
    }

    try {
      setIsVerifyingSms(true);
      setSmsMessage("");
      
      const response = await verifySmsCode(formData.phoneNumber, smsCode);
      
      if (response.data && response.data.success) {
        setSmsVerified(true);
        setSmsMessage(response.data.message || "인증이 완료되었습니다.");
      } else if (response.data) {
        setSmsVerified(false);
        setSmsMessage(response.data.message || "인증번호가 일치하지 않습니다.");
      } else {
        setSmsVerified(false);
        setSmsMessage("인증번호 확인에 실패했습니다.");
      }
    } catch (error) {
      console.error("SMS 인증 오류:", error);
      setSmsMessage("인증번호 확인 중 오류가 발생했습니다.");
    } finally {
      setIsVerifyingSms(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    // SMS 인증 확인
    if (!smsVerified) {
      alert("전화번호 인증을 완료해주세요.");
      return;
    }

    try {
      const response = await join({
        email: formData.email,
        name: formData.name,
        phoneNumber: formData.phoneNumber,
        role: formData.role,
        provider: formData.provider,
        oauthId: formData.oauthId,
        birthDate: formData.birthDate,
      });

      if (
        response &&
        (response.msg === "OK" || response.resultCode === "200")
      ) {
        // 회원가입 성공 후 쿠키 확인
        const roleData = getRoleFromCookie();
        if (roleData) {
          if (roleData.role === "ADMIN") {
            router.push("/admin");
          } else if (roleData.role === "BUSINESS") {
            if (roleData?.hasHotel) {
              router.push("/business/hotel/management");
            } else {
              router.push("/business/");
            }
          } else {
            // 일반 사용자는 홈으로
            router.push("/");
          }
        } else {
          // 쿠키가 없으면 로그인 페이지로 즉시 이동
          router.push("/login");
        }
      } else {
        alert(response.msg || "회원가입에 실패했습니다.");
      }
    } catch (error) {
      console.error("회원가입 실패:", error);
      alert("회원가입 중 오류가 발생했습니다.");
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      {/* 토스트 알림 */}
      {toast.show && (
        <div className="fixed top-4 left-1/2 transform -translate-x-1/2 z-50 animate-toast">
          <div className={`px-6 py-3 rounded-lg shadow-lg flex items-center space-x-2 ${
            toast.type === "error" ? "bg-red-500 text-white" : "bg-green-500 text-white"
          }`}>
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span>{toast.message}</span>
          </div>
        </div>
      )}

      <div className="max-w-[460px] w-full space-y-8 p-10 bg-white rounded-xl shadow-lg">
        <div className="text-center">
          <h2 className="text-4xl font-bold text-gray-900">회원가입</h2>
          <p className="mt-3 text-base text-gray-600">
            추가 정보를 입력해주세요
          </p>
        </div>

        <form onSubmit={handleSubmit} className="mt-8 space-y-6">
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">
                이메일 <span className="text-red-500">*</span>
              </label>
              <input
                type="email"
                value={formData.email}
                onChange={(e) => {
                  const value = e.target.value;
                  const emailRegex =
                    /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
                  const isValid = emailRegex.test(value);

                  setFormData({ ...formData, email: value });

                  const emailInput = e.target;
                  if (!isValid && value !== "") {
                    emailInput.setCustomValidity(
                      "올바른 이메일 형식이 아닙니다."
                    );
                  } else {
                    emailInput.setCustomValidity("");
                  }
                }}
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500"
                required
                pattern="[a-zA-Z0-9._\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,4}"
                placeholder="example@email.com"
              />
              <p className="mt-1 text-sm text-gray-500">
                예시: example@email.com
              </p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">
                이름
              </label>
              <input
                type="text"
                value={formData.name}
                onChange={(e) =>
                  setFormData({ ...formData, name: e.target.value })
                }
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">
                전화번호
              </label>
              <div className="flex space-x-2">
                <input
                  type="tel"
                  value={formData.phoneNumber}
                  onChange={(e) => {
                    const value = e.target.value;
                    let numericValue = value.replace(/[^0-9]/g, "");
                    
                    numericValue = numericValue.substring(0, 11);
                    
                    let formattedNumber = "";
                    if (numericValue.length > 0) {
                      formattedNumber += numericValue.substring(0, 3);
                      
                      if (numericValue.length > 3) {
                        formattedNumber += "-" + numericValue.substring(3, 7);
                      }
                      
                      if (numericValue.length > 7) {
                        formattedNumber += "-" + numericValue.substring(7, 11);
                      }
                    }

                    setFormData({ ...formData, phoneNumber: formattedNumber });
                    setSmsVerified(false);
                    setErrorMessage("");
                  }}
                  pattern="01[0-9]-[0-9]{3,4}-[0-9]{4}"
                  placeholder="010-0000-0000"
                  maxLength={13}
                  className={`mt-1 block w-full px-3 py-2 border ${
                    errorMessage ? "border-red-500" : "border-gray-300"
                  } rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500`}
                  required
                  disabled={smsVerified}
                />
                <button
                  type="button"
                  onClick={handleSendSms}
                  disabled={isSendingSms || smsVerified}
                  className="mt-1 whitespace-nowrap px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:bg-gray-400"
                >
                  {isSendingSms ? "발송 중..." : "인증번호 전송"}
                </button>
              </div>
              {errorMessage && (
                <p className="mt-2 text-sm text-red-600 bg-red-50 p-2 rounded-md border border-red-200">
                  {errorMessage}
                </p>
              )}
              {!errorMessage && (
                <p className="mt-1 text-sm text-gray-500">예시: 010-1234-5678</p>
              )}
            </div>

            {/* SMS 인증번호 입력 */}
            {showSmsInput && (
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  인증번호
                </label>
                <div className="flex space-x-2">
                  <input
                    type="text"
                    value={smsCode}
                    onChange={(e) => setSmsCode(e.target.value)}
                    className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500"
                    placeholder="인증번호 6자리"
                    maxLength={6}
                    disabled={smsVerified}
                  />
                  <button
                    type="button"
                    onClick={handleVerifySms}
                    disabled={isVerifyingSms || smsVerified || !smsCode}
                    className="mt-1 whitespace-nowrap px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:bg-gray-400"
                  >
                    {isVerifyingSms ? "확인 중..." : "확인"}
                  </button>
                </div>
                <p className={`mt-2 text-sm ${smsVerified ? "text-green-600 bg-green-50 p-2 rounded-md border border-green-200" : "text-blue-600 bg-blue-50 p-2 rounded-md border border-blue-200"}`}>
                  {smsMessage}
                </p>
              </div>
            )}

            <div>
              <label className="block text-sm font-medium text-gray-700">
                회원 유형
              </label>
              <select
                value={formData.role}
                onChange={(e) =>
                  setFormData({ ...formData, role: e.target.value })
                }
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500"
              >
                <option value="USER">일반 사용자</option>
                {/* <option value="BUSINESS">사업자</option> */}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700">
                생년월일 <span className="text-red-500">*</span>
              </label>
              <input
                type="date"
                value={formData.birthDate}
                onChange={(e) =>
                  setFormData({ ...formData, birthDate: e.target.value })
                }
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500"
                required
                max={new Date().toISOString().split("T")[0]}
              />
            </div>
          </div>

          <button
            type="submit"
            className="w-full flex justify-center py-3 px-4 border border-transparent rounded-md shadow-sm text-base font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:bg-gray-400"
            disabled={!smsVerified}
          >
            가입 완료
          </button>
        </form>
      </div>

      <style jsx>{`
        @keyframes slideDown {
          from {
            opacity: 0;
            transform: translate(-50%, -20px);
          }
          to {
            opacity: 1;
            transform: translate(-50%, 0);
          }
        }
        
        .animate-toast {
          animation: slideDown 0.3s ease-out forwards;
        }
      `}</style>
    </div>
  );
}
