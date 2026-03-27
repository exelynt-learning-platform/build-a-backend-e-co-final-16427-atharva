package com.ecommerce.constants;

/**
 * Constants for Stripe event types to avoid magic strings and improve maintainability.
 */
public class StripeConstants {
    
    public static final String PAYMENT_INTENT_SUCCEEDED = "payment_intent.succeeded";
    public static final String PAYMENT_INTENT_PAYMENT_FAILED = "payment_intent.payment_failed";
    public static final String PAYMENT_INTENT_CANCELED = "payment_intent.canceled";
    public static final String PAYMENT_INTENT_PROCESSING = "payment_intent.processing";
    
    public static final String WEBHOOK_SIGNATURE_HEADER = "Stripe-Signature";
    
    private StripeConstants() {
        // Utility class - prevent instantiation
    }
}
