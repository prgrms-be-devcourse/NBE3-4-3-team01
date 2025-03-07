package com.ll.hotel.domain.image.entity;

import com.ll.hotel.domain.image.type.ImageType;
import com.ll.hotel.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "images", indexes = @Index(name = "idx_reference", columnList = "referenceId, imageType"))
@EntityListeners(AuditingEntityListener.class)
class Image : BaseEntity {

    @Column(nullable = false)
    private String imageUrl;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Long referenceId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ImageType imageType;

    @Builder
    private Image(String imageUrl, Long referenceId, ImageType imageType) {
        this.imageUrl = imageUrl;
        this.referenceId = referenceId;
        this.imageType = imageType;
    }
}
