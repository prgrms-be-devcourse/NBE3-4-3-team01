package com.ll.hotel.global.exceptions

import org.springframework.http.HttpStatus
import software.amazon.awssdk.services.s3.model.S3Exception
import java.lang.reflect.InvocationTargetException

enum class ErrorCode(val httpStatus: HttpStatus, val message: String) {
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // common
    PAGE_NOT_FOUND(HttpStatus.BAD_REQUEST, "페이지가 존재하지 않습니다"),

    // api
    EXTERNAL_API_UNEXPECTED_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "API 응답이 올바르지 않습니다"),
    EXTERNAL_API_COMMUNICATION_ERROR(HttpStatus.BAD_GATEWAY, "API 요청 중 오류가 발생했습니다."),

    // jwt
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_LOGGED_OUT(HttpStatus.UNAUTHORIZED, "로그아웃된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "리프레시 토큰을 찾을 수 없습니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 리프레시 토큰입니다."),

    // member + oauth
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원이 존재하지 않습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다."),
    OAUTH_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 OAuth 정보를 찾을 수 없습니다."),
    OAUTH_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "OAuth 로그인에 실패했습니다."),

    // favorite
    FAVORITE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 즐겨찾기에 추가된 호텔입니다."),
    FAVORITE_NOT_FOUND(HttpStatus.BAD_REQUEST, "즐겨찾기에 없는 호텔입니다."),

    // Business
    BUSINESS_ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, "사업자만 관리할 수 있습니다."),
    INVALID_BUSINESS(HttpStatus.FORBIDDEN, "해당 호텔의 사업자가 아닙니다."),
    BUSINESS_NOT_FOUND(HttpStatus.NOT_FOUND, "사업자가 존재하지 않습니다."),
    BUSINESS_HOTEL_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "한 사업자는 하나의 호텔만 등록할 수 있습니다."),
    INVALID_BUSINESS_INFO(HttpStatus.BAD_REQUEST, "사업자가 유효하지 않습니다"),

    // Hotel
    HOTEL_NOT_FOUND(HttpStatus.NOT_FOUND, "호텔이 존재하지 않습니다"),
    HOTEL_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "호텔 상태 정보를 정확히 입력해주세요."),
    HOTEL_EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "동일한 이메일의 호텔이 이미 존재합니다."),

    // Room
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "객실이 존재하지 않습니다"),
    ROOM_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "객실 상태 정보를 정확히 입력해주세요."),
    ROOM_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "동일한 이름의 방이 이미 호텔에 존재합니다."),

    // option
    OPTION_IN_USE(HttpStatus.BAD_REQUEST, "사용 중인 옵션은 삭제할 수 없습니다"),
    HOTEL_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "사용할 수 없는 호텔 옵션이 존재합니다."),
    ROOM_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "사용할 수 없는 객실 옵션이 존재합니다."),

    // Review
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰가 존재하지 않습니다"),
    REVIEW_CREATION_FORBIDDEN(HttpStatus.FORBIDDEN, "리뷰 생성 권한이 없습니다"),
    REVIEW_ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, "리뷰 조회 권한이 없습니다"),
    REVIEW_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "리뷰 수정 권한이 없습니다"),
    REVIEW_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "리뷰 삭제 권한이 없습니다"),
    REVIEW_IMAGE_REGISTRATION_FORBIDDEN(HttpStatus.FORBIDDEN, "리뷰 사진 저장 권한이 없습니다"),
    USER_REVIEW_ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, "내 리뷰 목록 조회는 손님만 가능합니다"),

    // ReviewComment
    REVIEW_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰 답변이 존재하지 않습니다"),
    REVIEW_COMMENT_CREATION_FORBIDDEN(HttpStatus.FORBIDDEN, "리뷰 답변 생성 권한이 없습니다"),
    REVIEW_COMMENT_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "리뷰 답변 수정 권한이 없습니다"),
    REVIEW_COMMENT_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "리뷰 답변 삭제 권한이 없습니다"),

    // S3
    S3_PRESIGNED_GENERATION_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "Presigned URL 생성 실패"),
    S3_OBJECT_DELETE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "S3 객체 삭제 실패"),
    S3_OBJECT_ACCESS_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "S3 객체 조회 실패"),

    // Booking
    BOOKING_CANCEL_TO_CANCEL(HttpStatus.BAD_REQUEST, "이미 취소된 예약입니다."),
    BOOKING_COMPLETE_TO_CANCEL(HttpStatus.BAD_REQUEST, "완료된 예약은 취소할 수 없습니다."),
    BOOKING_COMPLETE_TO_COMPLETE(HttpStatus.BAD_REQUEST, "이미 완료된 예약입니다."),
    BOOKING_CANCEL_TO_COMPLETE(HttpStatus.BAD_REQUEST, "취소된 예약은 완료 처리할 수 없습니다."),
    BOOKING_ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, "예약 조회 권한이 없습니다."),
    BOOKING_CANCEL_FORBIDDEN(HttpStatus.FORBIDDEN, "예약 취소 권한이 없습니다."),
    BOOKING_COMPLETE_FORBIDDEN(HttpStatus.FORBIDDEN, "예약 완료 처리 권한이 없습니다."),
    BOOKING_NOT_FOUND(HttpStatus.NOT_FOUND, "예약 정보를 찾을 수 없습니다."),
    BOOKING_CREATE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "예약 생성에 실패했습니다."),
    BOOKING_CANCEL_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "예약 취소에 실패했습니다."),
    BOOKING_COMPLETE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "예약 완료 처리에 실패했습니다."),

    // Payment
    PAYMENT_TOKEN_FORBIDDEN(HttpStatus.FORBIDDEN, "토큰 발급 권한이 없습니다."),
    PAYMENT_CANCEL_FORBIDDEN(HttpStatus.FORBIDDEN, "결제 취소 권한이 없습니다."),
    PAYMENT_CANCEL_TO_CANCEL(HttpStatus.BAD_REQUEST, "이미 취소된 결제입니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보를 찾을 수 없습니다."),
    PAYMENT_UID_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "Uid 생성에 실패했습니다."),
    PAYMENT_CREATE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "결제 정보 저장에 실패했습니다."),
    PAYMENT_TOKEN_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 발급에 실패했습니다."),
    PAYMENT_CANCEL_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "결제 취소에 실패했습니다."),

    // Filtering
    PAGE_SIZE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "pageSize는 1에서 100 사이로 입력해주세요."),
    INVALID_CHECK_IN_OUT_DATE(HttpStatus.BAD_REQUEST, "체크인 날짜는 체크아웃 날짜보다 늦을 수 없습니다."),
    INVALID_FILTER_DIRECTION(HttpStatus.BAD_REQUEST, "정렬 방향은 ASC 또는 DESC만 가능합니다.");

    fun throwServiceException(): ServiceException {
        throw ServiceException(httpStatus, message)
    }

    fun throwServiceException(cause: Throwable): ServiceException {
        throw ServiceException(httpStatus, message, cause)
    }

    fun throwS3Exception(cause: Throwable): S3Exception {
        throw CustomS3Exception(httpStatus, message, cause)
    }
}