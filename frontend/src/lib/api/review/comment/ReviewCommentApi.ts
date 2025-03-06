// 리뷰 답변 생성
export const postReviewComment = async (reviewId: number, content: string) => {
  try {
    const response = await fetch(
      `http://localhost:8080/api/reviews/${reviewId}/comments`,
      {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ content }),
      }
    );

    if (response.status === 204) {
      return;
    }

    if (!response.ok) {
      throw new Error(await response.text());
    }
  } catch (error) {
    throw error;
  }
};

// 리뷰 답변 삭제
export const deleteReviewComment = async (
  reviewId: number,
  commentId: number
) => {
  try {
    const response = await fetch(
      `http://localhost:8080/api/reviews/${reviewId}/comments/${commentId}`,
      {
        method: "DELETE",
        credentials: "include",
      }
    );

    if (response.status === 204) {
      return;
    }

    if (!response.ok) {
      throw new Error(await response.text());
    }
  } catch (error) {
    throw error;
  }
};

// 리뷰 답변 수정
export const updateReviewComment = async (
  reviewId: number,
  commentId: number,
  content: string
) => {
  try {
    const response = await fetch(
      `http://localhost:8080/api/reviews/${reviewId}/comments/${commentId}`,
      {
        method: "PUT",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ content }),
      }
    );

    if (response.status === 204) {
      return;
    }

    if (!response.ok) {
      throw new Error(await response.text());
    }
  } catch (error) {
    throw error;
  }
};
