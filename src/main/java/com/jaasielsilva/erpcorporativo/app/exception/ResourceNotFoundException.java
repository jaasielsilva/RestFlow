package com.jaasielsilva.erpcorporativo.app.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, ApiErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
