package com.ll.hotel.domain.review.review.dto.response;

import java.net.URL;

data class PresignedUrlsResponse(
        val reviewId : Long,
        val presignedUrls : List<URL>
)
