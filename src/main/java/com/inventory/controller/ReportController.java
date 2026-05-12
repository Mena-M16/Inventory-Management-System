package com.inventory.controller;

import com.inventory.dao.ProductDAO;
import com.inventory.dao.TransactionDAO;
import com.inventory.model.Product;
import com.inventory.model.Transaction;
import com.inventory.utils.PermissionChecker;

import java.sql.Timestamp;
import java.util.List;

/**
 * Business logic for report generation.
 */
public class ReportController {

    private final ProductDAO productDAO = new ProductDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final AuthController auth = AuthController.getInstance();

    public List<Product> getInventoryReport() {
        return productDAO.findActive();
    }

    public List<Product> getLowStockReport() {
        return productDAO.findLowStock();
    }

    public List<Transaction> getTransactionReport(Timestamp from, Timestamp to) {
        if (!PermissionChecker.canViewReports(auth.getCurrentUser())) return List.of();
        return transactionDAO.findByDateRange(from, to);
    }

    public List<Transaction> getAllTransactions() {
        if (!PermissionChecker.canViewReports(auth.getCurrentUser())) return List.of();
        return transactionDAO.findAll();
    }

    public List<Object[]> getMonthlySummary() {
        return transactionDAO.getMonthlySummary();
    }
}
