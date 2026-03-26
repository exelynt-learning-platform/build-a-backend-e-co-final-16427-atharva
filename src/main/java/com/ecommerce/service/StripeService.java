package com.ecommerce.service;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import javax.annotation.PostConstruct;

@Service
public class StripeService {
    @Value("${stripe.secret.key}")
    private String secretKey;

    @PostConstruct
    public void validateConfig() {
        if (secretKey == null || secretKey.isBlank() || secretKey.startsWith("sk_test_placeholder")) {
            System.err.println("WARNING: Stripe secret key is missing or invalid. Payments will fail. Set STRIPE_SECRET_KEY environment variable.");
        } else {
            Stripe.apiKey = secretKey;
        }
    }

    public PaymentIntent createPaymentIntent(BigDecimal amount, String currency) throws StripeException {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("Stripe is not properly configured. Cannot process payment.");
        }
        
        // Stripe.apiKey can be explicitly set here just to be safe
        Stripe.apiKey = secretKey;

        // Stripe amount is in lowest currency unit (cents for USD)
        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(currency)
                .addPaymentMethodType("card")
                .build();

        return PaymentIntent.create(params);
    }
}
