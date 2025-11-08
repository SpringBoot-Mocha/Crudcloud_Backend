package com.crudzaso.CrudCloud.exception;

public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " with id " + id + " not found", "RESOURCE_NOT_FOUND");
    }
}
