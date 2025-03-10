"use client";
import { useState } from "react";
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

  // SMS 인증 관련 상태
  const [smsCode, setSmsCode] = useState("");
  const [isSendingSms, setIsSendingSms] = useState(false);
  const [isVerifyingSms, setIsVerifyingSms] = useState(false);
  const [smsVerified, setSmsVerified] = useState(false);
  const [smsMessage, setSmsMessage] = useState("");
  const [phoneError, setPhoneError] = useState("");

  // SMS 인증번호 발송
  const handleSendSms = async () => {
    if (!formData.phoneNumber) {
      setPhoneError("전화번호를 입력해주세요.");
      return;
    }

    try {
      setIsSendingSms(true);
      setSmsMessage("");
      setPhoneError("");
      
      const response = await sendSmsVerification(formData.phoneNumber);
      console.log("SMS 발송 응답:", response);
      
      if (response.data && response.data.success) {
        setSmsMessage(response.data.message || "인증번호가 발송되었습니다. 3분 내에 입력해주세요.");
      } else if (response.data) {
        if (response.data.message && response.data.message.includes("더 이상 가입할 수 없습니다")) {
          setPhoneError(response.data.message);
          setSmsMessage("");
        } else {
          setSmsMessage(response.data.message || "인증번호 발송에 실패했습니다.");
        }
      } else {
        setSmsMessage("인증번호 발송에 실패했습니다.");
      }
    } catch (error) {
      console.error("SMS 발송 오류:", error);
      setSmsMessage("인증번호 발송 중 오류가 발생했습니다.");
    } finally {
      setIsSendingSms(false);
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
      console.log("SMS 인증 응답:", response);
      
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

      console.log("회원가입 응답:", response);

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
                    const formattedNumber = value
                      .replace(/[^0-9]/g, "")
                      .replace(/^(\d{0,3})(\d{0,4})(\d{0,4})$/g, "$1-$2-$3")
                      .replace(/(\-{1,2})$/g, "");

                    setFormData({ ...formData, phoneNumber: formattedNumber });
                    setSmsVerified(false);
                    setPhoneError("");
                  }}
                  pattern="01[0-9]-[0-9]{3,4}-[0-9]{4}"
                  placeholder="010-0000-0000"
                  className={`mt-1 block w-full px-3 py-2 border ${
                    phoneError ? "border-red-500" : "border-gray-300"
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
              {phoneError ? (
                <p className="mt-1 text-sm text-red-600">{phoneError}</p>
              ) : (
                <p className="mt-1 text-sm text-gray-500">예시: 010-1234-5678</p>
              )}
            </div>

            {/* SMS 인증번호 입력 */}
            <div className={smsMessage ? "block" : "hidden"}>
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
              <p className={`mt-1 text-sm ${smsVerified ? "text-green-500" : smsMessage.includes("실패") ? "text-red-500" : "text-blue-500"}`}>
                {smsMessage}
              </p>
            </div>

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
    </div>
  );
}
