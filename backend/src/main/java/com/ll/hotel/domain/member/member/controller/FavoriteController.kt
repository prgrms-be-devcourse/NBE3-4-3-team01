package com.ll.hotel.domain.member.member.controller

import com.ll.hotel.domain.member.member.dto.FavoriteDto
import com.ll.hotel.domain.member.member.service.MemberService
import com.ll.hotel.global.response.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/favorites")
@Tag(name = "FavoriteController", description = "즐겨찾기 API")
class FavoriteController(
    private val memberService: MemberService
) {
    @PostMapping("/{hotelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "즐겨찾기 추가", description = "호텔을 즐겨찾기에 추가합니다.")
    fun addFavorite(@PathVariable hotelId: Long) {
        memberService.addFavorite(hotelId)
    }

    @DeleteMapping("/{hotelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "즐겨찾기 삭제", description = "즐겨찾기에서 호텔을 삭제합니다.")
    fun removeFavorite(@PathVariable hotelId: Long) {
        memberService.removeFavorite(hotelId)
    }

    @GetMapping("/me")
    @Operation(summary = "즐겨찾기 목록 조회", description = "사용자의 즐겨찾기 호텔 목록을 조회합니다.")
    fun getFavorites(): RsData<List<FavoriteDto>> {
        val favorites = memberService.getFavoriteHotels()
        
        if (favorites.isEmpty()) {
            return RsData.success(HttpStatus.OK, emptyList())
        }

        return RsData.success(HttpStatus.OK, favorites)
    }

    @GetMapping("/me/{hotelId}")
    @Operation(summary = "즐겨찾기 여부 확인", description = "특정 호텔이 즐겨찾기에 추가되어 있는지 확인합니다.")
    fun checkFavorite(@PathVariable("hotelId") hotelId: Long): RsData<Boolean> {
        val isFavorite = memberService.isFavoriteHotel(hotelId)
        return RsData.success(HttpStatus.OK, isFavorite)
    }
} 