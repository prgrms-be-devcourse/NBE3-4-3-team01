package com.ll.hotel.domain.image.converter;

import com.ll.hotel.domain.image.entity.Image;
import com.ll.hotel.domain.image.type.ImageType;

public class ImageConverter {
    private ImageConverter() {}

    public static Image toImage(ImageType imageType, long id, String imageUrl) {
        return Image.builder()
                .imageType(imageType)
                .referenceId(id)
                .imageUrl(imageUrl)
                .build();
    }
}
