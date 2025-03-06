import { BedTypeNumber } from "./BedTypeNumber";

export interface RoomDto {
  id: number;
  hotelId: number;
  roomName: string;
  roomNumber: number;
  basePrice: number;
  standardNumber: number;
  maxNumber: number;
  bedTypeNumber: BedTypeNumber;
  roomStatus: string;
  roomOptions: Set<string>;
}
