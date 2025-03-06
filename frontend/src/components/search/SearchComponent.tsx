"use client";

import { useState, useRef, useEffect } from "react";
import { Search, Users, Minus, Plus, Calendar, MapPin } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import {
  DatePickerWithRange,
  DatePickerWithRangeRef,
} from "@/components/ui/date-range-picker";
import { useRouter } from "next/navigation";
import { SeoulDistrict } from "@/lib/enum/SeoulDistriction";

interface SearchComponentProps {
  className?: string;
}

const REGIONS = [...new Set(Object.values(SeoulDistrict))].map(
  (district, index) => ({
    id: index,
    name: district,
    fullAddress: `${district}`,
  })
);

export default function SearchComponent({
  className = "",
}: SearchComponentProps) {
  const [location, setLocation] = useState("");
  const [adults, setAdults] = useState(2);
  const [showGuestSelector, setShowGuestSelector] = useState(false);
  const [showLocationSearch, setShowLocationSearch] = useState(false);
  const [filteredRegions, setFilteredRegions] = useState(REGIONS);
  const router = useRouter();
  const datePickerRef = useRef<DatePickerWithRangeRef>(null);
  const [errors, setErrors] = useState<{
    location?: string;
    date?: string;
  }>();

  const today = new Date();
  const tomorrow = new Date(today);
  tomorrow.setDate(today.getDate() + 1);

  const [selectedRange, setSelectedRange] = useState<{
    start?: Date;
    end?: Date;
  }>({
    start: today,
    end: tomorrow,
  });

  const handleSearch = () => {
    if (!location) {
      setErrors({ location: "목적지를 선택해주세요." });
      setShowLocationSearch(true);
      setTimeout(() => {
        setErrors({});
      }, 3000);
      return;
    }

    if (!selectedRange.start || !selectedRange.end) {
      setErrors({ date: "체크인/체크아웃 날짜를 선택해주세요." });
      setTimeout(() => setErrors({}), 3000);
      return;
    }

    // location이 SeoulDistrict.DEFAULT일 경우 빈 문자열로 설정
    const streetAddress = location === SeoulDistrict.DEFAULT ? "" : location;

    const searchParams = new URLSearchParams({
      streetAddress: streetAddress,
      checkInDate: selectedRange.start.toISOString().split("T")[0],
      checkoutDate: selectedRange.end.toISOString().split("T")[0],
      personal: adults.toString(),
    });

    router.push(`/hotels?${searchParams.toString()}`);
  };

  const handleLocationSearch = (value: string) => {
    setLocation(value);
    setFilteredRegions(
      REGIONS.filter(
        (region) =>
          region.name.toLowerCase().includes(value.toLowerCase()) ||
          region.fullAddress.toLowerCase().includes(value.toLowerCase())
      )
    );
  };

  const handleLocationSelect = (region: (typeof REGIONS)[0]) => {
    setLocation(region.fullAddress);
    setShowLocationSearch(false);
    datePickerRef.current?.open();
  };

  const handleDateSelect = (range: { start?: Date; end?: Date }) => {
    setSelectedRange(range);
    if (range.start && range.end) {
      setShowGuestSelector(true);
    }
  };

  const handleAdultsChange = (delta: number) => {
    const newValue = adults + delta;
    if (newValue >= 1 && newValue <= 10) {
      setAdults(newValue);
    }
  };

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        showLocationSearch &&
        !(event.target as HTMLElement).closest(".location-search")
      ) {
        setShowLocationSearch(false);
      }

      if (
        showGuestSelector &&
        !(event.target as HTMLElement).closest(".guest-selector")
      ) {
        setShowGuestSelector(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [showLocationSearch, showGuestSelector]);

  return (
    <Card className={`${className} shadow-lg`}>
      <CardContent className="p-4">
        <div className="flex items-center gap-4">
          {/* 위치 검색 */}
          <div className="location-search relative flex-1">
            <div className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500 pointer-events-none">
              <Search size={20} />
            </div>
            <Input
              type="text"
              placeholder="목적지가 어디인가요?"
              value={location}
              onChange={(e) => handleLocationSearch(e.target.value)}
              onFocus={() => setShowLocationSearch(true)}
              className={`pl-10 ${location ? "bg-gray-100" : ""} ${
                errors?.location ? "border-red-500" : ""
              }`}
            />
            {errors?.location && (
              <div className="absolute z-20 w-full -top-12">
                <div className="relative bg-black/80 rounded-lg p-2 text-white">
                  <div
                    className="absolute bottom-[-6px] left-5 
                    border-l-[6px] border-r-[6px] border-t-[6px] 
                    border-l-transparent border-r-transparent border-t-black/80"
                  ></div>
                  <div className="relative z-10 flex items-center gap-2 text-sm">
                    <Search size={16} className="text-white" />
                    {errors.location}
                  </div>
                </div>
              </div>
            )}
            {showLocationSearch && (
              <Card className="absolute z-10 w-full mt-1">
                <CardContent className="p-2 max-h-60 overflow-auto">
                  {filteredRegions.map((region) => (
                    <div
                      key={region.id}
                      className="p-2 hover:bg-gray-100 cursor-pointer rounded flex items-center gap-2"
                      onClick={() => handleLocationSelect(region)}
                    >
                      <MapPin size={18} className="text-gray-400" />
                      {region.fullAddress}
                    </div>
                  ))}
                </CardContent>
              </Card>
            )}
          </div>

          <div className="flex-1 relative">
            <div className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500 pointer-events-none z-10">
              <Calendar size={20} />
            </div>
            <DatePickerWithRange
              ref={datePickerRef}
              selectedRange={selectedRange}
              onRangeChange={handleDateSelect}
              onOpen={() => setShowGuestSelector(false)}
              className={`pl-10 ${
                selectedRange.start && selectedRange.end ? "bg-gray-100" : ""
              }`}
            />
            {errors?.date && (
              <p className="text-red-500 text-sm mt-1">{errors.date}</p>
            )}
          </div>

          <div className="guest-selector relative w-32">
            <Button
              variant="outline"
              className={`w-full justify-between ${
                adults >= 1 && !showGuestSelector ? "bg-gray-100" : ""
              }`}
              onClick={() => setShowGuestSelector(!showGuestSelector)}
            >
              <span className="flex items-center gap-2">
                <Users size={20} />
                인원 {adults}명
              </span>
            </Button>

            {showGuestSelector && (
              <Card className="absolute z-10 w-48 mt-1 right-0">
                <CardContent className="p-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600">인원</span>
                    <div className="flex items-center gap-2">
                      <Button
                        variant="outline"
                        size="icon"
                        onClick={() => handleAdultsChange(-1)}
                        disabled={adults <= 1}
                      >
                        <Minus size={16} />
                      </Button>
                      <span className="w-8 text-center">{adults}</span>
                      <Button
                        variant="outline"
                        size="icon"
                        onClick={() => handleAdultsChange(1)}
                        disabled={adults >= 10}
                      >
                        <Plus size={16} />
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            )}
          </div>

          <Button
            onClick={handleSearch}
            className="w-24 bg-blue-500 hover:bg-blue-600"
          >
            <Search className="mr-2" size={20} />
            검색
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
