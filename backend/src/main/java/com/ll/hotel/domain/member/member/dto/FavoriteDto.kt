package com.ll.hotel.domain.member.member.dto

import com.ll.hotel.domain.hotel.hotel.entity.Hotel
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class FavoriteDto(
    val hotelId: Long,
    
    @field:NotBlank
    val hotelName: String,
    
    @field:NotBlank
    val streetAddress: String,
    
    @field:NotNull
    val hotelGrade: Int,
    
    @field:NotBlank
    val hotelStatus: String
) {
    // 테스트 코드용 (추후 코틀린 마이그레이션 끝난 뒤 삭제)
    fun hotelId(): Long = hotelId
    fun hotelName(): String = hotelName
    fun streetAddress(): String = streetAddress
    fun hotelGrade(): Int = hotelGrade
    fun hotelStatus(): String = hotelStatus
    
    companion object {
        @JvmStatic
        fun from(hotel: Hotel): FavoriteDto {
            return FavoriteDto(
                hotelId = hotel.id,
                hotelName = hotel.hotelName,
                streetAddress = hotel.streetAddress,
                hotelGrade = hotel.hotelGrade,
//                hotelStatus = hotel.hotelStatus.value // 코틀린 마이그레이션이 전부 진행되면 변경
                hotelStatus = hotel.hotelStatus.getValue()
            )
        }
    }
} 