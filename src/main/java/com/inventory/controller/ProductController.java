package com.inventory.controller;

import com.inventory.dao.ProductDAO;
import com.inventory.model.Product;
import com.inventory.model.User;
import com.inventory.utils.PermissionChecker;

import java.math.BigDecimal;
import java.util.List;

/**
 * Business logic for product management.
 */
public class ProductController {

    private final ProductDAO productDAO = new ProductDAO();
    private final AuthController auth = AuthController.getInstance();

    public List<Product> getAllProducts() {
        return productDAO.findAll();
    }

    public List<Product> getActiveProducts() {
        return productDAO.findActive();
    }

    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.isBlank()) return productDAO.findActive();
        return productDAO.search(keyword.trim());
    }

    public List<Product> getLowStockProducts() {
        return productDAO.findLowStock();
    }

    public Product getProductById(int id) {
        return productDAO.findById(id);
    }

    public Product getProductByCode(String code) {
        return productDAO.findByCode(code);
    }

    public String addProduct(Product product) {
        User user = auth.getCurrentUser();
        if (!PermissionChecker.canManageProducts(user) && !PermissionChecker.isStaff(user)) {
            return "Permission denied.";
        }
        String validation = validateProduct(product);
        if (validation != null) return validation;

        int id = productDAO.insert(product);
        if (id > 0) {
            auth.logAudit(user.getId(), user.getUsername(), "ADD_PRODUCT",
                    "Added product: " + product.getName() + " (code: " + product.getCode() + ")");
            return null;
        }
        // Try to give a more specific error
        Product existing = productDAO.findByCode(product.getCode());
        if (existing != null) return "Product code '" + product.getCode() + "' already exists. Use a different code.";
        return "Failed to add product. Please check all fields and try again.";
    }

    public String updateProduct(Product product) {
        User user = auth.getCurrentUser();
        if (!PermissionChecker.canManageProducts(user)) return "Permission denied.";
        String validation = validateProduct(product);
        if (validation != null) return validation;

        boolean ok = productDAO.update(product);
        if (ok) {
            auth.logAudit(user.getId(), user.getUsername(), "UPDATE_PRODUCT",
                    "Updated product ID: " + product.getId());
            return null;
        }
        return "Failed to update product.";
    }

    public String deleteProduct(int productId) {
        User user = auth.getCurrentUser();
        if (!PermissionChecker.canDeleteProducts(user)) return "Permission denied.";
        boolean ok = productDAO.delete(productId);
        if (ok) {
            auth.logAudit(user.getId(), user.getUsername(), "DELETE_PRODUCT",
                    "Deleted product ID: " + productId);
            return null;
        }
        return "Failed to delete product.";
    }

    public int getTotalProducts() { return productDAO.countActive(); }
    public int getOutOfStockCount() { return productDAO.countOutOfStock(); }
    public int getLowStockCount() { return productDAO.countLowStock(); }
    public BigDecimal getTotalInventoryValue() { return productDAO.getTotalInventoryValue(); }

    private String validateProduct(Product p) {
        if (p.getName() == null || p.getName().isBlank()) return "Product name is required.";
        if (p.getCode() == null || p.getCode().isBlank()) return "Product code is required.";
        if (p.getPrice() == null || p.getPrice().compareTo(BigDecimal.ZERO) < 0) return "Price must be >= 0.";
        if (p.getReorderLevel() < 0) return "Reorder level must be >= 0.";
        return null;
    }
}
