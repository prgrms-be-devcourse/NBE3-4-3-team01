import { PageDto } from "@/lib/types/PageDto";
import { BookingResponseSummary } from "@/lib/types/booking/BookingResponseSummary";
import { MoreVertical } from "lucide-react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { cancelBooking } from "@/lib/api/booking/BookingApi";
import { useState } from "react";
import { useRouter } from "next/navigation";

const UserBookingList = function ({
  bookings,
}: {
  bookings: PageDto<BookingResponseSummary>;
}) {
  const [bookingList, setBookingList] = useState<BookingResponseSummary[]>(
    bookings.items
  );
  const [isStatusChanged, setIsStatusChanged] = useState(false);
  const router = useRouter();

  const openBookingDetailsPopup = function (bookingId: number) {
    window.open(
      `/orders/${bookingId}`,
      "OrderPopup",
      "width=500,height=600,left=200,top=200"
    );
  };

  const handleReviewClick = async (
    bookingId: number,
    hotelId: number,
    roomId: number,
    e: React.MouseEvent
  ) => {
    e.stopPropagation();
    router.push(`/me/orders/${bookingId}?hotelId=${hotelId}&roomId=${roomId}`);
  };

  const handleCancelClick = async (bookingId: number, e: React.MouseEvent) => {
    e.stopPropagation(); // 부모 요소의 클릭 이벤트 전파 방지

    if (confirm("정말로 이 예약을 취소하시겠습니까?")) {
      try {
        await cancelBooking(bookingId);
        // 로컬 상태 업데이트
        setBookingList((prevList) =>
          prevList.map((booking) =>
            booking.bookingId === bookingId
              ? { ...booking, bookingStatus: "CANCELLED" }
              : booking
          )
        );
        setIsStatusChanged(true);
        alert("예약이 정상적으로 취소되었습니다.");
      } catch (error) {
        alert(error);
      }
    }
  };

  const formatPrice = (amount: number) => {
    return `₩${Number(amount).toLocaleString()}`;
  };

  const getStatusInfo = (status: string) => {
    switch (status) {
      case "CONFIRMED":
        return { text: "확정", style: "bg-blue-100 text-blue-800" };
      case "CANCELLED":
        return { text: "취소", style: "bg-red-100 text-red-800" };
      case "COMPLETED":
        return { text: "완료", style: "bg-green-100 text-green-800" };
      default:
        return { text: status, style: "bg-gray-100 text-gray-800" };
    }
  };

  return (
    <div className="max-w-screen-lg mx-auto">
      <div className="space-y-4">
        {bookingList.length > 0 ? (
          bookingList.map((booking) => (
            <Card key={booking.bookingId} className="overflow-hidden">
              <div
                className="bg-white hover:bg-gray-50 cursor-pointer p-6 transition-colors"
                onClick={() => openBookingDetailsPopup(booking.bookingId)}
              >
                <div className="flex justify-between items-start">
                  <div className="flex gap-6">
                    <div className="w-40 h-32">
                      <img
                        src={booking.thumbnailUrl}
                        alt="Booking"
                        className="w-full h-full object-cover rounded-lg"
                      />
                    </div>
                    <div className="flex flex-col justify-between h-32">
                      <div className="space-y-2">
                        <h3 className="text-lg font-medium">
                          {booking.hotelName}
                        </h3>
                        <div className="text-sm text-gray-600">
                          {booking.checkInDate} ~ {booking.checkOutDate} ·{" "}
                          {booking.roomName}
                        </div>
                        <Badge
                          variant="secondary"
                          className={`${
                            getStatusInfo(booking.bookingStatus).style
                          }`}
                        >
                          {getStatusInfo(booking.bookingStatus).text}
                        </Badge>
                      </div>
                      <div className="text-2xl font-bold">
                        {formatPrice(booking.amount)}
                      </div>
                    </div>
                  </div>

                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button variant="ghost" className="h-8 w-8 p-0">
                        <MoreVertical className="h-4 w-4" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      {booking.bookingStatus === "COMPLETED" && (
                        <DropdownMenuItem
                          onClick={(e) =>
                            handleReviewClick(
                              booking.bookingId,
                              booking.hotelId,
                              booking.roomId,
                              e
                            )
                          }
                        >
                          리뷰 작성
                        </DropdownMenuItem>
                      )}
                      <DropdownMenuItem
                        onClick={(e) => handleCancelClick(booking.bookingId, e)}
                      >
                        예약 취소
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </div>
              </div>
            </Card>
          ))
        ) : (
          <Card className="p-6">
            <div className="text-center text-gray-500">예약이 없습니다.</div>
          </Card>
        )}
      </div>
    </div>
  );
};

export default UserBookingList;
