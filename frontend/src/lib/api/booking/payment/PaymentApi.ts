import { UidResponse } from "@/lib/types/booking/payment/UidResponse";

// Uid 및 API Key 발급
export const getUid = async function (): Promise<UidResponse> {
  try {
    const response = await fetch(
      "http://localhost:8080/api/bookings/payments/uid",
      {
        credentials: "include",
      }
    );

    if (!response.ok) {
      const body = await response.text();
      throw new Error(body);
    }

    const rsData = await response.json();
    return rsData.data;
  } catch (error) {
    throw error;
  }
};
