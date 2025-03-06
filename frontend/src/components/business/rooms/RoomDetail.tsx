import { Button } from "@/components/ui/button";
import { deleteRoom } from "@/lib/api/hotel/room/BusinessRoomApi";
import { GetRoomDetailResponse } from "@/lib/types/room/GetRoomDetailResponse";
import { getRoleFromCookie } from "@/lib/utils/CookieUtil";
import { useParams, useRouter, useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";
import { FaBed, FaUsers, FaTag, FaCheckCircle } from "react-icons/fa";

interface RoomDetailProps {
  room: GetRoomDetailResponse;
  checkInDate?: string;
  checkoutDate?: string;
}

const RoomDetail: React.FC<RoomDetailProps> = ({ room }) => {
  const cookie = getRoleFromCookie();
  const roomOptions = Array.from(room.roomDto.roomOptions).sort();
  const [hotelId, setHotelId] = useState(cookie?.hotelId);
  const roomId = room.roomDto.id;
  const [isBusinessUser, setIsBusinessUser] = useState<boolean>(false);
  const [canEdit, setCanEdit] = useState<boolean>(false);
  const searchParams = useSearchParams();
  const router = useRouter();
  const param = useParams();
  const roomDto = room.roomDto;
  const checkInDate = searchParams.get("checkInDate") || "";
  const checkoutDate = searchParams.get("checkoutDate") || "";

  useEffect(() => {
    const cookieHotelId = cookie?.hotelId ? Number(cookie.hotelId) : -1;
    const paramHotelId = param.hotelId ? Number(param.hotelId) : null;

    console.log("현재 객실 Id : ", roomId);
    console.log("쿠키 호텔 ID : ", cookieHotelId);
    console.log("파람 호텔 ID : ", paramHotelId);
    console.log("파람 객실 ID : ", roomId);

    setIsBusinessUser(cookie?.role == "BUSINESS");

    if (paramHotelId !== null) {
      setHotelId(paramHotelId);
      setCanEdit(isBusinessUser && cookieHotelId === paramHotelId);
    } else if (isBusinessUser) {
      setHotelId(cookieHotelId);
      setCanEdit(true);
    } else {
      setCanEdit(false);
    }
  }, [cookie, hotelId, roomId]);

  const handleEdit = (roomId: number) => {
    router.push(`/business/rooms/${roomId}`);
  };

  const handleDelete = async (hotelId: number) => {
    if (!window.confirm("객실을 삭제하시겠습니까?")) return;

    try {
      console.log("호텔 Id : ", hotelId);
      console.log("객실 Id : ", roomId);
      await deleteRoom(hotelId, roomId);
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
    if (checkoutDate) params.set("checkoutDate", checkoutDate);

    console.log(
      "예약 페이지로 이동 URL : ",
      `/orders/payment?${params.toString()}`
    );
    router.push(`/orders/payment?${params.toString()}`);
  };

  return (
    <div className="bg-white p-8 shadow-lg rounded-2xl border border-gray-200">
      {/* 방 이름 */}
      <div className="text-center mb-8">
        <h2 className="text-4xl font-extrabold text-pink-500">
          {roomDto.roomName}
        </h2>
      </div>

      {/* 방 기본 정보 */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-lg text-gray-700">
        {[
          {
            icon: FaTag,
            label: "기본 가격",
            value: `${roomDto.basePrice.toLocaleString()}원`,
            color: "text-green-500",
          },
          {
            icon: FaUsers,
            label: "기준 인원",
            value: `${roomDto.standardNumber}명`,
            color: "text-blue-500",
          },
          {
            icon: FaUsers,
            label: "최대 인원",
            value: `${roomDto.maxNumber}명`,
            color: "text-red-500",
          },
          {
            icon: FaBed,
            label: "침대 타입",
            value: Object.entries(roomDto.bedTypeNumber)
              .filter(([_, count]) => count > 0)
              .map(([type, count]) => `${type} (${count}개)`)
              .join(", "),
            color: "text-purple-500",
          },
          {
            icon: FaUsers,
            label: "보유 객실 수",
            value: `${roomDto.roomNumber}개`,
            color: "text-orange-500",
          },
        ].map(({ icon: Icon, label, value, color }, index) => (
          <div
            key={index}
            className="flex items-center gap-4 bg-gray-50 p-4 rounded-lg shadow-sm"
          >
            <Icon className={`${color} text-2xl`} />
            <p className="text-gray-900 font-semibold">{label} :</p>
            <span className="text-gray-700">{value}</span>
          </div>
        ))}
      </div>

      {/* 객실 옵션 */}
      <div className="mt-8 bg-gray-100 p-6 rounded-xl">
        <h3 className="text-xl font-bold text-gray-900 mb-4">객실 옵션</h3>
        <div className="flex flex-wrap gap-4">
          {roomOptions.length > 0 ? (
            Array.from(roomOptions).map((option) => (
              <span
                key={option}
                className="flex items-center gap-2 bg-white border px-4 py-2 rounded-full shadow-sm"
              >
                <FaCheckCircle className="text-green-500" />
                {option}
              </span>
            ))
          ) : (
            <p className="text-gray-500">제공되는 옵션이 없습니다.</p>
          )}
        </div>
      </div>

      {/* 버튼 섹션 */}
      <div className="mt-4 flex justify-end gap-2">
        {isBusinessUser && canEdit ? (
          <>
            <Button
              className="bg-blue-500 text-white"
              onClick={() => handleEdit(roomId)}
            >
              수정
            </Button>
            <Button
              variant="destructive"
              onClick={() => handleDelete(Number(hotelId))}
            >
              삭제
            </Button>
          </>
        ) : (
          <Button
            className={`bg-green-500 text-white ${
              roomDto.roomNumber === 0 ? "pointer-events-none opacity-50" : ""
            }`}
            disabled={roomDto.roomNumber === 0}
            onClick={() => handleReservation(roomId)}
          >
            예약하기
          </Button>
        )}
      </div>
    </div>
  );
};

export default RoomDetail;
