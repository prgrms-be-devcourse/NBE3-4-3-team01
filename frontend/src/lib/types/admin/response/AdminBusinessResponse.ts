import { BusinessApprovalStatus } from "../../business/BusinessApprovalStatus";
import { MemberStatus } from "../../member/MemberStatus";
import { HotelStatus } from "../../hotel/HotelStatus";

export interface AdminBusinessApprovalReponse {
  businessId: number;
  businessRegistrationNumber: string;
  startDate: string; // LocalDate → ISO 8601 문자열 (예: "2025-02-07")
  ownerName: string;
  approvalStatus: BusinessApprovalStatus;
}

export interface AdminBusinessSummaryReponse {
  businessId: number;
  ownerName: string;
  contact: string;
  approvalStatus: BusinessApprovalStatus;
  hotelName?: string | null;
}

export interface AdminBusinessDetailResponse {
  businessId: number;
  businessRegistrationNumber: string;
  ownerName: string;
  startDate: string;

  memberEmail: string;
  memberName: string;
  memberPhoneNumber: string;
  memberStatus: MemberStatus;

  approvalStatus: BusinessApprovalStatus;

  hotelId?: number | null;
  hotelName?: string | null;
  streetAddress?: string | null;
  hotelStatus?: HotelStatus | null;
}
