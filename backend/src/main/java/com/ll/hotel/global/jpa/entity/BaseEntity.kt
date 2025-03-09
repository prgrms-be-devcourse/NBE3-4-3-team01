package com.ll.hotel.global.jpa.entity

import jakarta.persistence.*

@MappedSuperclass
abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
    @Column(name = "id")
    protected var _id: Long? = null

    val id : Long
        get() = _id ?: 0 // TODO: 0을 예외로 변경할 지 결정 필요

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is BaseEntity) return false

        val thisClass = this::class.java
        val otherClass = other::class.java

        val thisClassNonProxy = thisClass.name.split("$")[0] // TODO : Ut.str 로 분리
        val otherClassNonProxy = otherClass.name.split("$")[0] // TODO : Ut.str 로 분리

        if (thisClassNonProxy != otherClassNonProxy) return false

        // ID 비교 (동일한 엔티티면 ID도 동일해야 함)
        return _id == other._id

    }

    override fun hashCode(): Int {
        return _id?.hashCode() ?: System.identityHashCode(this)
    }
}