package com.shopease.backend.service;

import com.shopease.backend.dto.CreateOrderRequest;
import com.shopease.backend.dto.OrderDetailsResponse;
import com.shopease.backend.dto.OrderItemRequest;
import com.shopease.backend.dto.OrderResponse;
import com.shopease.backend.model.Order;
import com.shopease.backend.model.OrderItem;
import com.shopease.backend.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

@Service
public class OrderService {
    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        if (request == null || request.getCustomerId() <= 0)
            throw new RuntimeException("Invalid customer");

        if (!repository.customerExists(request.getCustomerId()))
            throw new RuntimeException("Customer not found");

        if (request.getItems() == null || request.getItems().isEmpty())
            throw new RuntimeException("Order cannot be empty");

        validatePaymentMethod(request.getPaymentMethod());

        Map<Integer, Integer> quantities = new LinkedHashMap<>();
        for (OrderItemRequest item : request.getItems()) {
            if (item == null || item.getProductId() <= 0)
                throw new RuntimeException("Invalid product");
            if (item.getQuantity() <= 0)
                throw new RuntimeException("Quantity must be greater than zero");
            quantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }

        BigDecimal total = BigDecimal.ZERO;
        Map<Integer, Map<String, Object>> products = new HashMap<>();

        for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
            Map<String, Object> product = repository.getProductForUpdate(entry.getKey());

            if (product == null)
                throw new RuntimeException("Product not found: " + entry.getKey());

            int stock = ((Number) product.get("stock_quantity")).intValue();
            if (entry.getValue() > stock)
                throw new RuntimeException("Insufficient stock for " + product.get("product_name"));

            BigDecimal price = (BigDecimal) product.get("price");
            total = total.add(price.multiply(BigDecimal.valueOf(entry.getValue())));
            products.put(entry.getKey(), product);
        }

        int orderId = repository.createOrder(request.getCustomerId(), total);

        for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
            BigDecimal price = (BigDecimal) products.get(entry.getKey()).get("price");

            repository.createOrderItem(
                    orderId, entry.getKey(), entry.getValue(), price
            );
            repository.reduceStock(entry.getKey(), entry.getValue());
        }

        repository.createPayment(orderId, total, request.getPaymentMethod());

        return new OrderResponse(
                orderId, total, "PLACED", "Order placed successfully"
        );
    }

    public List<OrderDetailsResponse> getCustomerOrders(int customerId) {
        if (!repository.customerExists(customerId))
            throw new RuntimeException("Customer not found");

        return buildResponses(repository.findOrdersByCustomer(customerId));
    }

    public List<OrderDetailsResponse> getAllOrders() {
        return buildResponses(repository.findAllOrders());
    }

    @Transactional
    public void updateOrderStatus(int orderId, String newStatus) {
        if (!isValidStatus(newStatus))
            throw new RuntimeException("Invalid order status");

        Map<String, Object> existing = repository.findOrderById(orderId);
        if (existing == null)
            throw new RuntimeException("Order not found");

        String oldStatus = String.valueOf(existing.get("status"));

        if (oldStatus.equals("CANCELLED") && !newStatus.equals("CANCELLED"))
            throw new RuntimeException("A cancelled order cannot be reopened");

        if (!oldStatus.equals("CANCELLED") && newStatus.equals("CANCELLED")) {
            for (Map<String, Object> item : repository.findOrderItems(orderId)) {
                repository.restoreStock(
                        ((Number) item.get("product_id")).intValue(),
                        ((Number) item.get("quantity")).intValue()
                );
            }
        }

        if (repository.updateOrderStatus(orderId, newStatus) == 0)
            throw new RuntimeException("Order status was not updated");
    }

    private List<OrderDetailsResponse> buildResponses(List<Map<String, Object>> rows) {
        List<OrderDetailsResponse> result = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            Order order = new Order(
                    ((Number) row.get("order_id")).intValue(),
                    ((Number) row.get("customer_id")).intValue(),
                    (Timestamp) row.get("order_date"),
                    (BigDecimal) row.get("total_amount"),
                    (String) row.get("status")
            );

            List<OrderItem> items = new ArrayList<>();
            for (Map<String, Object> item : repository.findOrderItems(order.getOrderId())) {
                items.add(new OrderItem(
                        ((Number) item.get("order_item_id")).intValue(),
                        ((Number) item.get("order_id")).intValue(),
                        ((Number) item.get("product_id")).intValue(),
                        String.valueOf(item.get("product_name")),
                        ((Number) item.get("quantity")).intValue(),
                        (BigDecimal) item.get("price")
                ));
            }
            result.add(new OrderDetailsResponse(order, items));
        }

        return result;
    }

    private void validatePaymentMethod(String method) {
        if (!"UPI".equals(method) && !"CARD".equals(method) &&
                !"NET_BANKING".equals(method) && !"COD".equals(method))
            throw new RuntimeException("Invalid payment method");
    }

    private boolean isValidStatus(String status) {
        return "PLACED".equals(status) || "CONFIRMED".equals(status) ||
                "SHIPPED".equals(status) || "DELIVERED".equals(status) ||
                "CANCELLED".equals(status);
    }
}
