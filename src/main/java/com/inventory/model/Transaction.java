package com.inventory.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private int id;
    private String transactionCode;
    private String type; // STOCK_IN, STOCK_OUT, ADJUSTMENT
    private int productId;
    private int quantity;
    private double price;
    private double totalAmount;
    private int userId;
    private String notes;
    private String status;
    private LocalDateTime transactionDate;
    private LocalDateTime createdAt;
    
    // Constructors
    public Transaction() {
        this.status = "COMPLETED";
        this.transactionDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }
    
    public Transaction(String type, int productId, int quantity, double price, int userId) {
        this();
        this.type = type;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.userId = userId;
        this.totalAmount = quantity * price;
        this.transactionCode = generateTransactionCode();
    }
    
    private String generateTransactionCode() {
        return "TRX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
    
    // Getters
    public int getId() { return id; }
    public String getTransactionCode() { return transactionCode; }
    public String getType() { return type; }
    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public double getTotalAmount() { return totalAmount; }
    public int getUserId() { return userId; }
    public String getNotes() { return notes; }
    public String getStatus() { return status; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    // Setters
    public void setId(int id) { this.id = id; }
    public void setTransactionCode(String transactionCode) { this.transactionCode = transactionCode; }
    public void setType(String type) { this.type = type; }
    public void setProductId(int productId) { this.productId = productId; }
    public void setQuantity(int quantity) { 
        this.quantity = quantity;
        this.totalAmount = this.quantity * this.price;
    }
    public void setPrice(double price) {
        this.price = price;
        this.totalAmount = this.quantity * this.price;
    }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setStatus(String status) { this.status = status; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return String.format("%s - %s: %d units @ $%.2f", 
                           transactionCode, type, quantity, price);
    }
}