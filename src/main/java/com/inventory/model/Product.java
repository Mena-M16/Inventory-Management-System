package com.inventory.model;

import java.time.LocalDateTime;

public class Product {
    private int id;
    private String code;
    private String name;
    private String description;
    private int categoryId;
    private int supplierId;
    private int quantity;
    private double price;
    private double costPrice;
    private int reorderLevel;
    private int reorderQuantity;
    private String location;
    private String barcode;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructor
    public Product(String name, int quantity, double price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.isActive = true;
        this.reorderLevel = 10;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Default constructor
    public Product() {
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters
    public int getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getCategoryId() { return categoryId; }
    public int getSupplierId() { return supplierId; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public double getCostPrice() { return costPrice; }
    public int getReorderLevel() { return reorderLevel; }
    public int getReorderQuantity() { return reorderQuantity; }
    public String getLocation() { return location; }
    public String getBarcode() { return barcode; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    // Setters
    public void setId(int id) { this.id = id; }
    public void setCode(String code) { this.code = code; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setPrice(double price) { this.price = price; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }
    public void setReorderQuantity(int reorderQuantity) { this.reorderQuantity = reorderQuantity; }
    public void setLocation(String location) { this.location = location; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public void setActive(boolean active) { isActive = active; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Business methods
    public boolean isLowStock() {
        return quantity <= reorderLevel;
    }
    
    public boolean isOutOfStock() {
        return quantity <= 0;
    }
    
    public double getTotalValue() {
        return quantity * price;
    }
    
    public double getProfitMargin() {
        if (costPrice > 0) {
            return ((price - costPrice) / costPrice) * 100;
        }
        return 0;
    }
    
    @Override
    public String toString() {
        return String.format("%s - %s (Qty: %d, Price: $%.2f)", 
                           code != null ? code : "N/A", name, quantity, price);
    }
}