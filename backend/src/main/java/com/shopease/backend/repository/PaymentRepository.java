package com.shopease.backend.repository;

import com.shopease.backend.model.Payment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PaymentRepository {
    private final JdbcTemplate jdbcTemplate;

    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Payment> findAll() {
        return jdbcTemplate.query("""
                SELECT payment_id, order_id, payment_date, amount,
                       payment_method, payment_status
                FROM payments
                ORDER BY payment_date DESC
                """, this::mapPayment);
    }

    public Payment findByOrderId(int orderId) {
        List<Payment> list = jdbcTemplate.query("""
                SELECT payment_id, order_id, payment_date, amount,
                       payment_method, payment_status
                FROM payments
                WHERE order_id = ?
                """, this::mapPayment, orderId);
        return list.isEmpty() ? null : list.get(0);
    }

    public int updateStatus(int paymentId, String paymentStatus) {
        return jdbcTemplate.update(
                "UPDATE payments SET payment_status = ? WHERE payment_id = ?",
                paymentStatus, paymentId
        );
    }

    private Payment mapPayment(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new Payment(
                rs.getInt("payment_id"),
                rs.getInt("order_id"),
                rs.getTimestamp("payment_date"),
                rs.getBigDecimal("amount"),
                rs.getString("payment_method"),
                rs.getString("payment_status")
        );
    }
}
