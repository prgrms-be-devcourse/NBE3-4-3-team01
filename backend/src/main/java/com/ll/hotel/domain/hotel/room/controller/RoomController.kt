package com.ll.hotel.domain.hotel.room.controller

import com.ll.hotel.domain.hotel.room.dto.*
import com.ll.hotel.domain.hotel.room.service.RoomService
import com.ll.hotel.domain.image.type.ImageType
import com.ll.hotel.global.request.Rq
import com.ll.hotel.global.response.RsData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/hotels/{hotelId}/rooms")
@Tag(name = "RoomController")
class RoomController(
    private val roomService: RoomService,
    private val rq: Rq
) {
    @PostMapping
    @Operation(summary = "객실 추가")
    fun createRoom(
        @PathVariable hotelId: Long,
        @RequestBody @Valid postRoomRequest: PostRoomRequest
    ): RsData<PostRoomResponse> {
        val actor = this.rq.getActor()

        return RsData.success(HttpStatus.CREATED, this.roomService.createRoom(hotelId, actor, postRoomRequest))
    }

    @PostMapping("/{roomId}/urls")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "사진 URL 리스트 저장")
    fun saveImageUrls(
        @PathVariable hotelId: Long,
        @PathVariable roomId: Long,
        @RequestBody urls: List<String>
    ) {
        val actor = this.rq.getActor()

        this.roomService.saveImages(actor, ImageType.ROOM, roomId, urls)
    }

    @DeleteMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "객실 삭제",
        description = """
                    객실을 삭제합니다.<br>
                    정확히는 객실을 사용불가 상태로 변경합니다.
                    """
    )
    fun deleteRoom(@PathVariable hotelId: Long, @PathVariable roomId: Long) {
        val actor = this.rq.getActor()

        this.roomService.deleteRoom(hotelId, roomId, actor)
    }

    @GetMapping
    @Operation(summary = "객실 목록")
    fun findAllRooms(@PathVariable hotelId: Long): RsData<List<GetRoomResponse>> {
        return RsData.success(HttpStatus.OK, this.roomService.findAllRooms(hotelId))
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "객실 상세 정보")
    fun findRoomDetail(@PathVariable hotelId: Long, @PathVariable roomId: Long): RsData<GetRoomDetailResponse> {
        return RsData.success(HttpStatus.OK, this.roomService.findRoomDetail(hotelId, roomId))
    }

    @PutMapping("{roomId}")
    @Operation(summary = "객실 수정")
    fun modify(
        @PathVariable hotelId: Long,
        @PathVariable roomId: Long,
        @RequestBody @Valid request: PutRoomRequest
    ): RsData<PutRoomResponse> {
        val actor = this.rq.getActor()

        return RsData.success(HttpStatus.OK, this.roomService.modifyRoom(hotelId, roomId, actor, request))
    }

    @GetMapping("/room-option")
    @Operation(
        summary = "객실 옵션 정보",
        description = """
                    객실에 등록할 수 있는 모든 객실 옵션 정보를 불러옵니다.<br>
                    사업자는 소유 객실에서 제공하는 옵션을 체크하여 등록 및 수정할 수 있습니다.<br>
                    등록되지 않은 객실 옵션이 존재할 시, 관리자에 요청해야합니다.
                    """
    )
    fun findAllRoomOptions(@PathVariable hotelId: Long): RsData<GetAllRoomOptionsResponse> {
        val actor = this.rq.getActor()

        return RsData.success(HttpStatus.OK, this.roomService.findAllRoomOptions(actor))
    }
}
