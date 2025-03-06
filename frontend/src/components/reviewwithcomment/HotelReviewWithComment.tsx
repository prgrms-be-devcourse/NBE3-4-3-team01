import React, { useState, useEffect } from "react";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Star } from "lucide-react";
import { HotelReviewResponse } from "@/lib/types/review/HotelReviewResponse";
import {
  postReviewComment,
  updateReviewComment,
  deleteReviewComment,
} from "@/lib/api/review/comment/ReviewCommentApi";

interface ReviewWithCommentProps {
  review: HotelReviewResponse;
  isBusinessUser?: boolean;
  onCommentUpdate?: () => void;
}

export const HotelReviewWithComment: React.FC<ReviewWithCommentProps> = ({
  review,
  isBusinessUser = false,
  onCommentUpdate,
}) => {
  const [comment, setComment] = useState("");
  const [isEditing, setIsEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  const {
    hotelReviewWithCommentDto: {
      memberEmail,
      roomTypeName,
      reviewDto,
      reviewCommentDto,
      createdAt,
    },
    imageUrls,
  } = review;

  useEffect(() => {
    if (reviewCommentDto) {
      setComment(reviewCommentDto.content);
      setIsEditing(false);
    } else if (isBusinessUser) {
      setIsEditing(true);
      setComment("");
    }
  }, [reviewCommentDto, isBusinessUser]);

  const handleSaveComment = async () => {
    if (!comment.trim()) return;
    setIsSaving(true);
    try {
      if (reviewCommentDto) {
        await updateReviewComment(
          reviewDto.reviewId,
          reviewCommentDto.reviewCommentId,
          comment
        );
      } else {
        await postReviewComment(reviewDto.reviewId, comment);
      }
      setIsEditing(false);
      onCommentUpdate?.();
    } catch (error) {
      if (error instanceof Error) alert(error.message);
    } finally {
      setIsSaving(false);
    }
  };

  const handleDeleteComment = async () => {
    if (!reviewCommentDto || !window.confirm("답변을 삭제하시겠습니까?"))
      return;
    try {
      await deleteReviewComment(
        reviewDto.reviewId,
        reviewCommentDto.reviewCommentId
      );
      setComment("");
      onCommentUpdate?.();
    } catch (error) {
      if (error instanceof Error) alert(error.message);
    }
  };

  return (
    <Card className="mb-6 shadow-xl rounded-lg border border-gray-200">
      <CardHeader>
        <div className="flex justify-between items-center">
          <div>
            <h3 className="text-lg font-semibold text-gray-800">
              {memberEmail}
            </h3>
            <p className="text-sm text-gray-500">
              {roomTypeName} • {new Date(createdAt).toLocaleDateString()}
            </p>
          </div>
          <div className="flex items-center gap-1">
            {[...Array(5)].map((_, index) => (
              <Star
                key={index}
                className={`w-5 h-5 ${
                  index < reviewDto.rating
                    ? "text-yellow-400 fill-yellow-400"
                    : "text-gray-300"
                }`}
              />
            ))}
          </div>
        </div>
      </CardHeader>
      <CardContent>
        {/* 리뷰 내용 */}
        <div className="mb-4 space-y-2">
          <p className="text-gray-700 leading-relaxed">{reviewDto.content}</p>
          <p className="text-sm text-gray-500">
            {new Date(reviewDto.createdAt).toLocaleString()}
          </p>
        </div>

        {/* 리뷰 이미지 섹션 */}
        {imageUrls.length > 0 && (
          <div className="grid grid-cols-3 gap-2 mt-4">
            {imageUrls.map((url, index) => (
              <img
                key={index}
                src={url}
                alt={`Review image ${index + 1}`}
                className="w-full h-32 object-cover rounded-lg shadow-md"
              />
            ))}
          </div>
        )}

        {/* 댓글 섹션 */}
        <div className="mt-6">
          {isBusinessUser ? (
            isEditing ? (
              <div className="space-y-4">
                <Textarea
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  placeholder="답변을 입력해주세요..."
                  className="w-full min-h-[100px] border border-gray-300 rounded-md"
                />
                <div className="flex gap-2">
                  <Button
                    onClick={handleSaveComment}
                    disabled={!comment.trim() || isSaving}
                    className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg shadow"
                  >
                    {reviewCommentDto ? "수정" : "작성"}
                  </Button>
                  {reviewCommentDto && (
                    <Button
                      variant="outline"
                      onClick={() => {
                        setIsEditing(false);
                        setComment(reviewCommentDto.content);
                      }}
                      className="border border-gray-300 px-4 py-2 rounded-lg shadow"
                    >
                      취소
                    </Button>
                  )}
                </div>
              </div>
            ) : reviewCommentDto ? (
              <div className="relative bg-gray-100 p-4 rounded-lg shadow-md border border-gray-300">
                <p className="text-gray-700">{reviewCommentDto.content}</p>
                <p className="text-sm text-gray-500 mt-2">
                  {new Date(reviewCommentDto.createdAt).toLocaleString()}
                </p>
                <div className="flex justify-end gap-2 mt-2">
                  <Button
                    className="bg-blue-500 hover:bg-blue-600 text-white px-3 py-1.5 rounded-lg shadow"
                    onClick={() => setIsEditing(true)}
                  >
                    수정
                  </Button>
                  <Button
                    variant="destructive"
                    onClick={handleDeleteComment}
                    className="bg-red-500 hover:bg-red-600 text-white px-3 py-1.5 rounded-lg shadow"
                  >
                    삭제
                  </Button>
                </div>
              </div>
            ) : null
          ) : reviewCommentDto ? (
            <div className="relative bg-gray-100 p-4 rounded-lg shadow-md border border-gray-300">
              <p className="text-gray-700">{reviewCommentDto.content}</p>
              <p className="text-sm text-gray-500 mt-2">
                {new Date(reviewCommentDto.createdAt).toLocaleString()}
              </p>
            </div>
          ) : null}
        </div>
      </CardContent>
    </Card>
  );
};

export default HotelReviewWithComment;
