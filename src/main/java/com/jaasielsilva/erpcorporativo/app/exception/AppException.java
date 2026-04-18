package com.jaasielsilva.erpcorporativo.app.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    private final HttpStatus status;
    private final ApiErrorCode code;

    public AppException(HttpStatus status, ApiErrorCode code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
