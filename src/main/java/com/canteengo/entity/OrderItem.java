package com.canteengo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter @Setter
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private FoodOrder order;

    /** Plain id (no FK) so a menu item can be removed without breaking order history. */
    private Long foodId;
    private String foodName;
    private int quantity;
    private double price; // unit price at time of order
}
