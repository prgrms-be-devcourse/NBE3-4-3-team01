import { BedTypeNumber } from "./BedTypeNumber";

export interface PutRoomRequest {
  roomName: string;
  roomNumber: number;
  basePrice: number;
  standardNumber: number;
  maxNumber: number;
  bedTypeNumber: BedTypeNumber;
  roomStatus: string;
  deleteImageUrls: string[];
  imageExtensions: string[];
  roomOptions: string[];
}
