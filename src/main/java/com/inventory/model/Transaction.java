package com.inventory.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Model class representing a stock transaction (IN / OUT / ADJUST).
 */
public class Transaction {
    private int id;
    private String transactionCode;
    private String type;
    private int productId;
    private String productName;
    private String productCode;
    private int quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private int userId;
    private String username;
    private String notes;
    private Timestamp transactionDate;

    public Transaction() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String transactionCode) { this.transactionCode = transactionCode; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Timestamp getTransactionDate() { return transactionDate; }
    public void setTransactionDate(Timestamp transactionDate) { this.transactionDate = transactionDate; }

    @Override
    public String toString() { return transactionCode + " [" + type + "]"; }
}
