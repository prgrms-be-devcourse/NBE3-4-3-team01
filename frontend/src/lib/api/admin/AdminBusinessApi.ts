import {
  AdminBusinessApprovalReponse,
  AdminBusinessDetailResponse,
  AdminBusinessSummaryReponse,
} from "@/lib/types/admin/response/AdminBusinessResponse";
import { fetchAPI } from "../global/FetchApi";
import { AdminBusinessRequest } from "@/lib/types/admin/request/AdminBusinessRequest";
import { FetchOptions } from "@/lib/types/global/FetchOption";
import { PageDto } from "@/lib/types/PageDto";

export const getAllBusinesses = async (
  page: number = 0
): Promise<PageDto<AdminBusinessSummaryReponse>> => {
  const data = await fetchAPI<PageDto<AdminBusinessSummaryReponse>>(
    `http://localhost:8080/api/admin/businesses?page=${page}`
  );

  return {
    ...data,
    items: data.items.sort((a, b) => b.businessId - a.businessId),
  };
};

export const getBusiness = async (
  businessId: number
): Promise<AdminBusinessDetailResponse> => {
  return fetchAPI<AdminBusinessDetailResponse>(
    `http://localhost:8080/api/admin/businesses/${businessId}`
  );
};

export const modifyBusiness = async (
  businessId: number,
  formData: AdminBusinessRequest
): Promise<AdminBusinessApprovalReponse> => {
  const options: FetchOptions = {
    method: "PATCH",
    body: formData,
  };

  return fetchAPI<AdminBusinessApprovalReponse>(
    `http://localhost:8080/api/admin/businesses/${businessId}`,
    options
  );
};
