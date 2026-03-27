package com.ecommerce.controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ecommerce.dto.OrderItemDTO;
import com.ecommerce.dto.OrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.entity.User;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.StripeService;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.exception.SignatureVerificationException;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/orders")
public class OrderController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private StripeService stripeService;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(Principal principal, @Valid @RequestBody OrderRequest orderRequest) {
        User user = getCurrentUser(principal);
        Order order = orderService.createOrder(user, orderRequest.getShippingAddress());
        return ResponseEntity.ok(mapToResponse(order));
    }

    @GetMapping
    public List<OrderResponse> getUserOrders(Principal principal) {
        User user = getCurrentUser(principal);
        return orderService.getUserOrders(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(mapToResponse(order));
    }

    /**
     * Initiates payment for a PENDING order.
     * Status is set to PROCESSED (payment initiated), not PAID.
     * Use the /webhook endpoint to confirm actual payment from Stripe.
     */
    @PostMapping("/{id}/pay")
    public ResponseEntity<String> initiatePayment(Principal principal, @PathVariable Long id) throws Exception {
        User user = getCurrentUser(principal);
        Order order = orderService.getOrderById(id);
        
        if (!order.getUser().getId().equals(user.getId())) {
            throw new com.ecommerce.exception.UnauthorizedAccessException("Unauthorized access to order");
        }
        
        if (order.getStatus() != OrderStatus.PENDING) {
            return ResponseEntity.badRequest().body("Order is not in PENDING status");
        }

        PaymentIntent intent = stripeService.createPaymentIntent(order.getTotalPrice(), "usd");
        // Mark as PROCESSED (payment intent created), not PAID — PAID set via webhook
        orderService.updateOrderStatus(id, OrderStatus.PROCESSED, intent.getId());
        logger.info("Payment intent {} created for order {}", intent.getId(), id);

        return ResponseEntity.ok(intent.getClientSecret());
    }

    /**
     * Stripe webhook endpoint — handles payment confirmation from Stripe.
     * Extracts order from payment intent metadata instead of URL path.
     */
    @PostMapping("/webhook/confirm")
    public ResponseEntity<String> confirmPayment(
            @RequestBody String payload, 
            @RequestHeader("Stripe-Signature") String sigHeader) {
        
        try {
            if (webhookSecret == null || webhookSecret.isEmpty()) {
                logger.error("Webhook secret not configured. Cannot verify Stripe signature.");
                return ResponseEntity.status(500).body("Webhook not properly configured");
            }
            
            // Proper Stripe event construction and verification
            com.stripe.model.Event event = Webhook.constructEvent(
                payload, sigHeader, webhookSecret
            );
            
            // Process the event based on type
            if ("payment_intent.succeeded".equals(event.getType())) {
                com.stripe.model.PaymentIntent paymentIntent = (com.stripe.model.PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (paymentIntent != null) {
                    // Find order by payment intent ID
                    orderService.confirmPaymentByIntentId(paymentIntent.getId(), paymentIntent.getId());
                    logger.info("Payment confirmed for payment intent {} via webhook", paymentIntent.getId());
                    return ResponseEntity.ok("Payment confirmed");
                }
            } else if ("payment_intent.payment_failed".equals(event.getType())) {
                com.stripe.model.PaymentIntent paymentIntent = (com.stripe.model.PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
                if (paymentIntent != null) {
                    orderService.handlePaymentFailure(paymentIntent.getId());
                    logger.warn("Payment failed for payment intent {}", paymentIntent.getId());
                    return ResponseEntity.ok("Payment failure recorded");
                }
            }
            
            return ResponseEntity.ok("Event processed");
        } catch (SignatureVerificationException e) {
            logger.error("Stripe signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(400).body("Invalid signature");
        } catch (Exception e) {
            logger.error("Error processing webhook: {}", e.getMessage());
            return ResponseEntity.status(500).body("Internal error");
        }
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemDTO> items = order.getItems().stream()
                        .map(item -> new OrderItemDTO(
                                item.getProduct().getId(),
                                item.getProduct().getName(),
                                item.getQuantity(),
                                item.getPrice()))
                        .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getTotalPrice(),
                order.getShippingAddress(),
                order.getStatus() != null ? order.getStatus().name() : null,
                order.getPaymentId(),
                order.getCreatedAt(),
                items);
    }
}
