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
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A001", "Invalid token"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "Expired token"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A003", "Unauthorized"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A004", "Access denied"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "User not found"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "Email already exists"),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "U003", "Nickname already exists"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "U004", "Invalid password"),

    // Running
    RUNNING_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "Running record not found"),
    INVALID_RUNNING_DATA(HttpStatus.BAD_REQUEST, "R002", "Invalid running data")
}
