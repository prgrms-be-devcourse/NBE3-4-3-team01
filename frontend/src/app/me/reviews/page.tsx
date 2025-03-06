"use client";

import Navigation from "@/components/navigation/Navigation";
import MyReviews from "@/components/reviewwithcomment/MyReviews";
import { useSearchParams } from "next/navigation";

const MyReviewsPage = () => {
  const searchParams = useSearchParams();
  const page = Number(searchParams.get("page")) || 1;

  return (
    <>
      <Navigation />
      <div className="content-wrapper container mx-auto p-4">
        <MyReviews page={page} />
      </div>
    </>
  );
};

export default MyReviewsPage;
