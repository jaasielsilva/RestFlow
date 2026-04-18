package com.jaasielsilva.erpcorporativo.app.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends AppException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, ApiErrorCode.CONFLICT, message);
    }
}
