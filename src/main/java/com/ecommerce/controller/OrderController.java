package com.ecommerce.controller;

import java.security.Principal;
import java.math.BigDecimal;
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
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.StripeService;
import com.stripe.model.PaymentIntent;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private StripeService stripeService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

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
    public ResponseEntity<String> initiatePayment(@PathVariable Long id) throws Exception {
        Order order = orderService.getOrderById(id);
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
     * Stripe webhook endpoint — call this after receiving payment.succeeded event.
     */
    @PostMapping("/webhook/confirm/{orderId}")
    public ResponseEntity<Void> confirmPayment(@PathVariable Long orderId, @RequestParam String paymentId) {
        orderService.confirmPayment(orderId, paymentId);
        return ResponseEntity.ok().build();
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemDTO> items = order.getItems() == null ? List.of() :
                order.getItems().stream()
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
