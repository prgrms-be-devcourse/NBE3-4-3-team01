import { Empty } from "../../types/Empty";
import { GetAllHotelOptionResponse } from "../../types/hotel/GetAllHotelOptionResponse";
import { GetHotelDetailResponse } from "../../types/hotel/GetHotelDetailResponse";
import { GetHotelRevenueResponse } from "../../types/hotel/GetHotelRevenueResponse";
import { PostHotelResponse } from "@/lib/types/hotel/PostHotelResponse";
import { PutHotelRequest } from "@/lib/types/hotel/PutHotelRequest";
import { PutHotelResponse } from "@/lib/types/hotel/PutHotelResponse";
import { PostHotelRequest } from "@/lib/types/hotel/PostHotelRequest";
import { RsData } from "../../types/RsData";
import { PageDto } from "../../types/PageDto";
import { GetHotelResponse } from "../../types/hotel/GetHotelResponse";

const BASE_URL = "http://localhost:8080/api/hotels";

// 호텔 생성
export const createHotel = async (
  postHotelRequest: PostHotelRequest
): Promise<PostHotelResponse> => {
  try {
    const response = await fetch(`${BASE_URL}`, {
      credentials: "include",
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(postHotelRequest),
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

// 호텔 이미지 URL 저장
export const saveHotelImageUrls = async (
  hotelId: number,
  urls: string[]
): Promise<void> => {
  try {
    const response = await fetch(`${BASE_URL}/${hotelId}/urls`, {
      credentials: "include",
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(urls),
    });

    if (response.status === 204) {
      return;
    }

    throw await response.text();
  } catch (error) {
    throw error;
  }
};

// 호텔 상세 조회
export const findHotelDetail = async (
  hotelId: number
): Promise<GetHotelDetailResponse> => {
  try {
    const response = await fetch(`${BASE_URL}/${hotelId}/business`);

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

// 예약 가능한 객실이 존재하는 호텔 상세 조회
export const findHotelDetailWithAvailableRooms = async (
  hotelId: number,
  checkInDate?: string,
  checkoutDate?: string,
  personal?: string
): Promise<GetHotelDetailResponse> => {
  try {
    const params = new URLSearchParams();
    if (checkInDate) params.set("checkInDate", checkInDate);
    if (checkoutDate) params.set("checkoutDate", checkoutDate);
    if (personal) params.set("personal", personal);

    const response = await fetch(`${BASE_URL}/${hotelId}?${params.toString()}`);

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

// 호텔 수정
export const modifyHotel = async (
  hotelId: number,
  request: PutHotelRequest
): Promise<PutHotelResponse> => {
  try {
    const response = await fetch(`${BASE_URL}/${hotelId}`, {
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

// 호텔 삭제
export const deleteHotel = async (hotelId: number): Promise<void> => {
  try {
    const response = await fetch(`${BASE_URL}/${hotelId}`, {
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

// 호텔 매출 조회
export const findHotelRevenue = async (
  hotelId: number
): Promise<GetHotelRevenueResponse> => {
  try {
    const response = await fetch(`${BASE_URL}/${hotelId}/revenue`);

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

// 전체 호텔 옵션 조회
export const findAllHotelOptions =
  async (): Promise<GetAllHotelOptionResponse> => {
    try {
      const response = await fetch(`${BASE_URL}/hotel-option`, {
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

// 호텔 목록 획득 API
export const getHotelList = async (
  page: number,
  pageSize: number,
  filterName: string,
  streetAddress: string,
  checkInDate: string,
  checkoutDate: string,
  personal: number,
  filterDirection?: string
): Promise<PageDto<GetHotelResponse>> => {
  const params = new URLSearchParams({
    page: page.toString(),
    pageSize: pageSize.toString(),
    filterName: filterName,
    streetAddress: streetAddress,
    checkInDate: checkInDate,
    checkoutDate: checkoutDate,
    personal: personal.toString(),
  });

  if (filterDirection) {
    params.append("filterDirection", filterDirection);
  }

  const url = `http://localhost:8080/api/hotels?${params.toString()}`;

  try {
    console.log(url);
    const response = await fetch(url);
    const rsData: RsData<PageDto<GetHotelResponse>> = await response.json();
    console.log(rsData);
    if (!response.ok) {
      throw new Error(rsData.msg);
    }
    return rsData.data;
  } catch (error) {
    console.error("Error:", error);
    throw error;
  }
};
