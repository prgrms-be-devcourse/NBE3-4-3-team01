import { FetchOptions } from "@/lib/types/global/FetchOption";
import { BusinessRegistrationForm } from "@/lib/types/business/BusinessRequest";
import { BusinessRegistrationResult } from "@/lib/types/business/BusinessResponse";
import { fetchAPI } from "../global/FetchApi";

export const registerBusiness = async (
  formData: BusinessRegistrationForm
): Promise<BusinessRegistrationResult> => {
  const options: FetchOptions = {
    method: "POST",
    body: formData,
  };

  return fetchAPI<BusinessRegistrationResult>(
    `http://localhost:8080/api/businesses/register`,
    options
  );
};
