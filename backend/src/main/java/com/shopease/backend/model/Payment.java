package com.shopease.backend.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Payment {
    private int paymentId;
    private int orderId;
    private Timestamp paymentDate;
    private BigDecimal amount;
    private String paymentMethod;
    private String paymentStatus;

    public Payment() {}

    public Payment(int paymentId, int orderId, Timestamp paymentDate, BigDecimal amount,
                   String paymentMethod, String paymentStatus) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
    }

    public int getPaymentId() { return paymentId; }
    public int getOrderId() { return orderId; }
    public Timestamp getPaymentDate() { return paymentDate; }
    public BigDecimal getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
}
