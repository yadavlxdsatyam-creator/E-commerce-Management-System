package com.shopease.backend.repository;

import com.shopease.backend.model.Customer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomerRepository {
    private final JdbcTemplate jdbcTemplate;

    public CustomerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SELECT = """
            SELECT c.customer_id, c.user_id, u.name, u.email,
                   c.phone, c.address, c.city, c.state, c.pincode
            FROM customers c
            JOIN users u ON c.user_id = u.user_id
            """;

    public List<Customer> findAll() {
        return jdbcTemplate.query(SELECT + " ORDER BY c.customer_id", this::mapCustomer);
    }

    public Customer findById(int customerId) {
        List<Customer> list = jdbcTemplate.query(
                SELECT + " WHERE c.customer_id = ?",
                this::mapCustomer,
                customerId
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public int updateCustomer(Customer customer) {
        return jdbcTemplate.update("""
                UPDATE customers
                SET phone = ?, address = ?, city = ?, state = ?, pincode = ?
                WHERE customer_id = ?
                """,
                customer.getPhone(),
                customer.getAddress(),
                customer.getCity(),
                customer.getState(),
                customer.getPincode(),
                customer.getCustomerId()
        );
    }

    private Customer mapCustomer(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new Customer(
                rs.getInt("customer_id"),
                rs.getInt("user_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getString("city"),
                rs.getString("state"),
                rs.getString("pincode")
        );
    }
}
