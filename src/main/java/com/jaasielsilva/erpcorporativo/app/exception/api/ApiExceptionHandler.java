package com.jaasielsilva.erpcorporativo.app.exception.api;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.jaasielsilva.erpcorporativo.app.dto.api.error.ApiErrorResponse;
import com.jaasielsilva.erpcorporativo.app.exception.ApiErrorCode;
import com.jaasielsilva.erpcorporativo.app.exception.AppException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice(basePackages = "com.jaasielsilva.erpcorporativo.app.controller.api")
public class ApiExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiErrorResponse> handleAppException(
            AppException exception,
            HttpServletRequest request
    ) {
        return buildResponse(exception.getStatus(), exception.getCode(), exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));

        return buildResponse(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR, message, request);
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            Exception exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                ApiErrorCode.METHOD_NOT_ALLOWED,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler({UsernameNotFoundException.class, AccessDeniedException.class})
    public ResponseEntity<ApiErrorResponse> handleForbidden(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = exception instanceof UsernameNotFoundException ? HttpStatus.NOT_FOUND : HttpStatus.FORBIDDEN;
        ApiErrorCode code = exception instanceof UsernameNotFoundException
                ? ApiErrorCode.RESOURCE_NOT_FOUND
                : ApiErrorCode.ACCESS_DENIED;
        return buildResponse(status, code, exception.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ApiErrorCode.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro inesperado na API.",
                request
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            ApiErrorCode code,
            String message,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                "error",
                code.name(),
                code.name().toLowerCase(),
                message,
                request.getRequestURI(),
                OffsetDateTime.now()
        );

        return ResponseEntity.status(status).body(response);
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}
