package com.ll.hotel.domain.image.repository;

import com.ll.hotel.domain.image.dto.ImageDto
import com.ll.hotel.domain.image.entity.Image
import com.ll.hotel.domain.image.type.ImageType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface ImageRepository : JpaRepository<Image, Long> {

    fun findByImageTypeAndReferenceId(imageType: ImageType, referenceId: Long): List<Image>

    @Transactional
    @Modifying
    @Query(
        """
        DELETE FROM Image i
        WHERE i.referenceId = :referenceId 
        AND i.imageType = :imageType 
        AND i.imageUrl IN :imageUrls
    """
    )
    fun deleteByReferenceIdAndImageUrls(
        @Param("imageType") imageType: ImageType,
        @Param("referenceId") referenceId: Long,
        @Param("imageUrls") imageUrls: List<String>
    )

    fun deleteByImageTypeAndReferenceId(imageType: ImageType, referenceId: Long): Long

    @Query(
        """  
        SELECT new com.ll.hotel.domain.image.dto.ImageDto(
            i.referenceId,
            i.imageUrl
        )
        FROM Image i
        WHERE i.referenceId IN :referenceIds
        AND i.imageType = :imageType
    """
    )
    fun findImageUrlsByReferenceIdsAndImageType(
        @Param("referenceIds") referenceIds: List<Long>,
        @Param("imageType") imageType: ImageType,
        pageable: Pageable
    ): Page<ImageDto>
}
