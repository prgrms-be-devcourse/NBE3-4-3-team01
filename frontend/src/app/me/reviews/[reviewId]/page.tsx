"use client";

import { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { PresignedUrlsResponse } from "@/lib/types/review/PresignedUrlsResponse";
import { UpdateReviewRequest } from "@/lib/types/review/UpdateReviewRequest";
import { fetchReview, updateReview } from "@/lib/api/review/ReviewApi";
import { uploadImagesToS3 } from "@/lib/api/aws/AwsS3Api";
import { uploadImageUrls } from "@/lib/api/review/ReviewApi";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { MoveLeft, Star, XCircle } from "lucide-react";

export default function CreatePage() {
  const [content, setContent] = useState("");
  const [rating, setRating] = useState(0);
  const [existingImages, setExistingImages] = useState<string[]>([]);
  const [deleteImageUrls, setDeleteImageUrls] = useState<string[]>([]);
  const [newImages, setNewImages] = useState<File[]>([]);
  const [newImagePreviews, setNewImagePreviews] = useState<string[]>([]);
  const [newImageExtensions, setNewImageExtensions] = useState<string[]>([]);
  const [presignedUrls, setPresignedUrls] = useState<string[]>([]);
  const [reviewId, setReviewId] = useState(0);
  const params = useParams();
  const router = useRouter();

  useEffect(() => {
    const fetchReviewData = async () => {
      try {
        const response = await fetchReview(Number(params.reviewId));
        setReviewId(Number(params.reviewId));
        setContent(response.reviewDto.content);
        setRating(response.reviewDto.rating);
        setExistingImages(response.imageUrls);
      } catch (error) {
        console.error("Failed to fetch review:", error);
      }
    };

    fetchReviewData();
  }, [params.reviewId]);

  const handleImageDelete = (imageUrl: string) => {
    setExistingImages(existingImages.filter((img) => img !== imageUrl));
    setDeleteImageUrls([...deleteImageUrls, imageUrl]);
  };

  const handleNewImageDelete = (index: number) => {
    setNewImages(newImages.filter((_, i) => i !== index));
    setNewImagePreviews(newImagePreviews.filter((_, i) => i !== index));
    setNewImageExtensions(newImageExtensions.filter((_, i) => i !== index));
  };

  const handleNewImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const files = Array.from(e.target.files);
      setNewImages([...newImages, ...files]);

      const newPreviews = files.map((file) => URL.createObjectURL(file));
      setNewImagePreviews([...newImagePreviews, ...newPreviews]);

      const extensions = files.map((file) => {
        const ext = file.name.split(".").pop()?.toLowerCase() || "";
        return ext;
      });
      setNewImageExtensions([...newImageExtensions, ...extensions]);
    }
  };

  useEffect(() => {
    return () => {
      newImagePreviews.forEach((preview) => URL.revokeObjectURL(preview));
    };
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const UpdateReviewRequest: UpdateReviewRequest = {
      content,
      rating,
      deleteImageUrls,
      newImageExtensions,
    };

    try {
      const response: PresignedUrlsResponse = await updateReview(
        reviewId,
        UpdateReviewRequest
      );
      console.log("서버 응답:", response);

      setPresignedUrls(response.presignedUrls);
      console.log("리뷰 내용, 레이팅이 성공적으로 수정되었습니다.");

      if (newImages.length === 0) {
        console.log("이미지 없이 리뷰만 수정하였음");
        alert("리뷰가 수정되었습니다.");
        router.push("/me/reviews");
      }
    } catch (error) {
      console.error("Error:", error);
      alert("리뷰 생성 또는 이미지 업로드 중 오류가 발생했습니다.");
    }
  };

  useEffect(() => {
    if (presignedUrls.length > 0) {
      console.log("presignedUrls 설정됨:", presignedUrls);
      submitImages();
    }
  }, [presignedUrls]);

  const submitImages = async () => {
    try {
      await uploadImagesToS3(presignedUrls, newImages);
      await saveImageUrls();
      console.log("이미지가 성공적으로 업로드되었습니다.");
    } catch (error) {
      console.error("Error:", error);
      alert("이미지 업로드 중 오류가 발생했습니다.");
    }
  };

  const saveImageUrls = async () => {
    const viewUrls = presignedUrls.map((presignedUrl) => {
      return presignedUrl.split("?")[0];
    });

    try {
      await uploadImageUrls(reviewId, viewUrls);
      alert("리뷰가 수정되었습니다.");
      router.push("/me/reviews/");
    } catch (error) {
      console.error("Error:", error);
      alert("이미지 URL 저장 중 오류가 발생했습니다.");
    }
  };

  return (
    <div className="container content-wrapper max-w-6xl mx-auto py-6">
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between w-full">
            <div className="flex items-center gap-4">
              <Button
                variant="ghost"
                onClick={() => router.back()}
                className="hover:bg-gray-100 gap-2"
              >
                <MoveLeft className="w-5 h-5 text-gray-600" />
                <span className="text-gray-600">뒤로가기</span>
              </Button>
            </div>
            <CardTitle className="absolute left-1/2 transform -translate-x-1/2 text-2xl font-semibold">
              리뷰 수정
            </CardTitle>
          </div>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="space-y-2">
              <Label htmlFor="content">내용</Label>
              <Textarea
                id="content"
                value={content}
                onChange={(e) => setContent(e.target.value)}
                className="min-h-32"
                placeholder="리뷰 내용을 입력해주세요"
              />
            </div>

            <div className="space-y-2">
              <Label>평점</Label>
              <div className="flex gap-1">
                {[1, 2, 3, 4, 5].map((value) => (
                  <Button
                    key={value}
                    type="button"
                    variant="ghost"
                    size="sm"
                    className="p-2"
                    onClick={() => setRating(value)}
                  >
                    <Star
                      className={`w-6 h-6 ${
                        value <= rating
                          ? "text-yellow-400 fill-yellow-400"
                          : "text-gray-300"
                      }`}
                    />
                  </Button>
                ))}
              </div>
            </div>

            {existingImages.length > 0 && (
              <div className="space-y-2">
                <Label>기존 이미지</Label>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  {existingImages.map((img, index) => (
                    <div key={index} className="relative group">
                      <img
                        src={img}
                        alt={`리뷰 이미지 ${index + 1}`}
                        className="w-full h-32 object-cover rounded-md"
                      />
                      <Button
                        type="button"
                        variant="destructive"
                        size="icon"
                        className="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity"
                        onClick={() => handleImageDelete(img)}
                      >
                        <XCircle className="w-4 h-4" />
                      </Button>
                    </div>
                  ))}
                </div>
              </div>
            )}

            <div className="space-y-2">
              <Label htmlFor="images">새 이미지 추가</Label>
              <Input
                id="images"
                type="file"
                multiple
                accept="image/*"
                onChange={handleNewImageUpload}
                className="cursor-pointer"
              />

              {newImagePreviews.length > 0 && (
                <div className="mt-4">
                  <Label>새 이미지 미리보기</Label>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mt-2">
                    {newImagePreviews.map((preview, index) => (
                      <div key={index} className="relative group">
                        <img
                          src={preview}
                          alt={`새 이미지 ${index + 1}`}
                          className="w-full h-32 object-cover rounded-md"
                        />
                        <Button
                          type="button"
                          variant="destructive"
                          size="icon"
                          className="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity"
                          onClick={() => handleNewImageDelete(index)}
                        >
                          <XCircle className="w-4 h-4" />
                        </Button>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
            <div className="flex justify-center">
              <Button
                type="submit"
                className="bg-blue-400 w-1/5 text-white mx-auto"
              >
                수정완료
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
