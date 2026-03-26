package com.ecommerce.dto;

import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class OrderResponse {
    private Long id;
    private Double totalPrice;
    private String shippingAddress;
    private String status;
    private String paymentId;
    private Date createdAt;
    private List<OrderItemDTO> items;

    public OrderResponse(Long id, Double totalPrice, String shippingAddress, String status, String paymentId, Date createdAt, List<OrderItemDTO> items) {
        this.id = id;
        this.totalPrice = totalPrice;
        this.shippingAddress = shippingAddress;
        this.status = status;
        this.paymentId = paymentId;
        this.createdAt = createdAt;
        this.items = items;
    }
}
