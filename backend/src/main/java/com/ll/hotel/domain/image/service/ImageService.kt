package com.ll.hotel.domain.image.service;

import com.ll.hotel.domain.image.converter.ImageConverter;
import com.ll.hotel.domain.image.entity.Image;
import com.ll.hotel.domain.image.repository.ImageRepository;
import com.ll.hotel.domain.image.type.ImageType;
import com.ll.hotel.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ImageService {

    private final ImageRepository imageRepository;

    public void saveImages(ImageType imageType, long id, List<String> imageUrls) {
        List<Image> images = imageUrls.stream()
                .map(imageUrl -> ImageConverter.toImage(imageType, id, imageUrl))
                .toList();

        imageRepository.saveAll(images);
    }

    public void deleteImagesByIdAndUrls(ImageType imageType, long id, List<String> urls) {
        if(Ut.list.hasValue(urls)) {
            imageRepository.deleteByReferenceIdAndImageUrls(imageType, id, urls);
        }
    }

    public long deleteImages(ImageType imageType, long id) {
        return imageRepository.deleteByImageTypeAndReferenceId(imageType, id);
    }

    public List<Image> findImagesById(ImageType imageType, long id) {
        return imageRepository.findByImageTypeAndReferenceId(imageType, id);
    }
}
