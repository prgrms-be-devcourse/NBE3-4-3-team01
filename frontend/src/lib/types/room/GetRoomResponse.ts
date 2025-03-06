import { BedTypeNumber } from "./BedTypeNumber";

export interface GetRoomResponse {
  roomId: number;
  roomName: string;
  basePrice: number;
  standardNumber: number;
  maxNumber: number;
  bedTypeNumber: BedTypeNumber;
  thumbnailUrl: string;
  roomNumber: number;
}
