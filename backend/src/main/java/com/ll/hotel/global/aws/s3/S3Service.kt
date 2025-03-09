package com.ll.hotel.global.aws.s3

import com.ll.hotel.domain.image.type.ImageType
import com.ll.hotel.global.exceptions.ErrorCode
import com.ll.hotel.standard.util.Ut
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.exception.SdkException
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest
import java.net.URL
import java.time.Duration

@Service
class S3Service(
        private val s3Presigner: S3Presigner,
        private val s3Client: S3Client,
        @Value("\${spring.cloud.aws.s3.bucket}") private val bucketName: String
) {

    // 여러 사진 한번에 저장
    fun generatePresignedUrls(imageType: ImageType, id: Long, fileTypes: List<String>): List<URL> {
        if (!Ut.list.hasValue(fileTypes)) return emptyList()

        return try {
            fileTypes.map { fileType ->
                    createPresignedUrlResponse(imageType, id, fileType)
            }
        } catch (e: SdkException) {
            throw ErrorCode.S3_PRESIGNED_GENERATION_FAIL.throwS3Exception(e)
        }
    }

    // 사진 1개 저장
    fun createPresignedUrlResponse(imageType: ImageType, id: Long, fileType: String): URL {
        val key = S3Util.buildS3Key(imageType, id, fileType) // 임시

        return try {
            val presignedRequest = s3Presigner.presignPutObject { builder ->
                    builder.putObjectRequest { putObject ->
                    putObject.bucket(bucketName).key(key)
            }.signatureDuration(Duration.ofMinutes(10))
            }
            presignedRequest.url()
        } catch (e: SdkException) {
            throw ErrorCode.S3_PRESIGNED_GENERATION_FAIL.throwS3Exception(e)
        }
    }

    // URL 리스트를 받아 Object 모두 삭제
    fun deleteObjectsByUrls(urls: List<String>) {
        if (!Ut.list.hasValue(urls)) return

        try {
            val objectIdentifiers = urls.map { url ->
                    ObjectIdentifier.builder().key(S3Util.extractObjectKeyFromUrl(url)).build()
            }

            val deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete { delete -> delete.objects(objectIdentifiers) }
                .build()

            // S3 객체 삭제 요청
            s3Client.deleteObjects(deleteRequest)
        } catch (e: SdkException) {
            ErrorCode.S3_OBJECT_DELETE_FAIL.throwS3Exception(e)
        }
    }

    // S3의 폴더의 모든 Object 들 삭제
    fun deleteAllObjectsById(imageType: ImageType, id: Long) {
        val folderPath = S3Util.getFolderPath(imageType, id)

        try {
            // 폴더 내 모든 객체 목록 조회
            val objects = listAllObjectsInFolder(folderPath)

            // 객체가 없으면 종료
            if (objects.isEmpty()) return

                    // 삭제할 객체 키 목록 생성
                    val objectIdentifiers = objects.map { obj ->
                    ObjectIdentifier.builder().key(obj.key()).build()
            }

            // 객체 삭제 요청
            val deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete { delete -> delete.objects(objectIdentifiers) }
                .build()

            // S3 객체 삭제 요청
            s3Client.deleteObjects(deleteRequest)
        } catch (e: SdkException) {
            ErrorCode.S3_OBJECT_DELETE_FAIL.throwS3Exception(e)
        }
    }

    // 폴더의 모든 Object 조회
    private fun listAllObjectsInFolder(folderPath: String): List<S3Object> {
        val objects = mutableListOf<S3Object>()

        try {
            var listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(folderPath)
                    .build()

            do {
                val listResponse = s3Client.listObjectsV2(listRequest)
                objects.addAll(listResponse.contents())
                listRequest = listRequest.toBuilder()
                        .continuationToken(listResponse.nextContinuationToken())
                        .build()
            } while (listResponse.isTruncated)
        } catch (e: SdkException) {
            ErrorCode.S3_OBJECT_ACCESS_FAIL.throwS3Exception(e)
        }

        return objects
    }
}