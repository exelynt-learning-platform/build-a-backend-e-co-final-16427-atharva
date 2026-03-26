package com.ecommerce.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Creates an order from the user's cart.
     * NOTE: Cart is NOT cleared here. It should be cleared only after successful
     * payment confirmation (e.g., via a Stripe webhook handler).
     */
    @Transactional
    public Order createOrder(User user, String shippingAddress) {
        List<CartItem> cartItems = cartRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        Order order = new Order(user, BigDecimal.ZERO, shippingAddress, OrderStatus.PENDING);
        Order savedOrder = orderRepository.save(order);

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            Integer requested = cartItem.getQuantity();

            if (product.getStockQuantity() < requested) {
                throw new InsufficientStockException("Product '" + product.getName() + "' has insufficient stock");
            }

            // Decrement stock (optimistic locking via @Version handles race conditions)
            product.setStockQuantity(product.getStockQuantity() - requested);
            productRepository.save(product);

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(requested));
            total = total.add(lineTotal);

            orderItems.add(new OrderItem(savedOrder, product, requested, product.getPrice()));
        }

        savedOrder.setItems(orderItems);
        savedOrder.setTotalPrice(total);

        return orderRepository.save(savedOrder);
    }

    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUser(user);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    /**
     * Called by payment webhook/callback only — updates status after confirmed payment.
     */
    @Transactional
    public void confirmPayment(Long orderId, String paymentId) {
        Order order = getOrderById(orderId);
        order.setStatus(OrderStatus.PAID);
        order.setPaymentId(paymentId);
        orderRepository.save(order);

        // Clear cart only after successful payment
        cartRepository.deleteByUser(order.getUser());
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status, String paymentId) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        if (paymentId != null) {
            order.setPaymentId(paymentId);
        }
        orderRepository.save(order);
    }
}
