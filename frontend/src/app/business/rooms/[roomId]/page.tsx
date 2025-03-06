"use client";

import {
  findAllRoomOptions,
  findRoomDetail,
  modifyRoom,
  saveRoomImageUrls,
} from "@/lib/api/hotel/room/BusinessRoomApi";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { BED_TYPES, BedTypeNumber } from "@/lib/types/room/BedTypeNumber";
import { PutRoomRequest } from "@/lib/types/room/PutRoomRequest";
import { MoveLeft, XCircle } from "lucide-react";
import { uploadImagesToS3 } from "@/lib/api/aws/AwsS3Api";
import { PutRoomResponse } from "../../../../lib/types/room/PutRoomResponse";
import { PresignedUrlsResponse } from "@/lib/types/review/PresignedUrlsResponse";
import { GetAllRoomOptionsResponse } from "@/lib/types/room/GetAllRoomOptionsResponse";
import { getRoleFromCookie } from "@/lib/utils/CookieUtil";
import Navigation from "@/components/navigation/Navigation";

export default function ModifyRoomPage() {
  const cookie = getRoleFromCookie();
  const hotelId = Number(cookie?.hotelId);
  const params = useParams();
  const router = useRouter();
  const [roomId, setRoomId] = useState(Number(params.roomId));
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
  const [roomStatus, setRoomStatus] = useState("AVAILABLE");
  const [existingImages, setExistingImages] = useState<string[]>([]);
  const [deleteImageUrls, setDeleteImageUrls] = useState<string[]>([]);
  const [images, setImages] = useState<File[]>([]);
  const [imagePreviews, setImagePreviews] = useState<string[]>([]);
  const [imageExtensions, setImageExtensions] = useState<string[]>([]);
  const [presignedUrls, setPresignedUrls] = useState<string[]>([]);
  const [roomOptions, setRoomOptions] = useState<Set<string>>(new Set());
  const [availableRoomOptions, setAvailableRoomOptions] = useState<string[]>(
    []
  );

  // 객실 옵션 전체 리스트 가져오기
  useEffect(() => {
    const loadRoomOptions = async () => {
      try {
        const options: GetAllRoomOptionsResponse = await findAllRoomOptions(
          hotelId
        );
        setAvailableRoomOptions(options.roomOptions.sort());
      } catch (error) {
        throw error;
      }
    };
    loadRoomOptions();
  }, [roomId]);

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

  useEffect(() => {
    const fetchRoomData = async () => {
      try {
        console.log("호텔 Id : ", Number(hotelId));
        console.log("객실 Id : ", Number(roomId));
        const response = await findRoomDetail(hotelId, roomId);
        const bedTypeNumber = response.roomDto.bedTypeNumber;
        setRoomId(response.roomDto.id);
        setRoomName(response.roomDto.roomName);
        setRoomNumber(response.roomDto.roomNumber);
        setBasePrice(response.roomDto.basePrice);
        setStandardNumber(response.roomDto.standardNumber);
        setMaxNumber(response.roomDto.maxNumber);
        setBedTypeNumber(bedTypeNumber);
        setExistingImages(response.roomImageUrls);
        setRoomOptions(new Set(response.roomDto.roomOptions || []));
        console.log("객실 정보: ", response);
      } catch (error) {
        throw error;
      }
    };

    fetchRoomData();
  }, [params.hotelId, params.roomId]);

  const handleImageDelete = (imageUrl: string) => {
    console.log("Existing Image: ", existingImages);
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

  useEffect(() => {
    return () => {
      imagePreviews.forEach((preview) => URL.revokeObjectURL(preview));
    };
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const requestBody: PutRoomRequest = {
      roomName,
      roomNumber,
      basePrice,
      standardNumber,
      maxNumber,
      bedTypeNumber,
      roomStatus,
      deleteImageUrls,
      imageExtensions: imageExtensions,
      roomOptions: Array.from(roomOptions),
    };

    try {
      const response: PutRoomResponse = await modifyRoom(
        hotelId,
        roomId,
        requestBody
      );
      console.log("ModifyRoom response", response);
      console.log("responseUrl", response.urlResponse);

      const preSigendUrlsResponse: PresignedUrlsResponse = response.urlResponse;

      console.log("ModifyRoom response", response);
      console.log("PresignedUrlsResponse", presignedUrls);

      setRoomId(response.roomId);
      setPresignedUrls(preSigendUrlsResponse.presignedUrls);
      console.log(presignedUrls);
      alert("객실이 성공적으로 수정되었습니다.");
      router.push("/business/hotel/management");
    } catch (error) {
      console.error("Error:", error);
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
      console.error("Error:", error);
      alert(error);
    }
  };

  const saveImageUrls = async () => {
    const urls = presignedUrls.map((presignedUrls) => {
      return presignedUrls.split("?")[0];
    });

    try {
      saveRoomImageUrls(hotelId, roomId, urls);
      console.log("객실 이미지가 성공적으로 저장되었습니다.");
    } catch (error) {
      console.error("Error:", error);
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
              객실 수정
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
              <Label htmlFor="roomStatus">객실 상태</Label>
              <select
                id="roomStatus"
                value={roomStatus}
                onChange={(e) => setRoomStatus(e.target.value)}
                className="border p-2 w-full"
              >
                <option value="AVAILABLE">사용 가능</option>
                <option value="IN_BOOKING">예약 중</option>
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
                        alt={`객실 이미지 ${index + 1}`}
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
              <Label>새 이미지 업로드</Label>
              <Input
                type="file"
                multiple
                accept="image/*"
                onChange={handleNewImageUpload}
                className="cursor-pointer border p-2 w-full"
              />
              <div className="grid grid-cols-3 gap-4 mt-4">
                {imagePreviews.map((preview, index) => (
                  <div key={index} className="relative group">
                    <img
                      src={preview}
                      alt="새 객실 이미지"
                      className="w-full h-32 object-cover rounded-md"
                    />
                    <Button
                      type="button"
                      size="icon"
                      className="absolute top-2 right-2 bg-white text-red-500 p-1 rounded-full"
                      onClick={() => handlenewImageDelete(index)}
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
              수정 완료
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
