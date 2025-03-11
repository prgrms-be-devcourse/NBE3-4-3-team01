import HotelInfo from "./HotelInfo";
import { GetHotelResponse } from "@/lib/types/hotel/GetHotelResponse";

interface HotelListProps {
  hotels: GetHotelResponse[];
  checkInDate: string;
  checkOutDate: string;
  personal: string;
}

export default function HotelList({
  hotels,
  checkInDate,
  checkOutDate,
  personal,
}: HotelListProps) {
  return (
    <div className="flex flex-col items-center space-y-4 w-full">
      {hotels.map((hotel) => (
        <HotelInfo
          key={hotel.hotelId}
          {...hotel}
          checkInDate={checkInDate}
          checkOutDate={checkOutDate}
          personal={personal}
        />
      ))}
    </div>
  );
}
