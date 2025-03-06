// Presigned URLs을 사용하여 AWS S3에 이미지 업로드
export const uploadImagesToS3 = async (
  presignedUrls: string[],
  images: File[]
) => {
  try {
    await Promise.all(
      images.map((image, index) =>
        fetch(presignedUrls[index], {
          method: "PUT",
          body: image,
          headers: {
            "Content-Type": image.type, // 이미지 파일 타입 지정
          },
        }).then((uploadRes) => {
          if (!uploadRes.ok) {
            throw new Error(`이미지 업로드 실패: ${uploadRes.statusText}`);
          }
          console.log(`이미지 업로드 성공: ${presignedUrls[index]}`);
        })
      )
    );
  } catch (error) {
    console.error("Error:", error);
    throw error;
  }
};
