package com.shopease.backend.dto;

import com.shopease.backend.model.Order;
import com.shopease.backend.model.OrderItem;
import java.util.List;

public class OrderDetailsResponse {
    private Order order;
    private List<OrderItem> items;

    public OrderDetailsResponse(Order order, List<OrderItem> items) {
        this.order = order;
        this.items = items;
    }

    public Order getOrder() { return order; }
    public List<OrderItem> getItems() { return items; }
}
