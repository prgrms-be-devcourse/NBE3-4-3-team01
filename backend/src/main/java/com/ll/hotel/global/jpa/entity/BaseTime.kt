package com.ll.hotel.global.jpa.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener::class)
@MappedSuperclass
abstract class BaseTime : BaseEntity() {
    @CreatedDate
    lateinit var createdAt : LocalDateTime

    @LastModifiedDate
    lateinit var modifiedAt: LocalDateTime
}