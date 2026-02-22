package com.silentsupply.common.exception;

/**
 * Exception thrown when file storage operations fail (upload, download, or delete).
 */
public class FileStorageException extends RuntimeException {

    /**
     * Creates a new FileStorageException with the given message.
     *
     * @param message the error message
     */
    public FileStorageException(String message) {
        super(message);
    }

    /**
     * Creates a new FileStorageException with the given message and cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
