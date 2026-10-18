package com.shopease.backend.service;

import com.shopease.backend.model.Category;
import com.shopease.backend.repository.CategoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    public List<Category> getAllCategories() { return repository.findAll(); }

    public Category getCategory(int id) {
        Category category = repository.findById(id);
        if (category == null) throw new RuntimeException("Category not found");
        return category;
    }

    public void createCategory(Category category) {
        validate(category);
        try {
            repository.create(category);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Category name already exists");
        }
    }

    public void updateCategory(Category category) {
        if (category == null || category.getCategoryId() <= 0)
            throw new RuntimeException("Invalid category ID");
        validate(category);
        if (repository.findById(category.getCategoryId()) == null)
            throw new RuntimeException("Category not found");

        try {
            if (repository.update(category) == 0)
                throw new RuntimeException("Category was not updated");
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Category name already exists");
        }
    }

    public void deleteCategory(int id) {
        if (repository.findById(id) == null)
            throw new RuntimeException("Category not found");
        try {
            repository.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Cannot delete a category used by products");
        }
    }

    private void validate(Category category) {
        if (category == null || category.getCategoryName() == null ||
                category.getCategoryName().isBlank()) {
            throw new RuntimeException("Category name is required");
        }
        if (category.getCategoryName().trim().length() > 100)
            throw new RuntimeException("Category name is too long");
        if (category.getDescription() != null && category.getDescription().length() > 255)
            throw new RuntimeException("Category description is too long");
    }
}
