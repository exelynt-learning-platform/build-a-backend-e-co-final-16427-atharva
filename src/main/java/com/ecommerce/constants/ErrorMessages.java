package com.ecommerce.constants;

/**
 * Constants for error messages to improve maintainability and avoid magic strings.
 */
public class ErrorMessages {
    
    public static final String ROLE_NOT_FOUND = "Error: Role is not found.";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String PRODUCT_NOT_FOUND = "Product not found with id: %s";
    public static final String ORDER_NOT_FOUND = "Order not found with id: %s";
    public static final String CART_ITEM_NOT_FOUND = "Cart item not found with id: %s";
    
    public static final String INSUFFICIENT_STOCK = "Product '%s' has insufficient stock";
    public static final String INSUFFICIENT_STOCK_TEMPLATE = "Insufficient stock for product: %s";
    
    public static final String UNAUTHORIZED_ACCESS = "Unauthorized access to %s";
    
    public static final String CART_EMPTY = "Cart is empty";
    public static final String ORDER_NOT_PROCESSED = "Cannot confirm payment: order is not in PROCESSED status";
    
    public static final String JWT_SECRET_REQUIRED = "JWT secret must be provided via JWT_SECRET environment variable (minimum 32 characters). Application cannot start without proper JWT configuration for security. For development, create a .env file with: JWT_SECRET=your-32-character-secret-key";
    public static final String JWT_SECRET_TOO_SHORT = "JWT secret must be at least 32 characters (256 bits) for security. Current length: %d characters. Please use a longer secret for production.";
    
    public static final String STRIPE_NOT_CONFIGURED = "Stripe secret key is not configured. Set STRIPE_SECRET_KEY environment variable.";
    public static final String STRIPE_PLACEHOLDER_KEY = "Stripe secret key is using placeholder value. Set a real STRIPE_SECRET_KEY environment variable.";
    
    private ErrorMessages() {
        // Utility class - prevent instantiation
    }
}
