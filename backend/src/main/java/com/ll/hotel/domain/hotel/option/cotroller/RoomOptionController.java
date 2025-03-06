package com.ll.hotel.domain.hotel.option.cotroller;

import com.ll.hotel.domain.hotel.option.dto.request.OptionRequest;
import com.ll.hotel.domain.hotel.option.dto.response.OptionResponse;
import com.ll.hotel.domain.hotel.option.entity.RoomOption;
import com.ll.hotel.domain.hotel.option.service.RoomOptionService;
import com.ll.hotel.global.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "RoomOptionController")
@RestController
@RequestMapping("/api/admin/room-options")
@RequiredArgsConstructor
public class RoomOptionController {
    private final RoomOptionService roomOptionService;

    @Operation(summary = "객실 옵션 추가")
    @PostMapping
    public RsData<OptionResponse> add(@RequestBody @Valid OptionRequest optionRequest) {

        RoomOption roomOption = roomOptionService.add(optionRequest);

        return RsData.success(HttpStatus.CREATED, OptionResponse.from(roomOption));
    }

    @Operation(summary = "객실 옵션 전체 조회")
    @GetMapping
    public RsData<List<OptionResponse>> getAll() {

        List<OptionResponse> roomAmenityList = roomOptionService.findAll()
                .stream()
                .map(OptionResponse::from).toList();

        return RsData.success(HttpStatus.OK, roomAmenityList);
    }

    @Operation(summary = "객실 옵션 수정")
    @PatchMapping("/{id}")
    public RsData<OptionResponse> modify(@PathVariable("id") Long id,
                                         @RequestBody OptionRequest optionRequest) {
        RoomOption roomOption = roomOptionService.findById(id);
        roomOptionService.modify(roomOption, optionRequest);

        roomOptionService.flush();

        return RsData.success(HttpStatus.OK, OptionResponse.from(roomOption));
    }

    @Operation(summary = "객실 옵션 삭제")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        RoomOption roomOption = roomOptionService.findById(id);
        roomOptionService.delete(roomOption);
    }
}
