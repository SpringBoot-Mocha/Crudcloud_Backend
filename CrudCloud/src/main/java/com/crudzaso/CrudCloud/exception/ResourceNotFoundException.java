package com.crudzaso.CrudCloud.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found.
 *
 * Returns 404 Not Found HTTP status.
 */
public class ResourceNotFoundException extends BusinessException {

    /**
     * Constructs a ResourceNotFoundException with a message.
     *
     * @param message the error message
     */
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND.value());
    }

    /**
     * Constructs a ResourceNotFoundException with a formatted message.
     *
     * @param resourceName the name of the resource
     * @param fieldName the field used for lookup
     * @param fieldValue the value of the field
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue),
              HttpStatus.NOT_FOUND.value());
    }
}
