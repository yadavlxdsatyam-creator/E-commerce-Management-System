package com.shopease.backend.controller;

import com.shopease.backend.model.Category;
import com.shopease.backend.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {
    private final CategoryService service;

    public CategoryController(CategoryService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<List<Category>> getAll() {
        return ResponseEntity.ok(service.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getOne(@PathVariable int id) {
        return ResponseEntity.ok(service.getCategory(id));
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody Category category) {
        service.createCategory(category);
        return ResponseEntity.ok("Category created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable int id, @RequestBody Category category) {
        category.setCategoryId(id);
        service.updateCategory(category);
        return ResponseEntity.ok("Category updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable int id) {
        service.deleteCategory(id);
        return ResponseEntity.ok("Category deleted successfully");
    }
}
