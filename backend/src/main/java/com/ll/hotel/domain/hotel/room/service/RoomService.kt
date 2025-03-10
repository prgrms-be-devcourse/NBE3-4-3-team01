package com.ll.hotel.domain.hotel.room.service

import com.ll.hotel.domain.hotel.hotel.entity.Hotel
import com.ll.hotel.domain.hotel.hotel.repository.HotelRepository
import com.ll.hotel.domain.hotel.option.repository.RoomOptionRepository
import com.ll.hotel.domain.hotel.room.dto.*
import com.ll.hotel.domain.hotel.room.entity.Room
import com.ll.hotel.domain.hotel.room.repository.RoomRepository
import com.ll.hotel.domain.hotel.room.type.BedTypeNumber
import com.ll.hotel.domain.hotel.room.type.RoomStatus
import com.ll.hotel.domain.image.service.ImageService
import com.ll.hotel.domain.image.type.ImageType
import com.ll.hotel.domain.member.member.entity.Member
import com.ll.hotel.domain.review.review.dto.response.PresignedUrlsResponse
import com.ll.hotel.global.annotation.BusinessOnly
import com.ll.hotel.global.aws.s3.S3Service
import com.ll.hotel.global.exceptions.ErrorCode
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.function.Consumer
import java.util.function.Supplier

@Service
@Transactional
class RoomService(
    private val imageService: ImageService,
    private val s3Service: S3Service,
    private val hotelRepository: HotelRepository,
    private val roomRepository: RoomRepository,
    private val roomOptionRepository: RoomOptionRepository
) {
    @BusinessOnly
    @Transactional
    fun createRoom(hotelId: Long, actor: Member, postRoomRequest: PostRoomRequest): PostRoomResponse {
        val hotel = this.getHotelById(hotelId)

        if (!hotel.isOwnedBy(actor)) {
            ErrorCode.INVALID_BUSINESS.throwServiceException()
        }

        val bedTypeNumber = BedTypeNumber.fromJson(postRoomRequest.bedTypeNumber)
        val roomOptions = this.roomOptionRepository.findByNameIn(postRoomRequest.roomOptions)

        if (roomOptions.size != postRoomRequest.roomOptions.size) {
            ErrorCode.ROOM_OPTION_NOT_FOUND.throwServiceException()
        }

        val room = Room.roomBuild(hotel, postRoomRequest, bedTypeNumber, roomOptions)

        return try {
            PostRoomResponse(roomRepository.save(room), saveRoomImages(room.id, postRoomRequest.imageExtensions))
        } catch (_: DataIntegrityViolationException) {
            throw ErrorCode.ROOM_NAME_ALREADY_EXISTS.throwServiceException()
        }
    }

    @BusinessOnly
    @Transactional
    fun saveImages(actor: Member, imageType: ImageType, roomId: Long, urls: List<String>) {
        val hotel = this.getRoomById(roomId).hotel

        if (!hotel.isOwnedBy(actor)) {
            ErrorCode.INVALID_BUSINESS.throwServiceException()
        }

        this.imageService.saveImages(imageType, roomId, urls)
    }

    @BusinessOnly
    @Transactional
    fun deleteRoom(hotelId: Long, roomId: Long, actor: Member) {
        val hotel = this.getHotelById(hotelId)

        if (!hotel.isOwnedBy(actor)) {
            ErrorCode.INVALID_BUSINESS.throwServiceException()
        }

        val room = this.getRoomById(roomId)
        room.roomStatus = RoomStatus.UNAVAILABLE

        if (this.imageService.deleteImages(ImageType.ROOM, roomId) > 0) {
            this.s3Service.deleteAllObjectsById(ImageType.ROOM, roomId)
        }
    }

    @Transactional(readOnly = true)
    fun findAllRooms(hotelId: Long): List<GetRoomResponse> =
        this.roomRepository.findAllRooms(hotelId, ImageType.ROOM).map { GetRoomResponse(it) }

    @Transactional(readOnly = true)
    fun findRoomDetail(hotelId: Long, roomId: Long): GetRoomDetailResponse {
        this.checkHotelExists(hotelId)

        val room = this.getRoomDetail(hotelId, roomId)

        val imageUrls = this.imageService.findImagesById(ImageType.ROOM, roomId).map { it.imageUrl }

        return GetRoomDetailResponse(RoomDto(room), imageUrls)
    }

    @BusinessOnly
    @Transactional
    fun modifyRoom(hotelId: Long, roomId: Long, actor: Member, request: PutRoomRequest): PutRoomResponse {
        val hotel = this.getHotelById(hotelId)

        if (!hotel.isOwnedBy(actor)) {
            ErrorCode.INVALID_BUSINESS.throwServiceException()
        }

        val room = this.getRoomDetail(hotelId, roomId)

        if (this.roomRepository.existsByHotelIdAndRoomNameAndIdNot(hotelId, request.roomName, roomId)) {
            ErrorCode.ROOM_NAME_ALREADY_EXISTS.throwServiceException()
        }

        this.modifyIfPresent(request.roomName, room::roomName, room::roomName::set)
        this.modifyIfPresent(request.roomNumber, room::roomNumber, room::roomNumber::set)
        this.modifyIfPresent(request.basePrice, room::basePrice, room::basePrice::set)
        this.modifyIfPresent(request.standardNumber, room::standardNumber, room::standardNumber::set)
        this.modifyIfPresent(request.maxNumber, room::maxNumber, room::maxNumber::set)
        this.modifyIfPresent(request.bedTypeNumber, room::bedTypeNumber, room::bedTypeNumber::set)

        try {
            room.roomStatus = RoomStatus.valueOf(request.roomStatus.uppercase())
        } catch (_: Exception) {
            ErrorCode.ROOM_STATUS_NOT_FOUND.throwServiceException()
        }

        this.modifyOptions(room, request.roomOptions)

        val deleteImageUrls = request.deleteImageUrls
        this.imageService.deleteImagesByIdAndUrls(ImageType.ROOM, roomId, deleteImageUrls)
        this.s3Service.deleteObjectsByUrls(deleteImageUrls)

        return PutRoomResponse(room, saveRoomImages(roomId, request.imageExtensions))
    }

    private fun <T> modifyIfPresent(newValue: T, getter: Supplier<T>, setter: Consumer<T>) {
        if (newValue != getter.get()) {
            setter.accept(newValue)
        }
    }

    @Transactional
    fun modifyOptions(room: Room, optionNames: Set<String>) {
        if (optionNames.isEmpty()) {
            room.roomOptions = mutableSetOf()
            return
        }

        val options = this.roomOptionRepository.findByNameIn(optionNames)

        if (options.size != optionNames.size) {
            ErrorCode.ROOM_OPTION_NOT_FOUND.throwServiceException()
        }

        room.roomOptions = options
    }

    private fun getHotelById(hotelId: Long): Hotel =
        this.hotelRepository.findById(hotelId).orElseThrow { ErrorCode.HOTEL_NOT_FOUND.throwServiceException() }

    private fun getRoomById(roomId: Long): Room =
        this.roomRepository.findById(roomId).orElseThrow { ErrorCode.ROOM_NOT_FOUND.throwServiceException() }

    private fun getRoomDetail(hotelId: Long, roomId: Long): Room =
        this.roomRepository.findRoomDetail(hotelId, roomId)
            .orElseThrow { ErrorCode.ROOM_NOT_FOUND.throwServiceException() }

    private fun checkHotelExists(hotelId: Long) {
        if (!this.hotelRepository.existsById(hotelId)) {
            ErrorCode.HOTEL_NOT_FOUND.throwServiceException()
        }
    }

    private fun saveRoomImages(roomId: Long, extensions: List<String>): PresignedUrlsResponse {
        val urls = this.s3Service.generatePresignedUrls(ImageType.ROOM, roomId, extensions)
        return PresignedUrlsResponse(roomId, urls)
    }

    @BusinessOnly
    @Transactional(readOnly = true)
    fun findAllRoomOptions(actor: Member): GetAllRoomOptionsResponse =
        GetAllRoomOptionsResponse(roomOptionRepository.findAll())
}