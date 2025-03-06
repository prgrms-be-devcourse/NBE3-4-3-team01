"use client";

import React, { useEffect, useState } from "react";
import {
  createRoom,
  findAllRoomOptions,
  saveRoomImageUrls,
} from "@/lib/api/hotel/room/BusinessRoomApi";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { BED_TYPES, BedTypeNumber } from "@/lib/types/room/BedTypeNumber";
import { PostRoomResponse } from "@/lib/types/room/PostRoomResponse";
import { PostRoomRequest } from "@/lib/types/room/PostRoomRequest";
import { GetAllRoomOptionsResponse } from "@/lib/types/room/GetAllRoomOptionsResponse";
import { uploadImagesToS3 } from "@/lib/api/aws/AwsS3Api";
import { PresignedUrlsResponse } from "@/lib/types/review/PresignedUrlsResponse";
import { useRouter } from "next/navigation";
import { MoveLeft, XCircle } from "lucide-react";
import { getRoleFromCookie } from "@/lib/utils/CookieUtil";
import Navigation from "@/components/navigation/Navigation";

export default function CreateRoomPage() {
  const cookie = getRoleFromCookie();
  const router = useRouter();
  const [roomName, setRoomName] = useState("");
  const [roomNumber, setRoomNumber] = useState(1);
  const [basePrice, setBasePrice] = useState(50000);
  const [standardNumber, setStandardNumber] = useState(1);
  const [maxNumber, setMaxNumber] = useState(1);
  const [bedTypeNumber, setBedTypeNumber] = useState<BedTypeNumber>({
    SINGLE: 0,
    DOUBLE: 0,
    QUEEN: 0,
    KING: 0,
    TWIN: 0,
    TRIPLE: 0,
  });
  const [images, setImages] = useState<File[]>([]);
  const [imagePreviews, setImagePreviews] = useState<string[]>([]);
  const [presigendUrls, setPresignedUrls] = useState<string[]>([]);
  const [hotelId, setHotelId] = useState(Number(cookie?.hotelId));
  const [roomId, setRoomId] = useState(-1);
  const [roomOptions, setRoomOptions] = useState<Set<string>>(new Set());
  const [availableRoomOptions, setAvailableRoomOptions] = useState<string[]>(
    []
  );

  // 객실 옵션 전체 리스트 가져오기
  useEffect(() => {
    const loadRoomOptions = async () => {
      try {
        setHotelId(Number(cookie?.hotelId));
        const options: GetAllRoomOptionsResponse = await findAllRoomOptions(
          hotelId
        );
        setAvailableRoomOptions(options.roomOptions.sort());
      } catch (error) {
        alert("객실 옵션을 가져오는 중 오류가 발생했습니다.");
        console.error("객실 옵션을 가져오지 못했습니다.");
        throw error;
      }
    };
    loadRoomOptions();
  }, []);

  const handleOptionChange = (option: string) => {
    setRoomOptions((prev) => {
      const newOptions = new Set(prev);
      if (newOptions.has(option)) {
        newOptions.delete(option);
      } else {
        newOptions.add(option);
      }

      return newOptions;
    });
  };

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const selectedFiles = Array.from(e.target.files);
      setImages(selectedFiles);

      // 미리보기 업데이트
      setImagePreviews(selectedFiles.map((file) => URL.createObjectURL(file)));
    }
  };

  const handleImageRemove = (index: number) => {
    setImages((prevImages) => prevImages.filter((_, i) => i !== index));
    setImagePreviews((prevPreviews) =>
      prevPreviews.filter((_, i) => i !== index)
    );
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const requestBody: PostRoomRequest = {
      roomName,
      roomNumber: Number(roomNumber),
      basePrice: Number(basePrice),
      standardNumber: Number(standardNumber),
      maxNumber: Number(maxNumber),
      bedTypeNumber,
      imageExtensions: images.map(
        (file) => file.name.split(".").pop()?.toLowerCase() || ""
      ),
      roomOptions: Array.from(roomOptions),
    };

    try {
      setHotelId(Number(cookie?.hotelId));
      const response: PostRoomResponse = await createRoom(
        Number(hotelId),
        requestBody
      );

      const presignedUrlResponse: PresignedUrlsResponse = response.urlsResponse;
      setPresignedUrls(presignedUrlResponse.presignedUrls);
      setRoomId(presignedUrlResponse.reviewId);
      alert("객실이 성공적으로 등록되었습니다.");
      router.push("/business/hotel/management");
    } catch (error) {
      console.error("Error: ", error);
      alert(error);
    }
  };

  useEffect(() => {
    if (presigendUrls.length > 0) {
      console.log("✅ presignedUrls 설정됨:", presigendUrls);
      submitImages();
    }
  }, [presigendUrls]);

  // PresignedUrls 를 사용하여 이미지 업로드
  const submitImages = async () => {
    if (presigendUrls.length === 0) {
      alert("이미지 업로드 URL을 가져오지 못했습니다.");
      return;
    }

    try {
      await uploadImagesToS3(presigendUrls, images);
      await saveImageUrls();
      console.log("이미지가 성공적으로 업로드 되었습니다.");
    } catch (error) {
      console.error("Error: ", error);
      alert(error);
    }
  };

  // 사진 조회용 URL들 서버로 전달
  const saveImageUrls = async () => {
    const viewUrls = presigendUrls.map((presigendUrls) => {
      return presigendUrls.split("?")[0];
    });

    try {
      await saveRoomImageUrls(hotelId, roomId, viewUrls);
      console.log("객실 이미지가 성공적으로 저장되었습니다.");
    } catch (error) {
      console.error("Error: ", error);
      alert(error);
    }
  };

  return (
    <div className="p-6 max-w-3xl mx-auto pt-[100px]">
      <Navigation />
      <Card className="border p-6 rounded-lg shadow-lg">
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
              객실 추가
            </CardTitle>
          </div>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* 객실 기본 정보 */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <Label htmlFor="roomName">객실 이름</Label>
                <Input
                  id="roomName"
                  value={roomName}
                  onChange={(e) => setRoomName(e.target.value)}
                  required
                />
              </div>

              <div>
                <Label htmlFor="roomNumber">객실 수</Label>
                <Input
                  id="roomNumber"
                  type="number"
                  min="1"
                  value={roomNumber}
                  onChange={(e) => setRoomNumber(Number(e.target.value))}
                  required
                />
              </div>

              <div>
                <Label htmlFor="basePrice">인당 가격</Label>
                <Input
                  id="basePrice"
                  type="number"
                  min="0"
                  value={basePrice}
                  onChange={(e) => setBasePrice(Number(e.target.value))}
                  required
                />
              </div>

              <div>
                <Label htmlFor="standardNumber">최소 인원</Label>
                <Input
                  id="standardNumber"
                  type="number"
                  min="1"
                  value={standardNumber}
                  onChange={(e) => setStandardNumber(Number(e.target.value))}
                  required
                />
              </div>

              <div>
                <Label htmlFor="maxNumber">최대 인원</Label>
                <Input
                  id="maxNumber"
                  type="number"
                  min="1"
                  value={maxNumber}
                  onChange={(e) => setMaxNumber(Number(e.target.value))}
                  required
                />
              </div>
            </div>

            {/* 침대 옵션 */}
            <div>
              <Label>침대 유형 및 개수</Label>
              <div className="grid grid-cols-3 gap-4 mt-2">
                {BED_TYPES.map((type) => (
                  <div
                    key={type}
                    className="flex items-center justify-between border p-3 rounded-lg shadow-sm"
                  >
                    <Label className="font-medium">{type.toUpperCase()}</Label>
                    <Input
                      type="number"
                      min="0"
                      className="w-16 text-center border rounded-md"
                      value={bedTypeNumber[type] ?? 0}
                      onChange={(e) =>
                        setBedTypeNumber({
                          ...bedTypeNumber,
                          [type]: Number(e.target.value),
                        })
                      }
                    />
                  </div>
                ))}
              </div>
            </div>

            <div>
              <Label>새 이미지 업로드</Label>
              <Input
                type="file"
                multiple
                accept="image/*"
                onChange={handleImageUpload}
                className="cursor-pointer border p-2 w-full"
              />
              <div className="grid grid-cols-3 gap-4 mt-4">
                {imagePreviews.map((preview, index) => (
                  <div key={index} className="relative group">
                    <img
                      src={preview}
                      alt="객실 이미지"
                      className="w-full h-32 object-cover rounded-md"
                    />
                    <Button
                      type="button"
                      size="icon"
                      className="absolute top-2 right-2 bg-white text-red-500 p-1 rounded-full"
                      onClick={() => handleImageRemove(index)}
                    >
                      <XCircle className="w-4 h-4" />
                    </Button>
                  </div>
                ))}
              </div>
            </div>

            {/* 객실 옵션 */}
            <div>
              <Label>객실 옵션</Label>
              <div className="grid grid-cols-2 md:grid-cols-3 gap-4 mt-2">
                {[...availableRoomOptions].map((option) => (
                  <label
                    key={option}
                    className="flex items-center space-x-2 border p-3 rounded-md cursor-pointer transition hover:bg-gray-100"
                  >
                    <input
                      type="checkbox"
                      value={option}
                      checked={roomOptions.has(option)}
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
              className="w-full bg-blue-500 text-white mt-4"
            >
              등록
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
