package com.ll.hotel.global.aws.s3

import com.ll.hotel.domain.image.type.ImageType;
import java.util.*

object S3Util {
    // 사진 저장 path 설정
    fun buildS3Key(imageType: ImageType, id: Long, fileType: String): String {
        val fileName = "${UUID.randomUUID()}.$fileType"
        return when (imageType) {
            ImageType.HOTEL -> "hotels/$id/$fileName"
            ImageType.ROOM -> "rooms/$id/$fileName"
            ImageType.REVIEW -> "reviews/$id/$fileName"
        }
    }

    // URL에서 객체 키 추출
    fun extractObjectKeyFromUrl(url: String): String? {
        val domainEndIndex = url.indexOf(".com/")
        return if (domainEndIndex != -1) {
            url.substring(domainEndIndex + 5) // ".com/"의 길이인 5를 더함
        } else {
            null
        }
    }

    // 폴더 경로 생성
    fun getFolderPath(imageType: ImageType, id: Long): String {
        val type = when (imageType) {
            ImageType.HOTEL -> "hotels"
            ImageType.ROOM -> "rooms"
            ImageType.REVIEW -> "reviews"
        }
        return "$type/$id/"
    }
}