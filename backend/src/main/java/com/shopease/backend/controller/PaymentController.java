package com.shopease.backend.controller;

import com.shopease.backend.model.Payment;
import com.shopease.backend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    private final PaymentService service;

    public PaymentController(PaymentService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<List<Payment>> getAll() {
        return ResponseEntity.ok(service.getAllPayments());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getByOrder(@PathVariable int orderId) {
        return ResponseEntity.ok(service.getPaymentByOrderId(orderId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable int id,
            @RequestParam String status) {
        service.updatePaymentStatus(id, status);
        return ResponseEntity.ok("Payment status updated successfully");
    }
}
