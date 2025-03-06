import { PageDto } from "@/lib/types/PageDto";
import { BookingResponseSummary } from "@/lib/types/booking/BookingResponseSummary";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Card } from "@/components/ui/card";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { MoreHorizontal } from "lucide-react";
import { useState, useEffect } from "react";
import { cancelBooking, completeBooking } from "@/lib/api/booking/BookingApi";

const HotelBookingList = function ({
  bookings,
}: {
  bookings: PageDto<BookingResponseSummary>;
}) {
  const [bookingList, setBookingList] = useState<BookingResponseSummary[]>(
    bookings.items
  );
  const [isStatusChanged, setIsStatusChanged] = useState(false);

  useEffect(() => {
    setBookingList(bookings.items);
  }, [bookings]);

  useEffect(() => {
    if (isStatusChanged) {
      // 상태가 변경되었을 때 부모 컴포넌트에서 데이터를 새로 불러오도록 할 수 있습니다
      // 예: onStatusChange && onStatusChange();
      setIsStatusChanged(false);
    }
  }, [isStatusChanged]);

  const openBookingDetailsPopup = function (bookingId: number) {
    window.open(
      `/orders/${bookingId}`,
      "OrderPopup",
      "width=500,height=600,left=200,top=200"
    );
  };

  const handleComplete = async function (
    bookingId: number,
    e: React.MouseEvent
  ) {
    e.stopPropagation(); // 이벤트 전파 방지

    if (confirm("예약을 완료 처리하시겠습니까?")) {
      try {
        await completeBooking(bookingId);
        // 로컬 상태 업데이트
        setBookingList((prevList) =>
          prevList.map((booking) =>
            booking.bookingId === bookingId
              ? { ...booking, bookingStatus: "COMPLETED" }
              : booking
          )
        );
        setIsStatusChanged(true);
        alert("예약이 완료 처리되었습니다.");
      } catch (error) {
        alert(error);
      }
    }
  };

  const handleCancel = async function (bookingId: number, e: React.MouseEvent) {
    e.stopPropagation(); // 이벤트 전파 방지

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

  const getStatusText = (status: string) => {
    switch (status) {
      case "COMPLETED":
        return "완료";
      case "CANCELLED":
        return "취소";
      case "CONFIRMED":
        return "확정";
      default:
        return status;
    }
  };

  const getStatusStyle = (status: string) => {
    switch (status) {
      case "COMPLETED":
        return "text-green-600";
      case "CANCELLED":
        return "text-red-600";
      case "CONFIRMED":
        return "text-blue-600";
      default:
        return "text-gray-600";
    }
  };

  return (
    <div className="max-w-5xl mx-auto px-4 md:px-8">
      <Card className="overflow-x-auto shadow-lg">
        <Table>
          <TableHeader>
            <TableRow className="bg-gray-50">
              <TableHead className="p-3 text-center">예약 ID</TableHead>
              <TableHead className="p-3 text-center">투숙객 이름</TableHead>
              <TableHead className="p-3 text-center">객실 유형</TableHead>
              <TableHead className="p-3 text-center">체크인</TableHead>
              <TableHead className="p-3 text-center">체크아웃</TableHead>
              <TableHead className="p-3 text-center">상태</TableHead>
              <TableHead className="p-3 text-center w-16"></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {bookingList.length > 0 ? (
              bookingList.map((booking) => (
                <TableRow
                  key={booking.bookingId}
                  className="hover:bg-gray-50 transition duration-300"
                >
                  <TableCell className="p-3 text-center">
                    {booking.bookingId}
                  </TableCell>
                  <TableCell className="p-3 text-center">
                    {booking.memberName}
                  </TableCell>
                  <TableCell className="p-3 text-center">
                    {booking.roomName}
                  </TableCell>
                  <TableCell className="p-3 text-center">
                    {booking.checkInDate}
                  </TableCell>
                  <TableCell className="p-3 text-center">
                    {booking.checkOutDate}
                  </TableCell>
                  <TableCell className="p-3 text-center">
                    <span className={getStatusStyle(booking.bookingStatus)}>
                      {getStatusText(booking.bookingStatus)}
                    </span>
                  </TableCell>
                  <TableCell className="p-3 text-center">
                    <DropdownMenu>
                      <DropdownMenuTrigger className="focus:outline-none">
                        <div className="p-1 hover:bg-gray-100 rounded-full transition-colors">
                          <MoreHorizontal className="h-5 w-5 text-gray-600" />
                        </div>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end" className="w-32">
                        <DropdownMenuItem
                          onClick={() =>
                            openBookingDetailsPopup(booking.bookingId)
                          }
                          className="cursor-pointer"
                        >
                          자세히
                        </DropdownMenuItem>
                        <DropdownMenuItem
                          onClick={(e) => handleComplete(booking.bookingId, e)}
                          className="cursor-pointer text-green-600"
                        >
                          완료 처리
                        </DropdownMenuItem>
                        <DropdownMenuItem
                          onClick={(e) => handleCancel(booking.bookingId, e)}
                          className="cursor-pointer text-red-600"
                        >
                          예약 취소
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={7} className="text-center p-4">
                  예약 내역이 없습니다.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Card>
    </div>
  );
};

export default HotelBookingList;
