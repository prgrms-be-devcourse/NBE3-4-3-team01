import { BedTypeNumber } from "./BedTypeNumber";

export interface PostRoomRequest {
  roomName: string;
  roomNumber: number;
  basePrice: number;
  standardNumber: number;
  maxNumber: number;
  bedTypeNumber: BedTypeNumber;
  imageExtensions: string[];
  roomOptions: string[];
}
