package com.inventory.controller;

import com.inventory.dao.CategoryDAO;
import com.inventory.model.Category;
import com.inventory.utils.PermissionChecker;

import java.util.List;

/**
 * Business logic for category management.
 */
public class CategoryController {

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final AuthController auth = AuthController.getInstance();

    public List<Category> getAllCategories() { return categoryDAO.findAll(); }
    public List<Category> getActiveCategories() { return categoryDAO.findActive(); }

    public String addCategory(Category cat) {
        if (!PermissionChecker.canManageCategories(auth.getCurrentUser())) return "Permission denied.";
        if (cat.getName() == null || cat.getName().isBlank()) return "Category name is required.";
        cat.setActive(true);
        int id = categoryDAO.insert(cat);
        if (id > 0) {
            auth.logAudit(auth.getCurrentUser().getId(), auth.getCurrentUser().getUsername(),
                    "ADD_CATEGORY", "Added category: " + cat.getName());
            return null;
        }
        return "Failed to add category.";
    }

    public String updateCategory(Category cat) {
        if (!PermissionChecker.canManageCategories(auth.getCurrentUser())) return "Permission denied.";
        if (cat.getName() == null || cat.getName().isBlank()) return "Category name is required.";
        boolean ok = categoryDAO.update(cat);
        return ok ? null : "Failed to update category.";
    }

    public String deleteCategory(int id) {
        if (!PermissionChecker.canManageCategories(auth.getCurrentUser())) return "Permission denied.";
        boolean ok = categoryDAO.delete(id);
        if (ok) {
            auth.logAudit(auth.getCurrentUser().getId(), auth.getCurrentUser().getUsername(),
                    "DELETE_CATEGORY", "Deleted category ID: " + id);
            return null;
        }
        return "Failed to delete category.";
    }
}
