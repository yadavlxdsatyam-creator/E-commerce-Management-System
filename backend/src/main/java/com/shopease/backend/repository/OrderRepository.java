package com.shopease.backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Repository
public class OrderRepository {
    private final JdbcTemplate jdbcTemplate;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean customerExists(int customerId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customers WHERE customer_id = ?",
                Integer.class, customerId
        );
        return count != null && count > 0;
    }

    public Map<String, Object> getProductForUpdate(int productId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT product_id, product_name, price, stock_quantity
                FROM products
                WHERE product_id = ?
                FOR UPDATE
                """, productId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int createOrder(int customerId, BigDecimal totalAmount) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO orders (customer_id, total_amount, status)
                    VALUES (?, ?, 'PLACED')
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, customerId);
            statement.setBigDecimal(2, totalAmount);
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new RuntimeException("Failed to create order");
        }
        return key.intValue();
    }

    public void createOrderItem(int orderId, int productId, int quantity, BigDecimal price) {
        jdbcTemplate.update("""
                INSERT INTO order_items (order_id, product_id, quantity, price)
                VALUES (?, ?, ?, ?)
                """, orderId, productId, quantity, price);
    }

    public void reduceStock(int productId, int quantity) {
        int updated = jdbcTemplate.update("""
                UPDATE products
                SET stock_quantity = stock_quantity - ?
                WHERE product_id = ? AND stock_quantity >= ?
                """, quantity, productId, quantity);

        if (updated == 0) {
            throw new RuntimeException("Insufficient stock");
        }
    }

    public void restoreStock(int productId, int quantity) {
        jdbcTemplate.update("""
                UPDATE products
                SET stock_quantity = stock_quantity + ?
                WHERE product_id = ?
                """, quantity, productId);
    }

    public void createPayment(int orderId, BigDecimal amount, String paymentMethod) {
        jdbcTemplate.update("""
                INSERT INTO payments (order_id, amount, payment_method, payment_status)
                VALUES (?, ?, ?, ?)
                """,
                orderId,
                amount,
                paymentMethod,
                "COD".equals(paymentMethod) ? "PENDING" : "SUCCESS"
        );
    }

    public Map<String, Object> findOrderById(int orderId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT order_id, customer_id, order_date, total_amount, status
                FROM orders
                WHERE order_id = ?
                """, orderId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<Map<String, Object>> findOrdersByCustomer(int customerId) {
        return jdbcTemplate.queryForList("""
                SELECT order_id, customer_id, order_date, total_amount, status
                FROM orders
                WHERE customer_id = ?
                ORDER BY order_date DESC
                """, customerId);
    }

    public List<Map<String, Object>> findOrderItems(int orderId) {
        return jdbcTemplate.queryForList("""
                SELECT oi.order_item_id, oi.order_id, oi.product_id,
                       p.product_name, oi.quantity, oi.price
                FROM order_items oi
                JOIN products p ON oi.product_id = p.product_id
                WHERE oi.order_id = ?
                """, orderId);
    }

    public List<Map<String, Object>> findAllOrders() {
        return jdbcTemplate.queryForList("""
                SELECT o.order_id, o.customer_id, u.name AS customer_name,
                       u.email AS customer_email, c.phone, o.order_date,
                       o.total_amount, o.status
                FROM orders o
                JOIN customers c ON o.customer_id = c.customer_id
                JOIN users u ON c.user_id = u.user_id
                ORDER BY o.order_date DESC
                """);
    }

    public int updateOrderStatus(int orderId, String status) {
        return jdbcTemplate.update(
                "UPDATE orders SET status = ? WHERE order_id = ?",
                status, orderId
        );
    }
}
