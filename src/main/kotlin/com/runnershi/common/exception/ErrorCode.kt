package com.runnershi.common.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String
) {
    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "Invalid input value"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "Internal server error"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "Resource not found"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C004", "Method not allowed"),

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
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "User not found"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "Email already exists"),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "U003", "Nickname already exists"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "U004", "Invalid password"),

    REGION_NOT_SELECTED(HttpStatus.BAD_REQUEST, "U005", "지역을 먼저 선택해주세요"),

    // Running
    RUNNING_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "Running record not found"),
    INVALID_RUNNING_DATA(HttpStatus.BAD_REQUEST, "R002", "Invalid running data"),

    // Mission
    MISSION_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "미션 그룹을 찾을 수 없습니다"),
    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "M002", "미션을 찾을 수 없습니다")
}
