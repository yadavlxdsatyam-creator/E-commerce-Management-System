package com.shopease.backend.repository;

import com.shopease.backend.model.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class ProductRepository {
    private final JdbcTemplate jdbcTemplate;

    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SELECT = """
            SELECT p.product_id, p.category_id, c.category_name,
                   p.product_name, p.description, p.price,
                   p.stock_quantity, p.image_url
            FROM products p
            JOIN categories c ON p.category_id = c.category_id
            """;

    public List<Product> findAll() {
        return jdbcTemplate.query(SELECT + " ORDER BY p.product_id", this::mapProduct);
    }

    public Product findById(int productId) {
        List<Product> list = jdbcTemplate.query(
                SELECT + " WHERE p.product_id = ?",
                this::mapProduct,
                productId
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public int create(Product product) {
        String sql = """
                INSERT INTO products
                (category_id, product_name, description, price, stock_quantity, image_url)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS
            );
            statement.setInt(1, product.getCategoryId());
            statement.setString(2, product.getProductName());
            statement.setString(3, product.getDescription());
            statement.setBigDecimal(4, product.getPrice());
            statement.setInt(5, product.getStockQuantity());
            statement.setString(6, product.getImageUrl());
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new RuntimeException("Failed to create product");
        }
        return key.intValue();
    }

    public int update(Product product) {
        return jdbcTemplate.update("""
                UPDATE products
                SET category_id = ?, product_name = ?, description = ?,
                    price = ?, stock_quantity = ?, image_url = ?
                WHERE product_id = ?
                """,
                product.getCategoryId(),
                product.getProductName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getImageUrl(),
                product.getProductId()
        );
    }

    public int delete(int productId) {
        return jdbcTemplate.update(
                "DELETE FROM products WHERE product_id = ?",
                productId
        );
    }

    private Product mapProduct(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new Product(
                rs.getInt("product_id"),
                rs.getInt("category_id"),
                rs.getString("category_name"),
                rs.getString("product_name"),
                rs.getString("description"),
                rs.getBigDecimal("price"),
                rs.getInt("stock_quantity"),
                rs.getString("image_url")
        );
    }
}
