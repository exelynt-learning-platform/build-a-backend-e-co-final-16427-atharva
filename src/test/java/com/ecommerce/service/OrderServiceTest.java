package com.ecommerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ecommerce.entity.*;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;

public class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

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
        product.setPrice(100.0);
        product.setStockQuantity(10);
        cartItem = new CartItem(user, product, 2);
    }

    @Test
    public void testCreateOrder_Success() {
        when(cartService.getCartItems(user)).thenReturn(Collections.singletonList(cartItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        Order order = orderService.createOrder(user, "123 Street");

        assertNotNull(order);
        assertEquals(200.0, order.getTotalPrice());
        assertEquals("PENDING", order.getStatus());
        verify(productRepository, times(1)).save(product);
        verify(cartService, times(1)).clearCart(user);
    }

    @Test
    public void testCreateOrder_EmptyCart() {
        when(cartService.getCartItems(user)).thenReturn(Collections.emptyList());

        assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(user, "123 Street");
        });
    }
}
