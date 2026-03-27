package com.ecommerce.service;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
        if (secretKey == null || secretKey.isEmpty() || secretKey.startsWith("sk_test_placeholder")) {
            logger.error("Stripe secret key is missing or invalid. Payments will fail. Set STRIPE_SECRET_KEY environment variable.");
        } else {
            logger.info("Stripe configuration validated successfully");
            Stripe.apiKey = secretKey;
        }
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
