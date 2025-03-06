package com.ll.hotel.domain.member.member.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ll.hotel.domain.member.member.dto.FavoriteDto;
import com.ll.hotel.domain.member.member.service.MemberService;
import com.ll.hotel.global.response.RsData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
@Tag(name = "FavoriteController", description = "즐겨찾기 API")
public class FavoriteController {
    private final MemberService memberService;

    @PostMapping("/{hotelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "즐겨찾기 추가", description = "호텔을 즐겨찾기에 추가합니다.")
    public void addFavorite(@PathVariable Long hotelId) {
        memberService.addFavorite(hotelId);
    }

    @DeleteMapping("/{hotelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "즐겨찾기 삭제", description = "즐겨찾기에서 호텔을 삭제합니다.")
    public void removeFavorite(@PathVariable Long hotelId) {
        memberService.removeFavorite(hotelId);
    }

    @GetMapping("/me")
    @Operation(summary = "즐겨찾기 목록 조회", description = "사용자의 즐겨찾기 호텔 목록을 조회합니다.")
    public RsData<List<FavoriteDto>> getFavorites() {
        List<FavoriteDto> favorites = memberService.getFavoriteHotels();
        
        if (favorites.isEmpty()) {
            return RsData.success(HttpStatus.OK, List.of());
        }

        return RsData.success(HttpStatus.OK, favorites);
    }

    @GetMapping("/me/{hotelId}")
    @Operation(summary = "즐겨찾기 여부 확인", description = "특정 호텔이 즐겨찾기에 추가되어 있는지 확인합니다.")
    public RsData<Boolean> checkFavorite(@PathVariable("hotelId") Long hotelId) {
        boolean isFavorite = memberService.isFavoriteHotel(hotelId);
        return RsData.success(HttpStatus.OK, isFavorite);
    }
} 