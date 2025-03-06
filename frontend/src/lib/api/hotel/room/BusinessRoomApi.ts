import { GetAllRoomOptionsResponse } from "@/lib/types/room/GetAllRoomOptionsResponse";
import { GetRoomDetailResponse } from "@/lib/types/room/GetRoomDetailResponse";
import { GetRoomResponse } from "@/lib/types/room/GetRoomResponse";
import { PostRoomResponse } from "@/lib/types/room/PostRoomResponse";
import { PutRoomRequest } from "@/lib/types/room/PutRoomRequest";
import { PutRoomResponse } from "@/lib/types/room/PutRoomResponse";
import { PostRoomRequest } from "@/lib/types/room/PostRoomRequest";

const BASE_URL = "http://localhost:8080/api/hotels";

// 객실 생성
export const createRoom = async (
  hotelId: number,
  postRoomRequest: PostRoomRequest
): Promise<PostRoomResponse> => {
  try {
    const response = await fetch(`${BASE_URL}/${hotelId}/rooms`, {
      credentials: "include",
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(postRoomRequest),
    });

    const rsData = await response
      .clone()
      .json()
      .catch(() => response.text());

    if (!response.ok) {
      throw rsData;
    }

    return rsData.data;
  } catch (error) {
    throw error;
  }
};

// 객실 이미지 저장
export const saveRoomImageUrls = async (
  hotelId: number,
  roomId: number,
  urls: string[]
): Promise<void> => {
  try {
    const response = await fetch(
      `${BASE_URL}/${hotelId}/rooms/${roomId}/urls`,
      {
        credentials: "include",
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(urls),
      }
    );

    if (response.status === 204) {
      return;
    }

    throw await response.text();
  } catch (error) {
    throw error;
  }
};

// 객실 상세 조회
export const findRoomDetail = async (
  hotelId: number,
  roomId: number
): Promise<GetRoomDetailResponse> => {
  try {
    const response = await fetch(`${BASE_URL}/${hotelId}/rooms/${roomId}`);

    const rsData = await response
      .clone()
      .json()
      .catch(() => response.text());

    if (!response.ok) {
      throw rsData;
    }

    return rsData.data;
  } catch (error) {
    throw error;
  }
};

// 객실 수정
export const modifyRoom = async (
  hotelId: number,
  roomId: number,
  request: PutRoomRequest
): Promise<PutRoomResponse> => {
  try {
    const response = await fetch(`${BASE_URL}/${hotelId}/rooms/${roomId}`, {
      credentials: "include",
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    });

    const rsData = await response
      .clone()
      .json()
      .catch(() => response.text());

    if (!response.ok) {
      throw rsData;
    }

    return rsData.data;
  } catch (error) {
    throw error;
  }
};

// 객실 삭제
export const deleteRoom = async (
  hotelId: number,
  roomId: number
): Promise<void> => {
  try {
    const response = await fetch(`${BASE_URL}/${hotelId}/rooms/${roomId}`, {
      credentials: "include",
      method: "DELETE",
    });

    if (response.status === 204) {
      return;
    }

    throw await response.text();
  } catch (error) {
    throw error;
  }
};

// 전체 객실 조회
export const findAllRooms = async (
  hotelId: number
): Promise<GetRoomResponse[]> => {
  try {
    const response = await fetch(`${BASE_URL}/${hotelId}/rooms`);

    const rsData = await response
      .clone()
      .json()
      .catch(() => response.text());

    if (!response.ok) {
      throw rsData;
    }

    return rsData.data;
  } catch (error) {
    throw error;
  }
};

// 전체 객실 옵션 조회
export const findAllRoomOptions = async (
  hotelId: Number
): Promise<GetAllRoomOptionsResponse> => {
  try {
    const response = await fetch(`${BASE_URL}/${hotelId}/rooms/room-option`, {
      credentials: "include",
    });

    const rsData = await response
      .clone()
      .json()
      .catch(() => response.text());

    if (!response.ok) {
      throw rsData;
    }

    return rsData.data;
  } catch (error) {
    throw error;
  }
};
