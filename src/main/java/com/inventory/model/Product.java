package com.inventory.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Model class representing an inventory product.
 */
public class Product {
    private int id;
    private String code;
    private String name;
    private String description;
    private int categoryId;
    private String categoryName;
    private int supplierId;
    private String supplierName;
    private int quantity;
    private BigDecimal price;
    private BigDecimal costPrice;
    private int reorderLevel;
    private String location;
    private String barcode;
    private boolean isActive;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Product() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    /** Returns stock status string based on quantity vs reorder level. */
    public String getStockStatus() {
        if (quantity <= 0) return "Out of Stock";
        if (quantity <= reorderLevel) return "Low Stock";
        return "In Stock";
    }

    @Override
    public String toString() { return name + " (" + code + ")"; }
}
