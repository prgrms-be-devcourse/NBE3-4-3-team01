import { MyReviewResponse } from "@/lib/types/review/MyReviewResponse";
import { useEffect, useState } from "react";
import ReviewList from "./ReviewList";
import { getMyReviews } from "@/lib/api/review/ReviewApi";
import { PageDto } from "@/lib/types/PageDto";
import Pagination from "../pagination/Pagination";

const MyReviews: React.FC<{ page: number }> = ({ page }) => {
  const [response, setResponse] = useState<PageDto<MyReviewResponse> | null>(
    null
  );
  const [reviews, setReviews] = useState<MyReviewResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const fetchReviews = async () => {
    try {
      setIsLoading(true);
      const response: PageDto<MyReviewResponse> = await getMyReviews(page);
      setResponse(response);
      setReviews(response.items);
    } catch (error) {
      console.error("리뷰를 불러오는 중 오류가 발생했습니다:", error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchReviews();
  }, [page]);

  if (isLoading) {
    return <div className="text-center">로딩 중...</div>;
  }

  return (
    <>
      <ReviewList reviews={reviews} onReviewDelete={fetchReviews} />
      <Pagination
        currentPage={page}
        totalPages={response?.totalPages || 1}
        basePath="me/reviews"
      />
    </>
  );
};

export default MyReviews;
