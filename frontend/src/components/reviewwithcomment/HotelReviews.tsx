import { HotelReviewResponse } from "@/lib/types/review/HotelReviewResponse";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import ReviewList from "./ReviewList";
import { getHotelReviews } from "@/lib/api/review/ReviewApi";
import { HotelReviewListResponse } from "@/lib/types/review/HotelReviewListResponse";
import { PageDto } from "@/lib/types/PageDto";
import Pagination from "../pagination/Pagination";
import { Star } from "lucide-react";
import { Button } from "@/components/ui/button"; // Button 컴포넌트 가져오기
import { MoveLeft } from "lucide-react"; // MoveLeft 아이콘

interface HotelReviewsProps {
  hotelId: number;
  page: number;
  isBusinessUser?: boolean;
}

// 호텔 리뷰 조회
const HotelReviews: React.FC<HotelReviewsProps> = ({
  hotelId,
  page,
  isBusinessUser = false,
}) => {
  const [response, setResponse] = useState<HotelReviewListResponse | null>(
    null
  );
  const [reviewPage, setReviewPage] =
    useState<PageDto<HotelReviewResponse> | null>(null);
  const [reviews, setReviews] = useState<HotelReviewResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  const fetchReviews = async () => {
    try {
      setIsLoading(true);
      const response: HotelReviewListResponse = await getHotelReviews(
        hotelId,
        page
      );
      const reviewPage: PageDto<HotelReviewResponse> = response.hotelReviewPage;
      setResponse(response);
      setReviewPage(reviewPage);
      setReviews(reviewPage.items);
    } catch (error) {
      if (error instanceof Error) {
        alert(error.message);
      }
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchReviews();
  }, [hotelId, page]);

  if (isLoading) {
    return <div className="text-center">로딩 중...</div>;
  }

  return (
    <>
      <div className="max-w-3xl mx-auto p-4 flex items-center justify-between">
        {/* 뒤로가기 버튼 */}
        <Button
          variant="ghost"
          onClick={() => router.back()}
          className="hover:bg-gray-100 gap-2"
        >
          <MoveLeft className="w-5 h-5 text-gray-600" />
          <span className="text-gray-600">뒤로가기</span>
        </Button>

        {/* 평균 리뷰 점수 */}
        <div className="text-center flex-grow">
          <h2 className="text-lg text-gray-600 mb-3">평균 리뷰 점수</h2>
          <div className="flex items-center justify-center gap-3">
            <span className="text-4xl font-bold text-blue-600">
              {response?.averageRating?.toFixed(1)}
            </span>
            <div className="flex items-center">
              {[...Array(5)].map((_, i) => (
                <Star
                  key={i}
                  className={`w-6 h-6 ${
                    i < Math.floor(response?.averageRating || 0)
                      ? "text-yellow-400 fill-yellow-400"
                      : "text-gray-300"
                  }`}
                />
              ))}
            </div>
          </div>
        </div>
      </div>
      <ReviewList
        reviews={reviews}
        isBusinessUser={isBusinessUser}
        onCommentUpdate={fetchReviews}
      />
      <Pagination
        currentPage={page}
        totalPages={reviewPage?.totalPages || 1}
        basePath={`hotels/${hotelId}/reviews`}
      />
    </>
  );
};

export default HotelReviews;
