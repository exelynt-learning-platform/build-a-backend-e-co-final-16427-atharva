package com.ecommerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;

public class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    private User user;
    private Product product;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(10.0);
        product.setStockQuantity(5);
    }

    @Test
    public void testAddToCart_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());
        when(cartRepository.save(any(CartItem.class))).thenAnswer(i -> i.getArguments()[0]);

        CartItem item = cartService.addToCart(user, 1L, 2);

        assertNotNull(item);
        assertEquals(2, item.getQuantity());
        assertEquals(product, item.getProduct());
        verify(cartRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    public void testAddToCart_InsufficientStock() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            cartService.addToCart(user, 1L, 10);
        });

        assertEquals("Insufficient stock", exception.getMessage());
    }
}
