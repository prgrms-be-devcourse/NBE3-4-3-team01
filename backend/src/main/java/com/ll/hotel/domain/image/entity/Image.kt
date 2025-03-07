package com.ll.hotel.domain.image.entity;

import com.ll.hotel.domain.image.type.ImageType;
import com.ll.hotel.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "images",
    indexes = [
        Index(name = "idx_reference", columnList = "referenceId, imageType")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class Image(
    @Column(nullable = false)
    var imageUrl: String,

    @Column(nullable = false)
    var referenceId: Long = 0L,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var imageType: ImageType
) : BaseEntity() {
    @CreatedDate
    @Column(nullable = false)
    lateinit var createdAt: LocalDateTime
}

