"use client";

import { useEffect, useState } from "react";
import { getFavorites, removeFavorite } from "@/lib/api/member/FavoriteApi";
import { FavoriteHotelDto } from "@/lib/types/FavoriteHotelDto";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import Link from "next/link";
import { Heart, Hotel, Star, Trash2 } from "lucide-react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import Navigation from "@/components/navigation/Navigation";

interface Alert {
  type: "success" | "error" | "warning";
  message: string;
}

export default function FavoritesPage() {
  const [favorites, setFavorites] = useState<FavoriteHotelDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [alert, setAlert] = useState<Alert | null>(null);
  const router = useRouter();

  useEffect(() => {
    const fetchFavorites = async () => {
      try {
        setIsLoading(true);
        const response = await getFavorites();
        setFavorites(response.data);
      } catch (error) {
        if (error instanceof Error) {
          if (error.message.includes("로그인이 필요합니다")) {
            router.push("/login");
            return;
          }
          setAlert({
            type: "error",
            message: error.message,
          });
        } else {
          setAlert({
            type: "error",
            message: "즐겨찾기 목록을 불러오는데 실패했습니다.",
          });
        }
      } finally {
        setIsLoading(false);
      }
    };

    fetchFavorites();
  }, [router]);

  const handleRemoveFavorite = async (e: React.MouseEvent, hotelId: number) => {
    e.preventDefault();
    try {
      const response = await removeFavorite(hotelId);
      setFavorites(favorites.filter((hotel) => hotel.hotelId !== hotelId));
      setAlert({
        type: "warning",
        message: "즐겨찾기가 삭제되었습니다.",
      });

      setTimeout(() => {
        setAlert(null);
      }, 3000);
    } catch (error) {
      if (error instanceof Error) {
        setAlert({
          type: "error",
          message: error.message,
        });
      }
    }
  };

  if (isLoading) {
    return (
      <>
        <Navigation />
        <div className="content-wrapper flex items-center justify-center min-h-[400px]">
          <div className="animate-pulse text-lg">로딩 중...</div>
        </div>
      </>
    );
  }

  return (
    <>
      <Navigation />
      <div className="content-wrapper container mx-auto p-4">
        <div className="flex items-center gap-2 mb-8">
          <Heart className="w-8 h-8 text-red-500" />
          <h1 className="text-2xl font-bold">내 즐겨찾기</h1>
        </div>

        {alert && (
          <div
            className={`text-center mb-4 p-3 rounded-lg ${
              alert.type === "success"
                ? "bg-green-100 text-green-700 border border-green-200"
                : alert.type === "warning"
                ? "bg-yellow-100 text-yellow-700 border border-yellow-200"
                : "bg-red-100 text-red-700 border border-red-200"
            }`}
          >
            {alert.message}
          </div>
        )}

        {favorites.length === 0 ? (
          <div className="text-center text-gray-500 mt-10">
            <Heart className="mx-auto w-16 h-16 text-gray-300 mb-4" />
            <p className="text-xl font-semibold">즐겨찾기한 호텔이 없습니다.</p>
            <p className="text-gray-400 mt-2">
              마음에 드는 호텔을 즐겨찾기에 추가해보세요!
            </p>
            <Link
              href="/"
              className="inline-block mt-4 px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
            >
              호텔 둘러보기
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {favorites.map((hotel) => (
              <Link key={hotel.hotelId} href={`/hotels/${hotel.hotelId}`}>
                <Card className="hover:shadow-lg transition-all duration-300 hover:-translate-y-1">
                  <CardHeader>
                    <CardTitle className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <Hotel className="w-5 h-5 text-blue-500" />
                        {hotel.hotelName}
                      </div>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="text-red-500 hover:text-red-600 hover:bg-red-50"
                        onClick={(e) => handleRemoveFavorite(e, hotel.hotelId)}
                      >
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-sm text-gray-600 mb-2">
                      {hotel.streetAddress}
                    </p>
                    <div className="flex items-center gap-1 text-yellow-500">
                      <Star className="w-4 h-4 fill-current" />
                      <span>{hotel.hotelGrade}</span>
                    </div>
                  </CardContent>
                </Card>
              </Link>
            ))}
          </div>
        )}
      </div>
    </>
  );
}
