import { BusinessApprovalStatus } from "./BusinessApprovalStatus";

export interface BusinessRegistrationResult {
  businessId: number;
  businessRegistrationNumber: string;
  startDate: string;
  ownerName: string;
  approvalStatus: BusinessApprovalStatus;
}
