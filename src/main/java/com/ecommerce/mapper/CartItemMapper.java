package com.ecommerce.mapper;

import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.entity.CartItem;

/**
 * Utility class for mapping between CartItem entities and CartItemDTOs.
 * Follows Single Responsibility Principle by centralizing DTO mapping logic.
 */
public class CartItemMapper {
    
    /**
     * Maps a CartItem entity to a CartItemDTO.
     * @param item the CartItem entity
     * @return the CartItemDTO
     */
    public static CartItemDTO toDTO(CartItem item) {
        if (item == null) {
            return null;
        }
        
        return new CartItemDTO(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getPrice(),
                item.getQuantity(),
                item.getProduct().getImageUrl()
        );
    }
}
