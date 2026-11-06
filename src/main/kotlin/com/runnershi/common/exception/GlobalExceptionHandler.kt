package com.runnershi.common.exception

import com.runnershi.common.response.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException

/**
 * 모든 오류를 `{success:false, data:null, error:{code, message}}` 봉투로 돌려준다. 메시지는 한국어.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    private fun body(code: ErrorCode, message: String = code.message): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.status(code.status).body(ApiResponse.error(code.code, message))

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("BusinessException: ${e.errorCode.code} - ${e.message}")
        return body(e.errorCode, e.message)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val errorMessage = e.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
            .ifBlank { ErrorCode.INVALID_INPUT.message }
        log.warn("Validation failed: $errorMessage")
        return body(ErrorCode.INVALID_INPUT, errorMessage)
    }

    @ExceptionHandler(HandlerMethodValidationException::class)
    fun handleMethodValidation(e: HandlerMethodValidationException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("Method validation failed: ${e.message}")
        return body(ErrorCode.INVALID_INPUT)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("Invalid JSON: ${e.message}")
        return body(ErrorCode.INVALID_INPUT, "요청 본문을 읽을 수 없습니다")
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(e: MissingServletRequestParameterException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("Missing parameter: ${e.parameterName}")
        return body(ErrorCode.INVALID_INPUT, "필수 값이 없습니다: ${e.parameterName}")
    }

    @ExceptionHandler(MissingRequestHeaderException::class)
    fun handleMissingHeader(e: MissingRequestHeaderException): ResponseEntity<ApiResponse<Nothing>> {
        return body(ErrorCode.INVALID_INPUT, "필수 헤더가 없습니다: ${e.headerName}")
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(e: MethodArgumentTypeMismatchException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("Type mismatch: ${e.name}")
        return body(ErrorCode.INVALID_INPUT, "값의 형식이 올바르지 않습니다: ${e.name}")
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResource(e: NoResourceFoundException): ResponseEntity<ApiResponse<Nothing>> {
        return body(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 주소입니다")
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("Method not supported: ${e.method}")
        return body(ErrorCode.METHOD_NOT_ALLOWED)
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class, HttpMediaTypeNotAcceptableException::class)
    fun handleMediaType(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        return body(ErrorCode.UNSUPPORTED_MEDIA_TYPE)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleConflict(e: DataIntegrityViolationException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("Data conflict: ${e.mostSpecificCause.message}")
        return body(ErrorCode.CONFLICT)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("Access denied: ${e.message}")
        return body(ErrorCode.ACCESS_DENIED)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        log.error("Unexpected error", e)
        return body(ErrorCode.INTERNAL_SERVER_ERROR)
    }
}
