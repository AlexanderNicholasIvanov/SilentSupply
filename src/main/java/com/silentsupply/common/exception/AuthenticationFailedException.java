package com.silentsupply.common.exception;

/**
 * Thrown when authentication fails due to invalid credentials.
 */
public class AuthenticationFailedException extends RuntimeException {

    /**
     * Creates a new AuthenticationFailedException with the given message.
     *
     * @param message description of why authentication failed
     */
    public AuthenticationFailedException(String message) {
        super(message);
    }
}
