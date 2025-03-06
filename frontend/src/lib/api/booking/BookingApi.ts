import { BookingFormResponse } from "@/lib/types/booking/BookingFormResponse";
import { BookingRequest } from "@/lib/types/booking/BookingRequest";
import { BookingResponseDetails } from "@/lib/types/booking/BookingResponseDetails";
import { BookingResponseSummary } from "@/lib/types/booking/BookingResponseSummary";
import { PageDto } from "@/lib/types/PageDto";

// 예약 페이지 정보 조회
export const preBook = async function (
  hotelId: number,
  roomId: number
): Promise<BookingFormResponse> {
  try {
    const params = new URLSearchParams();
    params.append("hotelId", hotelId.toString());
    params.append("roomId", roomId.toString());

    const response = await fetch(
      `http://localhost:8080/api/bookings?${params.toString()}`,
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

// 예약 및 결제
export const book = async function (bookingRequest: BookingRequest) {
  try {
    const response = await fetch("http://localhost:8080/api/bookings", {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(bookingRequest),
    });

    if (!response.ok) {
      const body = await response.text();
      throw new Error(body);
    }

    return;
  } catch (error) {
    throw error;
  }
};

// 내 예약 조회 (사용자측)
export const getMyBookings = async function (
  page?: number,
  pageSize?: number
): Promise<PageDto<BookingResponseSummary>> {
  try {
    const params = new URLSearchParams();

    if (page) {
      params.append("page", page.toString());
    }
    if (pageSize) {
      params.append("page_size", pageSize.toString());
    }

    const response = await fetch(
      `http://localhost:8080/api/bookings/me?${params.toString()}`,
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

// 호텔 예약 조회 (사업자측)
export const getHotelBookings = async function (
  page?: number,
  pageSize?: number
): Promise<PageDto<BookingResponseSummary>> {
  try {
    const params = new URLSearchParams();

    if (page) {
      params.append("page", page.toString());
    }
    if (pageSize) {
      params.append("page_size", pageSize.toString());
    }

    const response = await fetch(
      `http://localhost:8080/api/bookings/myHotel?${params.toString()}`,
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

// 예약 상세 조회
export const getBookingDetails = async function (
  bookingId: number
): Promise<BookingResponseDetails> {
  try {
    const response = await fetch(
      `http://localhost:8080/api/bookings/${bookingId}`,
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

// 예약 취소
export const cancelBooking = async function (bookingId: number) {
  try {
    const response = await fetch(
      `http://localhost:8080/api/bookings/${bookingId}`,
      {
        method: "DELETE",
        credentials: "include",
      }
    );

    if (!response.ok) {
      const body = await response.text();
      throw new Error(body);
    }

    return;
  } catch (error) {
    throw error;
  }
};

// 예약 완료 처리
export const completeBooking = async function (bookingId: number) {
  try {
    const response = await fetch(
      `http://localhost:8080/api/bookings/${bookingId}`,
      {
        method: "PATCH",
        credentials: "include",
      }
    );

    if (!response.ok) {
      const body = await response.text();
      throw new Error(`${body}`);
    }

    return;
  } catch (error) {
    throw error;
  }
};
