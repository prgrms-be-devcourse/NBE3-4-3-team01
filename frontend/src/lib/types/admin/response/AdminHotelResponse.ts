import { HotelStatus } from "../../hotel/HotelStatus";

export interface HotelApprovalResult {
  name: string;
  status: HotelStatus;
}

export interface AdminHotelSummaryReponse {
  hotelId: number;
  name: string;

  streetAddress: string;
  status: HotelStatus;

  ownerName: string;
}

export interface AdminHotelDetailResponse {
  hotelName: string;
  streetAddress: string;
  zipCode: number;
  hotelGrade: string;
  checkInTime: string;
  checkOutTime: string;
  hotelStatus: HotelStatus;

  hotelEmail: string;
  hotelPhoneNumber: string;

  ownerId: number;
  ownerName: string;
  businessRegistrationNumber: string;
  startDate: string;

  averageRating: number;
  totalReviewCount: number;

  hotelOptions: Set<string>;
}
