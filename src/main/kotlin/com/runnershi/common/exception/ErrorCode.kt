package com.runnershi.common.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String
) {
    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "요청 값이 올바르지 않습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버에 문제가 생겼습니다. 잠시 후 다시 시도해 주세요"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "요청한 정보를 찾을 수 없습니다"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C004", "허용되지 않는 요청 방식입니다"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "C005", "지원하지 않는 요청 형식입니다"),
    CONFLICT(HttpStatus.CONFLICT, "C006", "이미 처리된 요청이거나 요청이 충돌합니다"),

    // Auth
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A001", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "토큰이 만료되었습니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A003", "인증이 필요합니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A004", "접근 권한이 없습니다"),
    TOKEN_REQUIRED(HttpStatus.UNAUTHORIZED, "A005", "토큰이 필요합니다"),
    MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "A006", "잘못된 토큰 형식입니다"),
    ACCESS_TOKEN_REQUIRED(HttpStatus.UNAUTHORIZED, "A007", "Access Token이 필요합니다"),
    REFRESH_TOKEN_REQUIRED(HttpStatus.UNAUTHORIZED, "A008", "Refresh Token이 필요합니다"),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "A009", "Refresh Token이 일치하지 않습니다"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "회원을 찾을 수 없습니다"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "이미 사용 중인 이메일입니다"),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "U003", "이미 사용 중인 닉네임입니다"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "U004", "비밀번호가 올바르지 않습니다"),

    REGION_NOT_SELECTED(HttpStatus.BAD_REQUEST, "U005", "지역을 먼저 선택해주세요"),
    INVALID_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "U006", "지원하지 않는 이미지 형식입니다"),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "U007", "이미지 업로드에 실패했습니다"),
    INVALID_OBJECT_KEY(HttpStatus.BAD_REQUEST, "U008", "잘못된 이미지 경로입니다"),

    // Running
    RUNNING_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "러닝 기록을 찾을 수 없습니다"),
    INVALID_RUNNING_DATA(HttpStatus.BAD_REQUEST, "R002", "러닝 기록 값이 올바르지 않습니다"),

    // Mission
    MISSION_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "미션 그룹을 찾을 수 없습니다"),
    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "M002", "미션을 찾을 수 없습니다"),

    // Notice
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "N001", "공지사항을 찾을 수 없습니다"),

    // App
    PLATFORM_NOT_FOUND(HttpStatus.BAD_REQUEST, "AP001", "지원하지 않는 플랫폼입니다")
}
