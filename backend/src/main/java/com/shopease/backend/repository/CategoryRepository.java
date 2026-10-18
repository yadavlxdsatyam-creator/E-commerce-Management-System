package com.shopease.backend.repository;

import com.shopease.backend.model.Category;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CategoryRepository {
    private final JdbcTemplate jdbcTemplate;

    public CategoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Category> findAll() {
        return jdbcTemplate.query("""
                SELECT category_id, category_name, description
                FROM categories
                ORDER BY category_id
                """, (rs, rowNum) -> new Category(
                rs.getInt("category_id"),
                rs.getString("category_name"),
                rs.getString("description")
        ));
    }

    public Category findById(int categoryId) {
        List<Category> list = jdbcTemplate.query("""
                SELECT category_id, category_name, description
                FROM categories
                WHERE category_id = ?
                """, (rs, rowNum) -> new Category(
                rs.getInt("category_id"),
                rs.getString("category_name"),
                rs.getString("description")
        ), categoryId);
        return list.isEmpty() ? null : list.get(0);
    }

    public int create(Category category) {
        return jdbcTemplate.update(
                "INSERT INTO categories (category_name, description) VALUES (?, ?)",
                category.getCategoryName(), category.getDescription()
        );
    }

    public int update(Category category) {
        return jdbcTemplate.update(
                "UPDATE categories SET category_name = ?, description = ? WHERE category_id = ?",
                category.getCategoryName(), category.getDescription(), category.getCategoryId()
        );
    }

    public int delete(int categoryId) {
        return jdbcTemplate.update(
                "DELETE FROM categories WHERE category_id = ?",
                categoryId
        );
    }
}
