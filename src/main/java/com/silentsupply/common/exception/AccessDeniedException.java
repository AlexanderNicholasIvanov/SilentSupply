package com.silentsupply.common.exception;

/**
 * Thrown when a user attempts an action they are not authorized to perform.
 */
public class AccessDeniedException extends RuntimeException {

    /**
     * Creates a new AccessDeniedException with the given message.
     *
     * @param message description of why access was denied
     */
    public AccessDeniedException(String message) {
        super(message);
    }
}
