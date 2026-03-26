package com.ecommerce.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class OrderResponse {
    private Long id;
    private BigDecimal totalPrice;
    private String shippingAddress;
    private String status;
    private String paymentId;
    private Date createdAt;
    private List<OrderItemDTO> items;

    public OrderResponse(Long id, BigDecimal totalPrice, String shippingAddress, String status, String paymentId, Date createdAt, List<OrderItemDTO> items) {
        this.id = id;
        this.totalPrice = totalPrice;
        this.shippingAddress = shippingAddress;
        this.status = status;
        this.paymentId = paymentId;
        this.createdAt = createdAt;
        this.items = items;
    }
}
