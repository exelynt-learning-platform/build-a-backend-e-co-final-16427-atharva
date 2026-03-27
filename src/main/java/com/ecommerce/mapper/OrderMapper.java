package com.ecommerce.mapper;

import com.ecommerce.dto.OrderItemDTO;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class for converting Order entities to OrderResponse DTOs.
 * Centralizes mapping logic for consistency and maintainability.
 */
public class OrderMapper {
    
    /**
     * Converts an Order entity to an OrderResponse DTO.
     * @param order the order entity
     * @return the order response DTO
     */
    public static OrderResponse toDTO(Order order) {
        List<OrderItemDTO> items = order.getItems().stream()
                .map(OrderMapper::toOrderItemDTO)
                .collect(Collectors.toList());
        
        return new OrderResponse(
                order.getId(),
                order.getTotalPrice(),
                order.getShippingAddress(),
                order.getStatus() != null ? order.getStatus().name() : null,
                order.getPaymentId(),
                order.getCreatedAt(),
                items
        );
    }
    
    /**
     * Converts an OrderItem entity to an OrderItemDTO.
     * @param item the order item entity
     * @return the order item DTO
     */
    private static OrderItemDTO toOrderItemDTO(OrderItem item) {
        return new OrderItemDTO(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getProduct().getPrice()
        );
    }
    
    private OrderMapper() {
        // Utility class - prevent instantiation
    }
}
