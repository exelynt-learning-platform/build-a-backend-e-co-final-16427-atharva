package com.ecommerce.entity;

import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

    private Double totalPrice;
    private String shippingAddress;
    private String status; // PENDING, PAID, SHIPPED, DELIVERED, CANCELLED
    private String paymentId;
    private Date createdAt;

    public Order(User user, Double totalPrice, String shippingAddress, String status) {
        this.user = user;
        this.totalPrice = totalPrice;
        this.shippingAddress = shippingAddress;
        this.status = status;
        this.createdAt = new Date();
    }
}
