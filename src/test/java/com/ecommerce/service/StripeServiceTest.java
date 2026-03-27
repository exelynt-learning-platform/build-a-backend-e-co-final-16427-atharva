package com.ecommerce.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class StripeServiceTest {
    
    @InjectMocks
    private StripeService stripeService;
    
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(stripeService, "secretKey", "sk_test_valid_key");
    }
    
    @Test
    void testValidateConfig_ValidKey() {
        ReflectionTestUtils.setField(stripeService, "secretKey", "sk_test_valid_key");
        assertDoesNotThrow(() -> stripeService.validateConfig());
    }
    
    @Test
    void testValidateConfig_NullKey() {
        ReflectionTestUtils.setField(stripeService, "secretKey", null);
        assertDoesNotThrow(() -> stripeService.validateConfig());
    }
    
    @Test
    void testValidateConfig_EmptyKey() {
        ReflectionTestUtils.setField(stripeService, "secretKey", "");
        assertDoesNotThrow(() -> stripeService.validateConfig());
    }
    
    @Test
    void testValidateConfig_PlaceholderKey() {
        ReflectionTestUtils.setField(stripeService, "secretKey", "sk_test_placeholder");
        assertDoesNotThrow(() -> stripeService.validateConfig());
    }
    
    @Test
    void testCreatePaymentIntent_NullSecretKey() {
        ReflectionTestUtils.setField(stripeService, "secretKey", null);
        
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> stripeService.createPaymentIntent(new BigDecimal("100.00"), "usd")
        );
        
        assertEquals("Stripe is not properly configured. Cannot process payment.", exception.getMessage());
    }
    
    @Test
    void testCreatePaymentIntent_EmptySecretKey() {
        ReflectionTestUtils.setField(stripeService, "secretKey", "");
        
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> stripeService.createPaymentIntent(new BigDecimal("100.00"), "usd")
        );
        
        assertEquals("Stripe is not properly configured. Cannot process payment.", exception.getMessage());
    }
}
