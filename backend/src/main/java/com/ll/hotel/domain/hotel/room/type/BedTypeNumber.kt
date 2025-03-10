package com.ll.hotel.domain.hotel.room.type

import jakarta.persistence.Embeddable

@Embeddable
data class BedTypeNumber(
    val bedSingle: Int = 0,
    val bedDouble: Int = 0,
    val bedQueen: Int = 0,
    val bedKing: Int = 0,
    val bedTwin: Int = 0,
    val bedTriple: Int = 0
) {
    companion object {
        fun fromJson(bedTypeNumber: Map<String, Int>): BedTypeNumber {
            return BedTypeNumber(
                bedSingle = bedTypeNumber["SINGLE"] ?: 0,
                bedDouble = bedTypeNumber["DOUBLE"] ?: 0,
                bedQueen = bedTypeNumber["QUEEN"] ?: 0,
                bedKing = bedTypeNumber["KING"] ?: 0,
                bedTwin = bedTypeNumber["TWIN"] ?: 0,
                bedTriple = bedTypeNumber["TRIPLE"] ?: 0
            )
        }
    }
}