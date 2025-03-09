"use client";

import { Button } from "@/components/ui/button";
import { GetRoomResponse } from "@/lib/types/room/GetRoomResponse";
import { deleteRoom } from "@/lib/api/hotel/room/BusinessRoomApi";
import { getRoleFromCookie } from "@/lib/utils/CookieUtil";
import { useParams, useRouter, useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";

interface RoomListProps {
  rooms: GetRoomResponse[];
  checkInDate?: string;
  checkOutDate?: string;
}

const RoomList: React.FC<RoomListProps> = ({ rooms }) => {
  const cookie = getRoleFromCookie();
  const [hotelId, setHotelId] = useState(cookie?.hotelId);
  const [selectedImage, setSelectedImage] = useState<string | null>(null);
  const [isBusinessUser, setIsBusinessUser] = useState<boolean>(false);
  const [paramHotelId, setParamHotelId] = useState<number | null>(null);
  const [canEdit, setCanEdit] = useState<boolean>(false);
  const searchParams = useSearchParams();
  const param = useParams();
  const router = useRouter();
  const cookieHotelId = cookie?.hotelId ? Number(cookie.hotelId) : -1;
  const checkInDate = searchParams.get("checkInDate") || "";
  const checkOutDate = searchParams.get("checkOutDate") || "";

  useEffect(() => {
    const parsedParamHotelId = param.hotelId ? Number(param.hotelId) : null;

    setParamHotelId(parsedParamHotelId);

    console.log("쿠키 호텔 ID : ", cookieHotelId);
    console.log("파람 호텔 ID : ", paramHotelId);

    setIsBusinessUser(cookie?.role == "BUSINESS");

    if (parsedParamHotelId !== null) {
      setHotelId(parsedParamHotelId);
      setCanEdit(isBusinessUser && cookieHotelId === parsedParamHotelId);
    } else if (isBusinessUser) {
      setHotelId(cookieHotelId);
      setCanEdit(true);
    } else {
      setCanEdit(false);
    }
  }, [cookie, param.hotelId]);

  const handleEdit = (roomId: number) => {
    router.push(`/business/rooms/${roomId}`);
  };

  const handleDelete = async (roomId: number) => {
    if (!window.confirm("객실을 삭제하시겠습니까?")) return;

    try {
      console.log("지우려는 호텔 ID: ", hotelId);
      console.log("지우려는 객실 ID: ", roomId);
      await deleteRoom(hotelId ?? -1, roomId);
      alert("객실이 삭제되었습니다.");
      router.push("/business/hotel/management");
    } catch (error) {
      console.error("Error : ", error);
      alert(error);
    }
  };

  const handleReservation = (roomId: number) => {
    const params = new URLSearchParams();
    params.set("hotelId", (hotelId ?? "").toString());
    params.set("roomId", roomId.toString());
    if (checkInDate) params.set("checkInDate", checkInDate);
    if (checkOutDate) params.set("checkOutDate", checkOutDate);

    console.log(
      "예약 페이지로 이동 URL : ",
      `/orders/payment?${params.toString()}`
    );
    router.push(`/orders/payment?${params.toString()}`);
  };

  const handleRoomClick = (roomId: number) => {
    const params = new URLSearchParams();
    if (isBusinessUser) {
      if (paramHotelId !== cookieHotelId) {
        params.set("checkInDate", checkInDate);
        params.set("checkOutDate", checkOutDate);
      }
      router.push(`/hotels/${hotelId}/rooms/${roomId}?${params.toString()}`);
    } else {
      params.set("checkInDate", checkInDate);
      params.set("checkOutDate", checkOutDate);
      router.push(`/hotels/${hotelId}/rooms/${roomId}?${params.toString()}`);
    }
  };

  const handleButtonClick =
    (action: () => void) => (e: React.MouseEvent<HTMLButtonElement>) => {
      e.stopPropagation();
      action();
    };

  return (
    <div className="p-6">
      <h2 className="text-2xl font-bold mb-6">객실 목록</h2>
      {rooms.length === 0 ? (
        <p className="text-gray-500 text-center">등록된 객실이 없습니다.</p>
      ) : (
        <ul className="space-y-4">
          {rooms.map((room) => (
            <li
              key={room.roomId}
              className="flex flex-col border rounded-lg shadow-md p-8 bg-white cursor-pointer duration-200 hover:scale-105"
              onClick={() => {
                if (room.roomNumber !== 0) {
                  handleRoomClick?.(room.roomId);
                }
              }}
            >
              {/* 이미지 & 정보 섹션 */}
              <div className="flex items-center">
                {room.thumbnailUrl && (
                  <img
                    src={room.thumbnailUrl}
                    alt={room.roomName}
                    className="w-80 h-64 object-cover rounded-lg mr-8 cursor-pointer duration-200 hover:scale-105"
                    onClick={() => setSelectedImage(room.thumbnailUrl)}
                  />
                )}

                <div className="flex-1">
                  <h3 className="text-3xl font-bold text-gray-900 mb-12">
                    {room.roomName}
                  </h3>
                  <p className="text-lg text-gray-600 mb-2">
                    🏠 수용 인원:{" "}
                    <span className="font-semibold">
                      {room.standardNumber} ~ {room.maxNumber}명
                    </span>
                  </p>
                  <p className="text-lg text-gray-600 mb-2">
                    🛏️ 침대 타입:{" "}
                    <span className="font-semibold">
                      {Object.entries(room.bedTypeNumber)
                        .filter(([_, count]) => count > 0)
                        .map(
                          ([type, count]) =>
                            `${type
                              .replace("bed_", "")
                              .toUpperCase()} ${count}개`
                        )
                        .join(", ")}
                    </span>
                  </p>

                  {/* 가격 및 남은 객실 수 (우측 중앙 정렬) */}
                  <div className="text-right">
                    <p className="text-2xl font-bold text-blue-600">
                      {room.basePrice.toLocaleString()}원~
                    </p>
                    <p className="text-lg font-semibold text-red-500">
                      잔여 객실 : {room.roomNumber}
                    </p>
                  </div>
                </div>
              </div>

              {/* 버튼 섹션 */}
              <div className="mt-4 flex justify-end gap-2">
                {isBusinessUser && canEdit ? (
                  <>
                    <Button
                      className="bg-blue-500 text-white"
                      onClick={handleButtonClick(() => handleEdit(room.roomId))}
                    >
                      수정
                    </Button>
                    <Button
                      variant="destructive"
                      onClick={handleButtonClick(() =>
                        handleDelete(room.roomId)
                      )}
                    >
                      삭제
                    </Button>
                  </>
                ) : (
                  <Button
                    className="bg-green-500 text-white"
                    disabled={room.roomNumber === 0}
                    onClick={(e) => {
                      if (room.roomNumber === 0) {
                        e.preventDefault(); // 예약하기 버튼이 비활성화 되어도 클릭 시 아무 동작도 안함
                      } else {
                        handleButtonClick(() => handleReservation(room.roomId))(
                          e
                        );
                      }
                    }}
                  >
                    예약하기
                  </Button>
                )}
              </div>

              {/* 모달 - 이미지 확대 */}
              {selectedImage && (
                <div
                  className="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50"
                  onClick={() => setSelectedImage(null)}
                >
                  <div className="relative p-4 bg-white rounded-lg shadow-lg max-w-3xl">
                    <button
                      className="absolute top-2 right-2 text-gray-600 hover:text-gray-900 test-2x1"
                      onClick={() => setSelectedImage(null)}
                    >
                      X
                    </button>
                    <img
                      src={selectedImage}
                      alt="확대한 이미지"
                      className="w-full max-h-[95vh] object-contain rounded-lg"
                    />
                  </div>
                </div>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default RoomList;
