package com.ll.hotel.global.aws.s3;

import com.ll.hotel.domain.image.type.ImageType;
import com.ll.hotel.global.exceptions.ErrorCode;
import com.ll.hotel.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    String bucketName;

    // 여러 사진 한번에 저장
    public List<URL> generatePresignedUrls(ImageType imageType, long id, List<String> fileTypes) {
        if(!Ut.list.hasValue(fileTypes)) return List.of();

        try {
            return fileTypes.stream()
                    .map(fileType -> createPresignedUrlResponse(imageType, id, fileType))
                    .toList();
        } catch (SdkException e) {
            throw ErrorCode.S3_PRESIGNED_GENERATION_FAIL.throwS3Exception(e);
        }
    }

    // 사진 1개 저장
    public URL createPresignedUrlResponse(ImageType imageType, long id, String fileType) {
        String key = S3Util.buildS3Key(imageType, id, fileType);// 임시

        try {
            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(builder -> builder
                    .putObjectRequest(putObject -> putObject
                            .bucket(bucketName)
                            .key(key))
                    .signatureDuration(Duration.ofMinutes(10)));

            return presignedRequest.url();
        } catch (SdkException e) {
            throw ErrorCode.S3_PRESIGNED_GENERATION_FAIL.throwS3Exception(e);
        }
    }

    // URL 리스트를 받아 Object 모두 삭제
    public void deleteObjectsByUrls(List<String> urls) {
        if(!Ut.list.hasValue(urls)) return;

        try {
            List<ObjectIdentifier> objectIdentifiers = urls.stream()
                    .map(S3Util::extractObjectKeyFromUrl)
                    .map(key -> ObjectIdentifier.builder().key(key).build())
                    .toList();

            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(delete -> delete.objects(objectIdentifiers))
                    .build();

            // S3 객체 삭제 요청
            s3Client.deleteObjects(deleteRequest);

        } catch (SdkException e) {
            ErrorCode.S3_OBJECT_DELETE_FAIL.throwS3Exception(e);
        }
    }

    // S3의 폴더의 모든 Object 들 삭제
    public void deleteAllObjectsById(ImageType imageType, long id) {
        // folderPath 로 변환
        String folderPath = S3Util.getFolderPath(imageType, id);

        try {
            // 폴더 내 모든 객체 목록 조회
            List<S3Object> objects = listAllObjectsInFolder(folderPath);

            // 객체가 없으면 종료
            if (objects.isEmpty()) {
                return;
            }

            // 삭제할 객체 키 목록 생성
            List<ObjectIdentifier> objectIdentifiers = new ArrayList<>();
            for (S3Object object : objects) {
                objectIdentifiers.add(
                        ObjectIdentifier.builder()
                                .key(object.key())
                                .build()
                );
            }

            // 객체 삭제 요청 (한 번에 최대 1000개 객체 삭제 가능)
            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(delete -> delete.objects(objectIdentifiers))
                    .build();

            // S3 객체 삭제 요청
            s3Client.deleteObjects(deleteRequest);

        } catch (SdkException e) {
            ErrorCode.S3_OBJECT_DELETE_FAIL.throwS3Exception(e);
        }
    }

    // 폴더의 모든 Object 조회
    private List<S3Object> listAllObjectsInFolder(String folderPath) {
        List<S3Object> objects = new ArrayList<>();

        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(folderPath)
                    .build();

            ListObjectsV2Response listResponse;
            do {
                listResponse = s3Client.listObjectsV2(listRequest);
                objects.addAll(listResponse.contents());
                listRequest = ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .prefix(folderPath)
                        .continuationToken(listResponse.nextContinuationToken())
                        .build();
            } while (Boolean.TRUE.equals(listResponse.isTruncated()));
        } catch (SdkException e) {
            ErrorCode.S3_OBJECT_ACCESS_FAIL.throwS3Exception(e);
        }

        return objects;
    }
}
