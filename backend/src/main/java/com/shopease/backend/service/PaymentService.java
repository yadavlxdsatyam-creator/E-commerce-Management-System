package com.shopease.backend.service;

import com.shopease.backend.model.Payment;
import com.shopease.backend.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {
    private final PaymentRepository repository;

    public PaymentService(PaymentRepository repository) {
        this.repository = repository;
    }

    public List<Payment> getAllPayments() { return repository.findAll(); }

    public Payment getPaymentByOrderId(int orderId) {
        Payment payment = repository.findByOrderId(orderId);
        if (payment == null) throw new RuntimeException("Payment not found for order");
        return payment;
    }

    public void updatePaymentStatus(int id, String status) {
        if (!isValidStatus(status)) throw new RuntimeException("Invalid payment status");
        if (repository.updateStatus(id, status) == 0)
            throw new RuntimeException("Payment not found");
    }

    private boolean isValidStatus(String status) {
        return "PENDING".equals(status) || "SUCCESS".equals(status) ||
                "FAILED".equals(status) || "REFUNDED".equals(status);
    }
}
