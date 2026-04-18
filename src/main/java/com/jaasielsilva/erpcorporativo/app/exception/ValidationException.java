package com.jaasielsilva.erpcorporativo.app.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends AppException {

    public ValidationException(String message) {
        super(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR, message);
    }
}
