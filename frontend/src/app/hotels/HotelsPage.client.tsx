"use client";

import { useState, Suspense } from "react";
import { useRouter } from "next/navigation";
import HotelList from "@/components/hotellist/HotelList";
import Loading from "@/components/hotellist/Loading";
import Pagination from "@/components/pagination/Pagination";
import Navigation from "@/components/navigation/Navigation";
import { FilterName } from "@/lib/enum/FilterName";
import {
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
} from "@/components/ui/select";
import { PageDto } from "@/lib/types/PageDto";
import { GetHotelResponse } from "@/lib/types/hotel/GetHotelResponse";
import SearchComponent from "@/components/search/SearchComponent";

interface HotelsPageClientProps {
  hotelData: PageDto<GetHotelResponse>;
  searchParams: {
    page: number;
    pageSize: number;
    filterName: FilterName;
    streetAddress: string;
    checkInDate: string;
    checkoutDate: string;
    personal: string;
    filterDirection: string;
  };
}

export default function HotelsPageClient({
  hotelData,
  searchParams,
}: HotelsPageClientProps) {
  const router = useRouter();

  const { page = 1, filterName = FilterName.LATEST } = searchParams;

  const [selectedFilter, setSelectedFilter] = useState<FilterName>(filterName);

  const stringifiedSearchParams: Record<string, string> = {
    page: String(searchParams.page),
    pageSize: String(searchParams.pageSize),
    filterName: String(searchParams.filterName),
    streetAddress: searchParams.streetAddress,
    checkInDate: searchParams.checkInDate,
    checkoutDate: searchParams.checkoutDate,
    personal: searchParams.personal,
    filterDirection: searchParams.filterDirection,
  };

  const params = new URLSearchParams(stringifiedSearchParams);
  params.delete("page");

  const handleFilterChange = (value: FilterName) => {
    params.set("filterName", value);
    params.set("page", stringifiedSearchParams.page);
    setSelectedFilter(value);
    router.push(`?${params.toString()}`);
    console.log(value);
  };

  return (
    <main className="container max-w-6xl mx-auto px-4 py-8 pb-20">
      <Navigation />
      <div className="content-wrapper flex items-center justify-between gap-4 mb-6">
        <h1 className="text-2xl font-bold whitespace-nowrap">호텔 목록</h1>
        <div className="flex-grow">
          <SearchComponent />
        </div>
        <Select value={selectedFilter} onValueChange={handleFilterChange}>
          <SelectTrigger className="w-40 px-4 py-2 border rounded-md bg-white whitespace-nowrap">
            <SelectValue placeholder="정렬 기준" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={FilterName.LATEST}>최신순</SelectItem>
            <SelectItem value={FilterName.AVERAGE_RATING}>
              리뷰 점수순
            </SelectItem>
            <SelectItem value={FilterName.REVIEW_COUNT}>리뷰 개수순</SelectItem>
          </SelectContent>
        </Select>
      </div>
      <Suspense fallback={<Loading />}>
        <HotelList
          hotels={hotelData.items}
          checkInDate={searchParams.checkInDate}
          checkoutDate={searchParams.checkoutDate}
          personal={searchParams.personal}
        />
      </Suspense>
      <Pagination
        currentPage={page}
        totalPages={hotelData?.totalPages || 1}
        basePath={`hotels?${params.toString()}`}
      />
    </main>
  );
}
