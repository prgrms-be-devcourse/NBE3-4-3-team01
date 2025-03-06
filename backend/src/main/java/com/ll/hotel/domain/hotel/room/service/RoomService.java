package com.ll.hotel.domain.hotel.room.service;

import com.ll.hotel.domain.hotel.hotel.entity.Hotel;
import com.ll.hotel.domain.hotel.hotel.repository.HotelRepository;
import com.ll.hotel.domain.hotel.option.entity.RoomOption;
import com.ll.hotel.domain.hotel.option.repository.RoomOptionRepository;
import com.ll.hotel.domain.hotel.room.dto.*;
import com.ll.hotel.domain.hotel.room.entity.Room;
import com.ll.hotel.domain.hotel.room.repository.RoomRepository;
import com.ll.hotel.domain.hotel.room.type.BedTypeNumber;
import com.ll.hotel.domain.hotel.room.type.RoomStatus;
import com.ll.hotel.domain.image.entity.Image;
import com.ll.hotel.domain.image.service.ImageService;
import com.ll.hotel.domain.image.type.ImageType;
import com.ll.hotel.domain.member.member.entity.Member;
import com.ll.hotel.domain.review.review.dto.response.PresignedUrlsResponse;
import com.ll.hotel.global.annotation.BusinessOnly;
import com.ll.hotel.global.aws.s3.S3Service;
import com.ll.hotel.global.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {
    private final ImageService imageService;
    private final S3Service s3Service;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final RoomOptionRepository roomOptionRepository;

    @BusinessOnly
    @Transactional
    public PostRoomResponse createRoom(long hotelId, Member actor, PostRoomRequest postRoomRequest) {
        Hotel hotel = this.getHotelById(hotelId);

        if (!hotel.isOwnedBy(actor)) {
            ErrorCode.INVALID_BUSINESS.throwServiceException();
        }

        BedTypeNumber bedTypeNumber = BedTypeNumber.fromJson(postRoomRequest.bedTypeNumber());

        Set<RoomOption> roomOptions = this.roomOptionRepository.findByNameIn(postRoomRequest.roomOptions());

        if (roomOptions.size() != postRoomRequest.roomOptions().size()) {
            ErrorCode.ROOM_OPTION_NOT_FOUND.throwServiceException();
        }

        Room room = Room.roomBuild(hotel, postRoomRequest, bedTypeNumber, roomOptions);

        try {
            return new PostRoomResponse(this.roomRepository.save(room),
                    this.saveRoomImages(room.getId(), postRoomRequest.imageExtensions()));
        } catch (DataIntegrityViolationException e) {
            throw ErrorCode.ROOM_NAME_ALREADY_EXISTS.throwServiceException();
        }
    }

    @BusinessOnly
    @Transactional
    public void saveImages(Member actor, ImageType imageType, long roomId, List<String> urls) {
        Hotel hotel = getRoomById(roomId).getHotel();

        if (!hotel.isOwnedBy(actor)) {
            ErrorCode.INVALID_BUSINESS.throwServiceException();
        }

        this.imageService.saveImages(imageType, roomId, urls);
    }

    @BusinessOnly
    @Transactional
    public void deleteRoom(long hotelId, long roomId, Member actor) {
        Hotel hotel = this.getHotelById(hotelId);

        if (!hotel.isOwnedBy(actor)) {
            ErrorCode.INVALID_BUSINESS.throwServiceException();
        }

        Room room = this.getRoomById(roomId);

        room.setRoomStatus(RoomStatus.UNAVAILABLE);

        if (this.imageService.deleteImages(ImageType.ROOM, roomId) > 0) {
            this.s3Service.deleteAllObjectsById(ImageType.ROOM, roomId);
        }
    }

    @Transactional(readOnly = true)
    public List<GetRoomResponse> findAllRooms(long hotelId) {
        return this.roomRepository.findAllRooms(hotelId, ImageType.ROOM).stream()
                .map(GetRoomResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GetRoomDetailResponse findRoomDetail(long hotelId, long roomId) {
        checkHotelExists(hotelId);

        Room room = this.getRoomDetail(hotelId, roomId);

        List<String> imageUrls = this.imageService.findImagesById(ImageType.ROOM, roomId).stream()
                .map(Image::getImageUrl)
                .toList();

        return new GetRoomDetailResponse(new RoomDto(room), imageUrls);
    }

    @BusinessOnly
    @Transactional
    public PutRoomResponse modifyRoom(long hotelId, long roomId, Member actor, PutRoomRequest request) {
        Hotel hotel = this.getHotelById(hotelId);

        if (!hotel.isOwnedBy(actor)) {
            ErrorCode.INVALID_BUSINESS.throwServiceException();
        }

        Room room = this.getRoomDetail(hotelId, roomId);

        if (this.roomRepository.existsByHotelIdAndRoomNameAndIdNot(hotelId, request.roomName(), roomId)) {
            ErrorCode.ROOM_NAME_ALREADY_EXISTS.throwServiceException();
        }

        modifyIfPresent(request.roomName(), room::getRoomName, room::setRoomName);
        modifyIfPresent(request.roomNumber(), room::getRoomNumber, room::setRoomNumber);
        modifyIfPresent(request.basePrice(), room::getBasePrice, room::setBasePrice);
        modifyIfPresent(request.standardNumber(), room::getStandardNumber, room::setStandardNumber);
        modifyIfPresent(request.maxNumber(), room::getMaxNumber, room::setMaxNumber);
        modifyIfPresent(request.bedTypeNumber(), room::getBedTypeNumber, room::setBedTypeNumber);

        if (request.roomStatus() != null) {
            try {
                room.setRoomStatus(RoomStatus.valueOf(request.roomStatus().toUpperCase()));
            } catch (Exception e) {
                ErrorCode.ROOM_STATUS_NOT_FOUND.throwServiceException();
            }
        }

        modifyOptions(room, request.roomOptions());

        List<String> deleteImageUrls = request.deleteImageUrls();

        this.imageService.deleteImagesByIdAndUrls(ImageType.ROOM, roomId, deleteImageUrls);
        this.s3Service.deleteObjectsByUrls(deleteImageUrls);

        return new PutRoomResponse(room, this.saveRoomImages(roomId, request.imageExtensions()));
    }

    private <T> void modifyIfPresent(T newValue, Supplier<T> getter, Consumer<T> setter) {
        if (newValue != null && !newValue.equals(getter.get())) {
            setter.accept(newValue);
        }
    }

    @Transactional
    public void modifyOptions(Room room, Set<String> optionNames) {
        if (optionNames == null || optionNames.isEmpty()) {
            room.setRoomOptions(new HashSet<>());
            return;
        }

        Set<RoomOption> options = this.roomOptionRepository.findByNameIn(optionNames);

        if (options.size() != optionNames.size()) {
            ErrorCode.ROOM_OPTION_NOT_FOUND.throwServiceException();
        }

        room.setRoomOptions(options);
    }

    private Hotel getHotelById(long hotelId) {
        return this.hotelRepository.findById(hotelId)
                .orElseThrow(ErrorCode.HOTEL_NOT_FOUND::throwServiceException);
    }

    private Room getRoomById(long roomId) {
        return this.roomRepository.findById(roomId)
                .orElseThrow(ErrorCode.ROOM_NOT_FOUND::throwServiceException);
    }

    private Room getRoomDetail(long hotelId, long roomId) {
        return this.roomRepository.findRoomDetail(hotelId, roomId)
                .orElseThrow(ErrorCode.ROOM_NOT_FOUND::throwServiceException);
    }

    private void checkHotelExists(long hotelId) {
        if (!this.hotelRepository.existsById(hotelId)) {
            ErrorCode.HOTEL_NOT_FOUND.throwServiceException();
        }
    }

    // 객실 이미지 저장
    private PresignedUrlsResponse saveRoomImages(long roomId, List<String> extensions) {
        List<URL> urls = this.s3Service.generatePresignedUrls(ImageType.ROOM, roomId, extensions);

        return new PresignedUrlsResponse(roomId, urls);
    }

    @BusinessOnly
    @Transactional(readOnly = true)
    public GetAllRoomOptionsResponse findAllRoomOptions(Member actor) {
        return new GetAllRoomOptionsResponse(this.roomOptionRepository.findAll());
    }
}
