package com.crudzaso.CrudCloud.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user is not authorized to perform an action.
 *
 * Returns 403 Forbidden HTTP status.
 */
public class UnauthorizedException extends BusinessException {

    /**
     * Constructs an UnauthorizedException with a message.
     *
     * @param message the error message
     */
    public UnauthorizedException(String message) {
        super(message, HttpStatus.FORBIDDEN.value());
    }
}
