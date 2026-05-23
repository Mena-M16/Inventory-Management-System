package com.inventory.controller;

import com.inventory.dao.ProductDAO;
import com.inventory.dao.SaleDAO;
import com.inventory.model.Product;
import com.inventory.model.Sale;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Business logic for sales operations.
 */
public class SaleController {

    private final SaleDAO saleDAO = new SaleDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final AuthController auth = AuthController.getInstance();

    public List<Sale> getAllSales() {
        return saleDAO.findAll();
    }

    public List<Sale> getSalesByDateRange(Timestamp from, Timestamp to) {
        return saleDAO.findByDateRange(from, to);
    }

    public int getTotalSales() {
        return saleDAO.countTotal();
    }

    public BigDecimal getTotalRevenue() {
        return saleDAO.getTotalRevenue();
    }

    /**
     * Processes a sale with discount and VAT.
     */
    public String processSale(int productId, int quantity, String customerName,
                               BigDecimal discountPercent, BigDecimal vatPercent, String notes) {
        if (quantity <= 0) return "Quantity must be greater than zero.";

        Product product = productDAO.findById(productId);
        if (product == null) return "Product not found.";
        if (product.getQuantity() < quantity) {
            return "Insufficient stock. Available: " + product.getQuantity();
        }

        // Deduct stock
        int newQty = product.getQuantity() - quantity;
        boolean updated = productDAO.updateQuantity(productId, newQty);
        if (!updated) return "Failed to update stock.";

        // Calculate amounts
        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        BigDecimal discountAmt = subtotal.multiply(discountPercent).divide(BigDecimal.valueOf(100));
        BigDecimal afterDiscount = subtotal.subtract(discountAmt);
        BigDecimal vatAmt = afterDiscount.multiply(vatPercent).divide(BigDecimal.valueOf(100));
        BigDecimal finalAmount = afterDiscount.add(vatAmt);

        // Record sale
        Sale sale = new Sale();
        sale.setSaleCode(generateSaleCode());
        sale.setProductId(productId);
        sale.setQuantity(quantity);
        sale.setUnitPrice(product.getPrice());
        sale.setTotalAmount(subtotal);
        sale.setDiscountPercent(discountPercent);
        sale.setVatPercent(vatPercent);
        sale.setFinalAmount(finalAmount);
        sale.setCustomerName(customerName);
        sale.setUserId(auth.getCurrentUser().getId());
        sale.setNotes(notes);

        int id = saleDAO.insert(sale);
        if (id < 0) return "Sale recorded but failed to save record.";

        auth.logAudit(auth.getCurrentUser().getId(), auth.getCurrentUser().getUsername(),
                "SALE", "Sold " + quantity + " x " + product.getName() +
                " Final: $" + String.format("%.2f", finalAmount));
        return null;
    }

    private String generateSaleCode() {
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        int rand = new Random().nextInt(9000) + 1000;
        return "SL-" + date + "-" + rand;
    }
}
