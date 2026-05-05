package com.inventory.controller;

import com.inventory.dao.ProductDAO;
import com.inventory.dao.TransactionDAO;
import com.inventory.model.Product;
import com.inventory.model.Transaction;
import com.inventory.model.User;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Business logic for stock IN / OUT / ADJUST operations.
 */
public class StockController {

    private final ProductDAO productDAO = new ProductDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final AuthController auth = AuthController.getInstance();

    /**
     * Processes a stock movement.
     *
     * @param productId target product
     * @param type      "IN", "OUT", or "ADJUST"
     * @param quantity  quantity to move (must be > 0)
     * @param notes     optional notes
     * @return null on success, error message on failure
     */
    public String processStock(int productId, String type, int quantity, String notes) {
        if (quantity <= 0) return "Quantity must be greater than zero.";

        Product product = productDAO.findById(productId);
        if (product == null) return "Product not found.";

        User user = auth.getCurrentUser();
        if (user == null) return "Not authenticated.";

        int newQuantity;
        switch (type) {
            case "IN":
                newQuantity = product.getQuantity() + quantity;
                break;
            case "OUT":
                if (product.getQuantity() < quantity) {
                    return "Insufficient stock. Available: " + product.getQuantity();
                }
                newQuantity = product.getQuantity() - quantity;
                break;
            case "ADJUST":
                newQuantity = quantity; // absolute value
                break;
            default:
                return "Invalid transaction type.";
        }

        // Update product quantity
        boolean updated = productDAO.updateQuantity(productId, newQuantity);
        if (!updated) return "Failed to update stock quantity.";

        // Record transaction
        Transaction tx = new Transaction();
        tx.setTransactionCode(generateCode(type));
        tx.setType(type);
        tx.setProductId(productId);
        tx.setQuantity(quantity);
        tx.setPrice(product.getPrice());
        tx.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        tx.setUserId(user.getId());
        tx.setNotes(notes);

        int txId = transactionDAO.insert(tx);
        if (txId < 0) return "Stock updated but transaction record failed.";

        auth.logAudit(user.getId(), user.getUsername(), "STOCK_" + type,
                "Product: " + product.getName() + ", Qty: " + quantity + ", New total: " + newQuantity);
        return null;
    }

    public List<Transaction> getAllTransactions() {
        return transactionDAO.findAll();
    }

    public List<Transaction> getRecentTransactions(int limit) {
        return transactionDAO.findRecent(limit);
    }

    public List<Transaction> getTransactionsByDateRange(java.sql.Timestamp from, java.sql.Timestamp to) {
        return transactionDAO.findByDateRange(from, to);
    }

    public int getTotalTransactions() {
        return transactionDAO.countTotal();
    }

    public List<Object[]> getMonthlySummary() {
        return transactionDAO.getMonthlySummary();
    }

    private String generateCode(String type) {
        String prefix = type.equals("IN") ? "TXI" : type.equals("OUT") ? "TXO" : "TXA";
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        int rand = new Random().nextInt(9000) + 1000;
        return prefix + "-" + date + "-" + rand;
    }
}
