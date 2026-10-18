package com.shopease.backend.model;

import java.math.BigDecimal;

public class Product {
    private int productId;
    private int categoryId;
    private String categoryName;
    private String productName;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    private String imageUrl;

    public Product() {}

    public Product(int productId, int categoryId, String categoryName, String productName,
                    String description, BigDecimal price, int stockQuantity, String imageUrl) {
        this.productId = productId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getProductName() { return productName; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
    public String getImageUrl() { return imageUrl; }
}
