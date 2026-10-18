package com.shopease.backend.controller;

import com.shopease.backend.dto.CreateOrderRequest;
import com.shopease.backend.dto.OrderResponse;
import com.shopease.backend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(service.createOrder(request));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> customerOrders(@PathVariable int customerId) {
        return ResponseEntity.ok(service.getCustomerOrders(customerId));
    }

    @GetMapping("/admin")
    public ResponseEntity<?> allOrders() {
        return ResponseEntity.ok(service.getAllOrders());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable int id,
            @RequestParam String status) {
        service.updateOrderStatus(id, status);
        return ResponseEntity.ok("Order status updated successfully");
    }
}
