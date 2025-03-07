import { getHotelList } from "@/lib/api/hotel/BusinessHotelApi";
import { PageDto } from "@/lib/types/PageDto";
import { GetHotelResponse } from "@/lib/types/hotel/GetHotelResponse";
import { FilterName } from "@/lib/enum/FilterName";
import { SeoulDistrict } from "@/lib/enum/SeoulDistriction";
import { FilterDirection } from "@/lib/enum/FilterDirection";
import HotelsPageClient from "./HotelsPage.client";

interface PageProps {
  searchParams: {
    page?: number;
    pageSize?: number;
    filterName?: FilterName;
    streetAddress?: SeoulDistrict;
    checkInDate?: string;
    checkOutDate?: string;
    personal?: string;
    filterDirection?: FilterDirection;
  };
}

export default async function HotelsPage({ searchParams }: PageProps) {
  const params = await Promise.resolve(searchParams);

  const searchParamsWithDefaults = {
    page: params.page ?? 1,
    pageSize: params.pageSize ?? 10,
    filterName: params.filterName ?? FilterName.LATEST,
    streetAddress: params.streetAddress ?? "",
    checkInDate: params.checkInDate ?? new Date().toISOString().split("T")[0],
    checkOutDate:
      params.checkOutDate ??
      new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString().split("T")[0],
    personal: params.personal ?? "2",
    filterDirection: params.filterDirection ?? FilterDirection.DESC,
  };

  const hotelData: PageDto<GetHotelResponse> = await getHotelList(
    searchParamsWithDefaults.page,
    searchParamsWithDefaults.pageSize,
    searchParamsWithDefaults.filterName,
    searchParamsWithDefaults.streetAddress,
    searchParamsWithDefaults.checkInDate,
    searchParamsWithDefaults.checkOutDate,
    parseInt(searchParamsWithDefaults.personal),
    searchParamsWithDefaults.filterDirection
  );

  return (
    <HotelsPageClient
      hotelData={hotelData}
      searchParams={searchParamsWithDefaults}
    />
  );
}
