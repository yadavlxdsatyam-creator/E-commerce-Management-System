package com.shopease.backend.dto;

import java.util.List;

public class CreateOrderRequest {
    private int customerId;
    private String paymentMethod;
    private List<OrderItemRequest> items;

    public CreateOrderRequest() {}

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
}
