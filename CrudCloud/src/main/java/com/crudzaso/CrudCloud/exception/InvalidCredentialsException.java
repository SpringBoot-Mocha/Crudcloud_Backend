package com.crudzaso.CrudCloud.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when provided credentials are invalid.
 *
 * Returns 401 Unauthorized HTTP status.
 */
public class InvalidCredentialsException extends BusinessException {

    /**
     * Constructs an InvalidCredentialsException.
     *
     * @param message the error message
     */
    public InvalidCredentialsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED.value());
    }
}