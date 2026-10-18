package com.shopease.backend.dto;

import java.math.BigDecimal;

public class OrderResponse {
    private int orderId;
    private BigDecimal totalAmount;
    private String status;
    private String message;

    public OrderResponse(int orderId, BigDecimal totalAmount, String status, String message) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.message = message;
    }

    public int getOrderId() { return orderId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
}
