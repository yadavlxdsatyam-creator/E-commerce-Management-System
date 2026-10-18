package com.shopease.backend.repository;

import com.shopease.backend.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean emailExists(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?)",
                Integer.class, email
        );
        return count != null && count > 0;
    }

    public int createUser(String name, String email, String passwordHash, String role) {
        String sql = """
                INSERT INTO users (name, email, password_hash, role)
                VALUES (?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.setString(4, role);
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new RuntimeException("Failed to create user");
        }
        return key.intValue();
    }

    public User findByEmail(String email) {
        String sql = """
                SELECT user_id, name, email, password_hash, role
                FROM users
                WHERE LOWER(email) = LOWER(?)
                """;

        List<User> users = jdbcTemplate.query(sql, (rs, rowNum) ->
                new User(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("role")
                ), email
        );

        return users.isEmpty() ? null : users.get(0);
    }

    public int createCustomer(int userId, String phone) {
        jdbcTemplate.update(
                "INSERT INTO customers (user_id, phone) VALUES (?, ?)",
                userId, phone
        );

        Integer id = jdbcTemplate.queryForObject(
                "SELECT customer_id FROM customers WHERE user_id = ?",
                Integer.class, userId
        );

        if (id == null) {
            throw new RuntimeException("Failed to create customer");
        }
        return id;
    }

    public int findCustomerId(int userId) {
        List<Integer> ids = jdbcTemplate.query(
                "SELECT customer_id FROM customers WHERE user_id = ?",
                (rs, rowNum) -> rs.getInt("customer_id"),
                userId
        );
        return ids.isEmpty() ? 0 : ids.get(0);
    }
}
