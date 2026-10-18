package com.shopease.backend.service;

import com.shopease.backend.model.Product;
import com.shopease.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> getAllProducts() { return repository.findAll(); }

    public Product getProduct(int id) {
        Product product = repository.findById(id);
        if (product == null) throw new RuntimeException("Product not found");
        return product;
    }

    public int createProduct(Product product) {
        validate(product);
        return repository.create(product);
    }

    public void updateProduct(Product product) {
        validate(product);
        if (repository.findById(product.getProductId()) == null)
            throw new RuntimeException("Product not found");
        if (repository.update(product) == 0)
            throw new RuntimeException("Product was not updated");
    }

    public void deleteProduct(int id) {
        if (repository.findById(id) == null)
            throw new RuntimeException("Product not found");
        try {
            if (repository.delete(id) == 0)
                throw new RuntimeException("Product was not deleted");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new RuntimeException("Cannot delete a product already used in an order");
        }
    }

    private void validate(Product product) {
        if (product == null || product.getProductName() == null ||
                product.getProductName().isBlank())
            throw new RuntimeException("Product name is required");
        if (product.getCategoryId() <= 0)
            throw new RuntimeException("Category is required");
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Price must be greater than zero");
        if (product.getStockQuantity() < 0)
            throw new RuntimeException("Stock cannot be negative");
    }
}
