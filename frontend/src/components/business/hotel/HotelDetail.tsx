import { Button } from "@/components/ui/button";
import { deleteHotel } from "@/lib/api/hotel/BusinessHotelApi";
import { HotelDetailDto } from "@/lib/types/hotel/HotelDetailDto";
import { getRoleFromCookie } from "@/lib/utils/CookieUtil";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import {
  addFavorite,
  removeFavorite,
  checkFavorite,
} from "@/lib/api/member/FavoriteApi";
import {
  FaEnvelope,
  FaPhone,
  FaMapMarkerAlt,
  FaClock,
  FaStar,
  FaCheckCircle,
  FaHeart,
  FaRegHeart,
  FaCommentAlt,
} from "react-icons/fa";

interface HotelDetailProps {
  hotel: HotelDetailDto;
}

const HotelDetail: React.FC<HotelDetailProps> = ({ hotel }) => {
  const cookie = getRoleFromCookie();
  const hotelOptions = Array.from(hotel.hotelOptions).sort();
  const [hotelId, setHotelId] = useState(-1);
  const [isBusinessUser, setIsBusinessUser] = useState<boolean>(false);
  const [canEdit, setCanEdit] = useState<boolean>(false);
  const [isLiked, setIsLiked] = useState<boolean>(false);
  const router = useRouter();
  const param = useParams();

  useEffect(() => {
    console.log("호텔 옵션: ", hotelOptions);
    const cookieHotelId = cookie?.hotelId ? Number(cookie.hotelId) : -1;
    const paramHotelId = param.hotelId ? Number(param.hotelId) : null;
    console.log("쿠키 호텔 ID : ", cookieHotelId);
    console.log("파람 호텔 ID : ", paramHotelId);

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
  }, [cookie, param.hotelId]);

  useEffect(() => {
    const fetchFavorite = async () => {
      try {
        const isFavorite: boolean = await checkFavorite(hotelId);
        if (isFavorite) {
          setIsLiked(true);
        }
      } catch (error) {
        console.error("Error fetching favorites:", error);
      }
    };

    if (cookie && cookie.role === "USER") {
      fetchFavorite();
    }
  });

  const handleLike = async () => {
    if (!isLiked) {
      await addFavorite(hotelId);
    } else {
      await removeFavorite(hotelId);
    }
    setIsLiked(!isLiked);
  };

  const handleEdit = (hotelId: number) => {
    router.push(`/business/hotel/${hotelId}`);
  };

  const handleRoomCreate = () => {
    router.push("/business/rooms");
  };

  const handleDelete = async (hotelId: number) => {
    if (!window.confirm("호텔을 삭제하시겠습니까?")) return;

    try {
      await deleteHotel(hotelId);
      alert("호텔이 삭제되었습니다.");
      router.push("/business/hotel/management");
    } catch (error) {
      console.error("Error : ", error);
      alert(error);
    }
  };

  const handleReview = () => {
    router.push(`/hotels/${hotelId}/reviews`);
  };

  return (
    <div className="bg-white p-8 shadow-lg rounded-2xl border border-gray-200">
      {/* 호텔명 */}
      <div className="text-center mb-8">
        <div className="flex justify-center items-center gap-4">
          <h2 className="text-6xl font-extrabold text-sky-500 drop-shadow-[2px_2px_5px_rgba(255,255,255,0.7)]">
            {hotel.hotelName}
          </h2>
          {cookie && cookie.role === "USER" && (
            <button
              onClick={handleLike}
              className="transition-transform hover:scale-110 focus:outline-none"
            >
              {isLiked ? (
                <FaHeart className="text-4xl text-red-500" />
              ) : (
                <FaRegHeart className="text-4xl text-gray-400 hover:text-red-500" />
              )}
            </button>
          )}
        </div>
        <p className="text-xl text-gray-700 mt-2 italic">
          ✨ 특별한 하루, 최고의 경험을 선사합니다 ✨
        </p>
      </div>

      {/* 호텔 기본 정보 카드 */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-lg text-gray-700">
        {[
          {
            icon: FaEnvelope,
            label: "이메일",
            value: hotel.hotelEmail,
            color: "text-blue-500",
          },
          {
            icon: FaPhone,
            label: "전화번호",
            value: hotel.hotelPhoneNumber,
            color: "text-green-500",
          },
          {
            icon: FaMapMarkerAlt,
            label: "주소",
            value: `${hotel.streetAddress} (${hotel.zipCode})`,
            color: "text-red-500",
          },
          {
            icon: FaStar,
            label: "호텔 등급",
            value: `${hotel.hotelGrade}성급`,
            color: "text-yellow-500",
          },
          {
            icon: FaClock,
            label: "체크인",
            value: hotel.checkInTime.slice(0, 5),
            color: "text-purple-500",
          },
          {
            icon: FaClock,
            label: "체크아웃",
            value: hotel.checkOutTime.slice(0, 5),
            color: "text-purple-500",
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

      {/* 리뷰 및 좋아요 버튼 섹션 */}
      <div className="mt-8 flex justify-center gap-4">
        <Button
          onClick={handleReview}
          className="bg-gradient-to-r from-purple-500 to-blue-500 hover:from-purple-600 hover:to-blue-600 text-white px-6 py-3 rounded-full shadow-lg hover:shadow-xl transition-all duration-300 flex items-center gap-2 text-lg"
        >
          <FaCommentAlt />
          리뷰 보기
        </Button>
      </div>

      {/* 호텔 옵션 */}
      <div className="mt-8 bg-gray-100 p-6 rounded-xl">
        <h3 className="text-xl font-bold text-gray-900 mb-4">호텔 옵션</h3>
        <div className="flex flex-wrap gap-4">
          {hotelOptions.length > 0 ? (
            Array.from(hotelOptions).map((option) => (
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

      {/* 호텔 설명 */}
      <div className="mt-8 bg-gray-50 p-6 rounded-xl shadow-inner">
        <h3 className="text-xl font-bold text-gray-900 mb-4">호텔 설명</h3>
        <p className="text-gray-700 leading-relaxed whitespace-pre-line">
          {hotel.hotelExplainContent}
        </p>
      </div>

      {/* 수정/삭제 버튼 */}
      {isBusinessUser && canEdit && (
        <div className="mt-4 flex justify-between items-center">
          <Button
            className="bg-green-500 text-white"
            onClick={handleRoomCreate}
          >
            객실 추가
          </Button>
          <div className="flex gap-2">
            <Button
              className="bg-blue-500 text-white"
              onClick={() => handleEdit(hotelId)}
            >
              수정
            </Button>
            <Button variant="destructive" onClick={() => handleDelete(hotelId)}>
              삭제
            </Button>
          </div>
        </div>
      )}
    </div>
  );
};

export default HotelDetail;
