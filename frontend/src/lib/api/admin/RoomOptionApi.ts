import { OptionResponse } from "@/lib/types/admin/response/OptionResponse";
import { fetchAPI } from "../global/FetchApi";
import { OptionRequest } from "@/lib/types/admin/request/OptionRequest";
import { FetchOptions } from "@/lib/types/global/FetchOption";

export const addRoomOption = async (
  formData: OptionRequest
): Promise<OptionResponse> => {
  const options: FetchOptions = {
    method: "POST",
    body: formData,
  };

  return fetchAPI<OptionResponse>(
    `http://localhost:8080/api/admin/room-options`,
    options
  );
};

export const getAllRoomOptions = async (): Promise<OptionResponse[]> => {
  const data = await fetchAPI<OptionResponse[]>(
    "http://localhost:8080/api/admin/room-options"
  );

  return data.sort((a, b) => b.optionId - a.optionId);
};

export const getRoomOption = async (
  roomOptionId: number
): Promise<OptionResponse> => {
  return fetchAPI<OptionResponse>(
    `http://localhost:8080/api/admin/room-options/${roomOptionId}`
  );
};

export const modifyRoomOption = async (
  roomOptionId: number,
  formData: OptionRequest
): Promise<OptionResponse> => {
  const options: FetchOptions = {
    method: "PATCH",
    body: formData,
  };

  return fetchAPI<OptionResponse>(
    `http://localhost:8080/api/admin/room-options/${roomOptionId}`,
    options
  );
};

export const deleteRoomOption = async (roomOptionId: number): Promise<void> => {
  const options: FetchOptions = {
    method: "DELETE",
  };

  return fetchAPI<void>(
    `http://localhost:8080/api/admin/room-options/${roomOptionId}`,
    options
  );
};
