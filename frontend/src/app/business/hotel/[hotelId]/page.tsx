"use client";

import {
  findAllHotelOptions,
  findHotelDetail,
  modifyHotel,
  saveHotelImageUrls,
} from "@/lib/api/hotel/BusinessHotelApi";
import { PutHotelRequest } from "@/lib/types/hotel/PutHotelRequest";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { PresignedUrlsResponse } from "@/lib/types/review/PresignedUrlsResponse";
import { PutHotelResponse } from "@/lib/types/hotel/PutHotelResponse";
import { uploadImagesToS3 } from "@/lib/api/aws/AwsS3Api";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import DatePicker from "react-datepicker";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { MoveLeft, Star, XCircle } from "lucide-react";
import "react-datepicker/dist/react-datepicker.css";
import { GetAllHotelOptionResponse } from "@/lib/types/hotel/GetAllHotelOptionResponse";
import Navigation from "@/components/navigation/Navigation";

export default function ModifyHotelPage() {
  const params = useParams();
  const router = useRouter();
  const [hotelId, setHotelId] = useState(Number(params.hotelId));
  const [hotelName, setHotelName] = useState("");
  const [hotelEmail, setHotelEmail] = useState("");
  const [hotelPhoneNumber, setHotelPhoneNumber] = useState("");
  const [streetAddress, setStreetAddress] = useState("");
  const [zipCode, setZipCode] = useState("");
  const [hotelGrade, setHotelGrade] = useState(1);
  const [hoverGrade, setHoverGrade] = useState<number | null>(null);
  const [checkInTime, setCheckInTime] = useState<string>("");
  const [checkOutTime, setCheckOutTime] = useState<string>("");
  const [hotelExplainContent, setHotelExplainContent] = useState("");
  const [hotelStatus, setHotelStatus] = useState("AVAILABLE");
  const [existingImages, setExistingImages] = useState<string[]>([]);
  const [deleteImageUrls, setDeleteImageUrls] = useState<string[]>([]);
  const [images, setImages] = useState<File[]>([]);
  const [imagePreviews, setImagePreviews] = useState<string[]>([]);
  const [imageExtensions, setImageExtensions] = useState<string[]>([]);
  const [presignedUrls, setPresignedUrls] = useState<string[]>([]);
  const [hotelOptions, setHotelOptions] = useState<Set<string>>(new Set());
  const [availableHotelOptions, setAvailableHotelOptions] = useState<string[]>(
    []
  );

  useEffect(() => {
    const fetchHotelData = async () => {
      try {
        const response = await findHotelDetail(Number(params.hotelId));
        setHotelId(response.hotelDetailDto.hotelId);
        setHotelName(response.hotelDetailDto.hotelName);
        setHotelEmail(response.hotelDetailDto.hotelEmail);
        setHotelPhoneNumber(response.hotelDetailDto.hotelPhoneNumber);
        setStreetAddress(response.hotelDetailDto.streetAddress);
        setZipCode(response.hotelDetailDto.zipCode.toString());
        setHotelGrade(response.hotelDetailDto.hotelGrade);
        setCheckInTime(response.hotelDetailDto.checkInTime);
        setCheckOutTime(response.hotelDetailDto.checkOutTime);
        setHotelExplainContent(response.hotelDetailDto.hotelExplainContent);
        setHotelStatus(response.hotelDetailDto.hotelStatus);
        setExistingImages(response.hotelImageUrls);
        setHotelOptions(new Set(response.hotelDetailDto.hotelOptions || []));
        console.log(response);
        console.log(response);
      } catch (error) {
        throw error;
      }
    };

    fetchHotelData();
  }, [params.hotelId]);

  // 호텔 옵션 전체 리스트 가져오기
  useEffect(() => {
    const loadHotelOptions = async () => {
      try {
        const options: GetAllHotelOptionResponse = await findAllHotelOptions();
        setAvailableHotelOptions(options.hotelOptions.sort());
      } catch (error) {
        throw error;
      }
    };
    loadHotelOptions();
  }, []);

  const handleOptionChange = (option: string) => {
    setHotelOptions((prev) => {
      const newOptions = new Set(prev);
      if (newOptions.has(option)) {
        newOptions.delete(option);
      } else {
        newOptions.add(option);
      }

      return newOptions;
    });
  };

  const handleImageDelete = (imageUrl: string) => {
    setExistingImages(existingImages.filter((img) => img !== imageUrl));
    setDeleteImageUrls([...deleteImageUrls, imageUrl]);
  };

  const handlenewImageDelete = (index: number) => {
    setImages(images.filter((_, i) => i !== index));
    setImagePreviews(imagePreviews.filter((_, i) => i !== index));
    setImageExtensions(imageExtensions.filter((_, i) => i !== index));
  };

  const handleNewImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const files = Array.from(e.target.files);
      setImages([...images, ...files]);

      const newPreviews = files.map((file) => URL.createObjectURL(file));
      setImagePreviews([...imagePreviews, ...newPreviews]);

      const extensions = files.map((file) => {
        const ext = file.name.split(".").pop()?.toLowerCase() || "";
        return ext;
      });

      setImageExtensions([...imageExtensions, ...extensions]);
    }
  };

  // ✅ Date 객체 → HH:mm 문자열 변환 함수
  const formatTimeToHHMM = (date: Date | null): string => {
    if (!date) return "";
    return date.toLocaleTimeString("ko-KR", {
      hour: "2-digit",
      minute: "2-digit",
      hour12: false,
    });
  };

  // ✅ HH:mm 문자열 → Date 객체 변환 함수
  const parseTimeToDate = (timeString: string): Date | null => {
    if (!timeString) return null;
    const [hours, minutes] = timeString.split(":").map(Number);
    const date = new Date();
    date.setHours(hours, minutes, 0, 0);
    return date;
  };

  const formatPhoneNumber = (value: string) => {
    value = value.replace(/\D/g, "");
    if (value.length <= 3) return value;
    if (value.length <= 7) return `${value.slice(0, 3)}-${value.slice(3)}`;
    return `${value.slice(0, 3)}-${value.slice(3, 7)}-${value.slice(7, 11)}`;
  };

  const handlePhoneNumberChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setHotelPhoneNumber(formatPhoneNumber(e.target.value));
  };

  useEffect(() => {
    return () => {
      imagePreviews.forEach((preview) => URL.revokeObjectURL(preview));
    };
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!hotelId) {
      alert("유효하지 않은 호텔 ID입니다.");
      console.error("유효하지 않은 호텔 ID입니다.");
      return;
    }

    const requestBody: PutHotelRequest = {
      hotelName,
      hotelEmail,
      hotelPhoneNumber,
      streetAddress,
      zipCode: Number(zipCode),
      hotelGrade,
      checkInTime,
      checkOutTime,
      hotelExplainContent,
      hotelStatus,
      deleteImageUrls,
      imageExtensions: imageExtensions,
      hotelOptions: Array.from(hotelOptions),
    };

    try {
      const response: PutHotelResponse = await modifyHotel(
        hotelId,
        requestBody
      );
      console.log("response: ", response);
      const presignedUrlsResponse: PresignedUrlsResponse =
        response.urlsResponse;
      console.log("서버 응답:", presignedUrlsResponse);

      setPresignedUrls(presignedUrlsResponse.presignedUrls);
      console.log(presignedUrls);
      alert("호텔이 성공적으로 수정되었습니다.");
      router.push("/business/hotel/management");
    } catch (error) {
      alert(error);
    }
  };

  useEffect(() => {
    if (presignedUrls.length > 0) {
      console.log("✅ presignedUrls 설정됨:", presignedUrls);
      submitImages();
    }
  }, [presignedUrls]);

  const submitImages = async () => {
    try {
      await uploadImagesToS3(presignedUrls, images);
      await saveImageUrls();
      console.log("이미지가 성공적으로 업로드되었습니다.");
    } catch (error) {
      console.error(error);
      alert(error);
    }
  };

  const saveImageUrls = async () => {
    const urls = presignedUrls.map((presignedUrls) => {
      console.log(presignedUrls);
      return presignedUrls.split("?")[0];
    });

    try {
      saveHotelImageUrls(hotelId, urls);
      console.info("이미지 URL이 성공적으로 저장되었습니다.");
    } catch (error) {
      console.error("Error: ", error);
      alert(error);
    }
  };

  return (
    <div className="p-6 flex justify-center pt-[100px]">
      <Navigation />
      <Card className="w-full max-w-3xl">
        <CardHeader className="border-b pb-4">
          <div className="flex items-center justify-between w-full">
            <div className="flex items-center gap-4">
              <Button
                variant="ghost"
                onClick={() => router.back()}
                className="hover:bg-gray-100 gap-2"
              >
                <MoveLeft className="w-5 h-5 text-gray-600" />
                <span className="text-gray-600">뒤로가기</span>
              </Button>
            </div>
            <CardTitle className="absolute left-1/2 transform -translate-x-1/2 text-2xl font-semibold">
              호텔 수정
            </CardTitle>
          </div>
        </CardHeader>
        <CardContent className="p-6">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* 호텔 이름 */}
            <div>
              <Label htmlFor="hotelName" className="text-lg font-semibold">
                호텔 이름
              </Label>
              <Input
                id="hotelName"
                value={hotelName}
                onChange={(e) => setHotelName(e.target.value)}
                required
                className="w-full border p-3 rounded-md"
              />
            </div>

            {/* 이메일 */}
            <div>
              <Label htmlFor="hotelEmail" className="text-lg font-semibold">
                이메일
              </Label>
              <Input
                id="hotelEmail"
                value={hotelEmail}
                onChange={(e) => setHotelEmail(e.target.value)}
                required
                className="w-full border p-3 rounded-md"
              />
            </div>

            {/* 전화번호 */}
            <div>
              <Label
                htmlFor="hotelPhoneNumber"
                className="text-lg font-semibold"
              >
                전화번호
              </Label>
              <Input
                id="hotelPhoneNumber"
                value={hotelPhoneNumber}
                onChange={handlePhoneNumberChange}
                placeholder="010-1234-5678"
                maxLength={13}
                required
                className="w-full border p-3 rounded-md"
              />
            </div>

            {/* 주소 */}
            <div>
              <Label htmlFor="streetAddress" className="text-lg font-semibold">
                주소
              </Label>
              <Input
                id="streetAddress"
                value={streetAddress}
                onChange={(e) => setStreetAddress(e.target.value)}
                required
                className="w-full border p-3 rounded-md"
              />
            </div>

            {/* 우편번호 */}
            <div>
              <Label htmlFor="zipCode" className="text-lg font-semibold">
                우편번호
              </Label>
              <Input
                id="zipCode"
                value={zipCode}
                onChange={(e) => setZipCode(e.target.value)}
                required
                className="w-full border p-3 rounded-md"
              />
            </div>

            {/* 호텔 등급 (별점) */}
            <div>
              <Label className="text-lg font-semibold">호텔 등급</Label>
              <div className="flex space-x-1 mt-2">
                {[1, 2, 3, 4, 5].map((value) => (
                  <Star
                    key={value}
                    size={32}
                    className={`cursor-pointer transition ${
                      value <= (hoverGrade ?? hotelGrade)
                        ? "fill-yellow-400 stroke-yellow-400"
                        : "stroke-gray-400"
                    }`}
                    onClick={() => setHotelGrade(value)}
                    onMouseEnter={() => setHoverGrade(value)}
                    onMouseLeave={() => setHoverGrade(null)}
                  />
                ))}
              </div>
            </div>

            {/* 체크인 & 체크아웃 */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="text-lg font-semibold">체크인 시간 </Label>
                <DatePicker
                  selected={parseTimeToDate(checkInTime)}
                  onChange={(date) => setCheckInTime(formatTimeToHHMM(date))}
                  showTimeSelect
                  showTimeSelectOnly
                  timeIntervals={30}
                  timeFormat="HH:mm"
                  dateFormat="HH:mm"
                  className="border p-3 w-full rounded-md"
                />
              </div>
              <div>
                <Label className="text-lg font-semibold">체크아웃 시간 </Label>
                <DatePicker
                  selected={parseTimeToDate(checkOutTime)}
                  onChange={(date) => setCheckOutTime(formatTimeToHHMM(date))}
                  showTimeSelect
                  showTimeSelectOnly
                  timeIntervals={30}
                  timeFormat="HH:mm"
                  dateFormat="HH:mm"
                  className="border p-3 w-full rounded-md"
                />
              </div>
            </div>

            {/* 호텔 설명 */}
            <div>
              <Label
                htmlFor="hotelExplainContent"
                className="text-lg font-semibold"
              >
                호텔 설명
              </Label>
              <textarea
                id="hotelExplainContent"
                placeholder="호텔 설명을 입력하세요"
                value={hotelExplainContent}
                onChange={(e) => setHotelExplainContent(e.target.value)}
                className="w-full border p-3 rounded-md h-32 resize-none"
              ></textarea>
            </div>

            <div>
              <Label htmlFor="hotelStatus">호텔 상태</Label>
              <select
                id="hotelStatus"
                value={hotelStatus}
                onChange={(e) => setHotelStatus(e.target.value)}
                className="border p-2 w-full"
              >
                <option value="AVAILABLE">사용 가능</option>
                <option value="PENDING">승인 대기 중</option>
                <option value="UNAVAILABLE">사용 불가</option>
              </select>
            </div>

            {existingImages.length > 0 && (
              <div className="space-y-2">
                <Label>기존 이미지</Label>
                <div className="mt-4 grid grid-cols-3 gap-4">
                  {existingImages.map((img, index) => (
                    <div key={index} className="relative group">
                      <img
                        src={img}
                        alt={`호텔 이미지 ${index + 1}`}
                        className="w-full h-32 object-cover rounded-md"
                      />
                      <Button
                        type="button"
                        variant="destructive"
                        size="icon"
                        className="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity"
                        onClick={() => handleImageDelete(img)}
                      >
                        <XCircle className="w-4 h-4" />
                      </Button>
                    </div>
                  ))}
                </div>
              </div>
            )}

            <div>
              <Label htmlFor="images" className="block mb-2">
                이미지
              </Label>
              <Input
                id="images"
                type="file"
                multiple
                accept="image/*"
                onChange={handleNewImageUpload}
                className="cursor-pointer border p-2 w-full"
              />
              <div className="mt-4 grid grid-cols-3 gap-4">
                {imagePreviews.map((preview, index) => (
                  <div key={index} className="relative group">
                    <img
                      src={preview}
                      alt={`미리보기 이미지 ${index + 1}`}
                      className="w-full h-32 object-cover rounded-md"
                    />
                    <Button
                      type="button"
                      variant="destructive"
                      size="icon"
                      className="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity"
                      onClick={() => handlenewImageDelete(index)}
                    >
                      <XCircle className="w-4 h-4" />
                    </Button>
                  </div>
                ))}
              </div>
            </div>

            {/* 호텔 옵션 */}
            <div>
              <Label htmlFor="hotelOptions" className="text-lg font-semibold">
                호텔 옵션
              </Label>
              <div className="grid grid-cols-2 gap-2">
                {[...availableHotelOptions].map((option) => (
                  <label
                    key={option}
                    className="flex items-center space-x-2 border p-3 rounded-md cursor-pointer transition hover:bg-gray-100"
                  >
                    <input
                      type="checkbox"
                      value={option}
                      checked={hotelOptions.has(option)}
                      onChange={() => handleOptionChange(option)}
                      className="w-5 h-5 accent-blue-500"
                    />
                    <span className="text-sm font-medium">{option}</span>
                  </label>
                ))}
              </div>
            </div>

            <Button
              type="submit"
              className="w-full bg-blue-500 text-white p-3 text-lg rounded-md"
            >
              수정 완료
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
