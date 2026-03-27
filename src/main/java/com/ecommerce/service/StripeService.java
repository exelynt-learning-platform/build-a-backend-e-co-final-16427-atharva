package com.ecommerce.service;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import javax.annotation.PostConstruct;

@Service
public class StripeService {
    private static final Logger logger = LoggerFactory.getLogger(StripeService.class);
    
    @Value("${stripe.secret.key}")
    private String secretKey;

    @PostConstruct
    public void validateConfig() {
        if (!StringUtils.hasText(secretKey)) {
            logger.error("Stripe secret key is not configured. Set STRIPE_SECRET_KEY environment variable.");
            // Don't set API key when secret is missing
            return;
        }
        
        if (secretKey.startsWith("sk_test_placeholder")) {
            logger.error("Stripe secret key is using placeholder value. Set a real STRIPE_SECRET_KEY environment variable.");
            // Don't set API key for placeholder
            return;
        }
        
        // Always set the Stripe API key when we have a valid secret
        Stripe.apiKey = secretKey;
        logger.info("Stripe configuration validated and API key set successfully");
    }

    public PaymentIntent createPaymentIntent(BigDecimal amount, String currency) throws StripeException {
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalStateException("Stripe is not properly configured. Cannot process payment.");
        }
        
        // Ensure API key is set (defensive programming)
        if (Stripe.apiKey == null || !Stripe.apiKey.equals(secretKey)) {
            Stripe.apiKey = secretKey;
        }

        // Stripe amount is in lowest currency unit (cents for USD)
        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(currency)
                .addPaymentMethodType("card")
                .build();

        try {
            return PaymentIntent.create(params);
        } catch (StripeException e) {
            logger.error("Failed to create payment intent for amount {} {}: {}", amount, currency, e.getMessage());
            throw e;
        }
    }
}
