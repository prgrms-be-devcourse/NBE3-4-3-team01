export enum BusinessApprovalStatus {
  PENDING = "PENDING",
  APPROVED = "APPROVED",
  REJECTED = "REJECTED",
}

// 코드 → 한글 변환
export const BusinessApprovalStatusLabels: Record<
  BusinessApprovalStatus,
  string
> = {
  [BusinessApprovalStatus.PENDING]: "승인 대기",
  [BusinessApprovalStatus.APPROVED]: "승인 완료",
  [BusinessApprovalStatus.REJECTED]: "승인 거절",
};

// 한글 → 코드 변환
export const ReverseBusinessApprovalStatusMap = Object.fromEntries(
  Object.entries(BusinessApprovalStatusLabels).map(([key, value]) => [
    value,
    key,
  ])
) as Record<string, BusinessApprovalStatus>;
