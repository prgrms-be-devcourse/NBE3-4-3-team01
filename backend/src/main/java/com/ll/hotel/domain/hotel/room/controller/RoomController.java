package com.ll.hotel.domain.hotel.room.controller;

import com.ll.hotel.domain.hotel.room.dto.GetAllRoomOptionsResponse;
import com.ll.hotel.domain.hotel.room.dto.GetRoomDetailResponse;
import com.ll.hotel.domain.hotel.room.dto.GetRoomResponse;
import com.ll.hotel.domain.hotel.room.dto.PostRoomRequest;
import com.ll.hotel.domain.hotel.room.dto.PostRoomResponse;
import com.ll.hotel.domain.hotel.room.dto.PutRoomRequest;
import com.ll.hotel.domain.hotel.room.dto.PutRoomResponse;
import com.ll.hotel.domain.hotel.room.service.RoomService;
import com.ll.hotel.domain.image.type.ImageType;
import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.global.request.Rq;
import com.ll.hotel.global.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hotels/{hotelId}/rooms")
@Tag(name = "RoomController")
public class RoomController {
    private final RoomService roomService;
    private final Rq rq;

    @PostMapping
    @Operation(summary = "객실 추가")
    public RsData<PostRoomResponse> createRoom(@PathVariable long hotelId,
                                               @RequestBody @Valid PostRoomRequest postRoomRequest) {
        Member actor = this.rq.getActor();

        return RsData.success(HttpStatus.CREATED, this.roomService.createRoom(hotelId, actor, postRoomRequest));
    }

    @PostMapping("/{roomId}/urls")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "사진 URL 리스트 저장")
    public void saveImageUrls(@PathVariable long hotelId, @PathVariable long roomId,
                              @RequestBody List<String> urls
    ) {
        Member actor = this.rq.getActor();

        this.roomService.saveImages(actor, ImageType.ROOM, roomId, urls);
    }

    @DeleteMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "객실 삭제",
            description = """
                    객실을 삭제합니다.<br>
                    정확히는 객실을 사용불가 상태로 변경합니다.
                    """)
    public void deleteRoom(@PathVariable long hotelId, @PathVariable long roomId) {
        Member actor = this.rq.getActor();

        this.roomService.deleteRoom(hotelId, roomId, actor);
    }

    @GetMapping
    @Operation(summary = "객실 목록")
    public RsData<List<GetRoomResponse>> findAllRooms(@PathVariable long hotelId) {
        return RsData.success(HttpStatus.OK, this.roomService.findAllRooms(hotelId));
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "객실 상세 정보")
    public RsData<GetRoomDetailResponse> findRoomDetail(@PathVariable long hotelId, @PathVariable long roomId) {
        return RsData.success(HttpStatus.OK, this.roomService.findRoomDetail(hotelId, roomId));
    }

    @PutMapping("{roomId}")
    @Operation(summary = "객실 수정")
    public RsData<PutRoomResponse> modify(@PathVariable long hotelId,
                                          @PathVariable long roomId,
                                          @RequestBody PutRoomRequest request) {
        Member actor = this.rq.getActor();

        return RsData.success(HttpStatus.OK, this.roomService.modifyRoom(hotelId, roomId, actor, request));
    }

    @GetMapping("/room-option")
    @Operation(summary = "객실 옵션 정보",
            description = """
                    객실에 등록할 수 있는 모든 객실 옵션 정보를 불러옵니다.<br>
                    사업자는 소유 객실에서 제공하는 옵션을 체크하여 등록 및 수정할 수 있습니다.<br>
                    등록되지 않은 객실 옵션이 존재할 시, 관리자에 요청해야합니다.
                    """)
    public RsData<GetAllRoomOptionsResponse> findAllRoomOptions(@PathVariable long hotelId) {
        Member actor = this.rq.getActor();

        return RsData.success(HttpStatus.OK, this.roomService.findAllRoomOptions(actor));
    }
}
