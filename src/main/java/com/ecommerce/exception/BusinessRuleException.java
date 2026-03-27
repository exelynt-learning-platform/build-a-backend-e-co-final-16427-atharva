package com.ecommerce.exception;

/**
 * Exception thrown when a business rule is violated.
 * More specific than generic RuntimeException for business logic violations.
 */
public class BusinessRuleException extends RuntimeException {
    
    public BusinessRuleException(String message) {
        super(message);
    }
    
    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
