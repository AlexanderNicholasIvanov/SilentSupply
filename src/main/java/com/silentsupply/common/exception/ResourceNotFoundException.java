package com.silentsupply.common.exception;

/**
 * Thrown when a requested resource cannot be found in the system.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Creates a new ResourceNotFoundException.
     *
     * @param resourceName the type of resource that was not found
     * @param fieldName    the field used in the lookup
     * @param fieldValue   the value that was searched for
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
