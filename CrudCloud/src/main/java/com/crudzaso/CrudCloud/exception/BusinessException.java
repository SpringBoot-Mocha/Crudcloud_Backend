package com.crudzaso.CrudCloud.exception;

/**
 * Base exception for business logic errors.
 *
 * All custom business exceptions should extend this class.
 */
public class BusinessException extends RuntimeException {

    /**
     * HTTP status code associated with this exception.
     */
    private final int statusCode;

    /**
     * Constructs a BusinessException with a message and status code.
     *
     * @param message the error message
     * @param statusCode the HTTP status code
     */
    public BusinessException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Constructs a BusinessException with a message, status code, and cause.
     *
     * @param message the error message
     * @param statusCode the HTTP status code
     * @param cause the underlying cause
     */
    public BusinessException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    /**
     * Gets the HTTP status code for this exception.
     *
     * @return the status code
     */
    public int getStatusCode() {
        return statusCode;
    }
}