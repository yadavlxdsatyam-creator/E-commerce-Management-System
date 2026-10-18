package com.shopease.backend.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Order {
    private int orderId;
    private int customerId;
    private Timestamp orderDate;
    private BigDecimal totalAmount;
    private String status;

    public Order() {}

    public Order(int orderId, int customerId, Timestamp orderDate,
                 BigDecimal totalAmount, String status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public int getOrderId() { return orderId; }
    public int getCustomerId() { return customerId; }
    public Timestamp getOrderDate() { return orderDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
}
