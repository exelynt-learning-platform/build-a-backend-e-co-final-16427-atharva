package com.ecommerce.controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.CartService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public List<CartItemDTO> getCart(Principal principal) {
        User user = getCurrentUser(principal);
        return cartService.getCartItems(user).stream()
                .map(item -> new CartItemDTO(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getPrice(),
                        item.getQuantity(),
                        item.getProduct().getImageUrl()
                ))
                .collect(Collectors.toList());
    }

    @PostMapping("/add")
    public ResponseEntity<CartItemDTO> addToCart(Principal principal, @RequestParam Long productId, @RequestParam Integer quantity) {
        User user = getCurrentUser(principal);
        CartItem item = cartService.addToCart(user, productId, quantity);
        return ResponseEntity.ok(new CartItemDTO(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getPrice(),
                item.getQuantity(),
                item.getProduct().getImageUrl()
        ));
    }

    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<CartItemDTO> updateCartItem(Principal principal, @PathVariable Long cartItemId, @RequestParam Integer quantity) {
        User user = getCurrentUser(principal);
        CartItem item = cartService.updateCartItem(user, cartItemId, quantity);
        return ResponseEntity.ok(new CartItemDTO(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getPrice(),
                item.getQuantity(),
                item.getProduct().getImageUrl()
        ));
    }

    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<Void> removeFromCart(Principal principal, @PathVariable Long cartItemId) {
        User user = getCurrentUser(principal);
        cartService.removeFromCart(user, cartItemId);
        return ResponseEntity.ok().build();
    }
}
