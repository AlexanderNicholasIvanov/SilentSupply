package com.silentsupply.common.exception;

/**
 * Thrown when a business rule is violated during a domain operation.
 */
public class BusinessRuleException extends RuntimeException {

    /**
     * Creates a new BusinessRuleException with the given message.
     *
     * @param message description of the violated business rule
     */
    public BusinessRuleException(String message) {
        super(message);
    }
}
