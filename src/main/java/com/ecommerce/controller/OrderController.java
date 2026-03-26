package com.ecommerce.controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ecommerce.dto.OrderItemDTO;
import com.ecommerce.dto.OrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.StripeService;
import com.stripe.model.PaymentIntent;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/orders")
public class OrderController {
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

    @PostMapping("/{id}/pay")
    public ResponseEntity<String> initiatePayment(@PathVariable Long id) throws Exception {
        Order order = orderService.getOrderById(id);
        if (!"PENDING".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body("Order is not in PENDING status");
        }

        PaymentIntent intent = stripeService.createPaymentIntent(order.getTotalPrice(), "usd");
        orderService.updateOrderStatus(id, "PROCESSED", intent.getId()); // In real app, update to PAID after webhook
        
        return ResponseEntity.ok(intent.getClientSecret());
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemDTO> items = order.getItems().stream()
                .map(item -> new OrderItemDTO(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getTotalPrice(),
                order.getShippingAddress(),
                order.getStatus(),
                order.getPaymentId(),
                order.getCreatedAt(),
                items
        );
    }
}
