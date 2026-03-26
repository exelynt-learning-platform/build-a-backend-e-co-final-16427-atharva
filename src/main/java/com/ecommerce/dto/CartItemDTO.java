package com.ecommerce.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class CartItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private String imageUrl;

    public CartItemDTO(Long id, Long productId, String productName, BigDecimal unitPrice, Integer quantity, String imageUrl) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }
}
