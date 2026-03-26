package com.ecommerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ecommerce.entity.*;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;

public class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    private User user;
    private Product product;
    private CartItem cartItem;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100.0));
        product.setStockQuantity(10);
        cartItem = new CartItem(user, product, 2);
    }

    @Test
    public void testCreateOrder_Success() {
        when(cartRepository.findByUser(user)).thenReturn(Collections.singletonList(cartItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        Order order = orderService.createOrder(user, "123 Street");

        assertNotNull(order);
        assertEquals(BigDecimal.valueOf(200.0), order.getTotalPrice());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        verify(productRepository, times(1)).save(product);
        // Cart is no longer cleared on order creation — only after payment confirmed
        verify(cartRepository, never()).deleteByUser(user);
    }

    @Test
    public void testCreateOrder_EmptyCart() {
        when(cartRepository.findByUser(user)).thenReturn(Collections.emptyList());

        assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(user, "123 Street");
        });
    }
}
