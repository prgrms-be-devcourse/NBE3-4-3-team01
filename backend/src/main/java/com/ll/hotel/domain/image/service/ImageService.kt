package com.ll.hotel.domain.image.service;

import com.ll.hotel.domain.image.entity.Image;
import com.ll.hotel.domain.image.repository.ImageRepository;
import com.ll.hotel.domain.image.type.ImageType;
import com.ll.hotel.standard.util.Ut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class ImageService(
    private val imageRepository: ImageRepository
) {
    fun saveImages(imageType: ImageType, id: Long, imageUrls: List<String>) {
        val images: List<Image> = imageUrls.stream()
            .map { Image(it, id, imageType) }
            .toList();

        imageRepository.saveAll(images);
    }

    fun deleteImagesByIdAndUrls(imageType: ImageType, id: Long, urls: List<String>) {
        if (Ut.list.hasValue(urls)) {
            imageRepository.deleteByReferenceIdAndImageUrls(imageType, id, urls);
        }
    }

    fun deleteImages(imageType: ImageType, id: Long): Long {
        return imageRepository.deleteByImageTypeAndReferenceId(imageType, id);
    }

    fun findImagesById(imageType: ImageType, id: Long): List<Image> {
        return imageRepository.findByImageTypeAndReferenceId(imageType, id);
    }
}
