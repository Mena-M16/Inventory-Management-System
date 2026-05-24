package com.inventory.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Model class representing a sales transaction.
 */
public class Sale {
    private int id;
    private String saleCode;
    private int productId;
    private String productName;
    private String productCode;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private BigDecimal discountPercent;
    private BigDecimal vatPercent;
    private BigDecimal finalAmount;
    private String customerName;
    private int userId;
    private String username;
    private String notes;
    private Timestamp saleDate;

    public Sale() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSaleCode() { return saleCode; }
    public void setSaleCode(String saleCode) { this.saleCode = saleCode; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }

    public BigDecimal getVatPercent() { return vatPercent; }
    public void setVatPercent(BigDecimal vatPercent) { this.vatPercent = vatPercent; }

    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Timestamp getSaleDate() { return saleDate; }
    public void setSaleDate(Timestamp saleDate) { this.saleDate = saleDate; }

    @Override
    public String toString() { return saleCode + " - " + productName; }
}
