package com.inventory.controller;

import com.inventory.dao.SupplierDAO;
import com.inventory.model.Supplier;
import com.inventory.utils.PermissionChecker;

import java.util.List;

/**
 * Business logic for supplier management.
 */
public class SupplierController {

    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final AuthController auth = AuthController.getInstance();

    public List<Supplier> getAllSuppliers() { return supplierDAO.findAll(); }

    public String addSupplier(Supplier s) {
        if (!PermissionChecker.canManageSuppliers(auth.getCurrentUser())) return "Permission denied.";
        if (s.getName() == null || s.getName().isBlank()) return "Supplier name is required.";
        int id = supplierDAO.insert(s);
        if (id > 0) {
            auth.logAudit(auth.getCurrentUser().getId(), auth.getCurrentUser().getUsername(),
                    "ADD_SUPPLIER", "Added supplier: " + s.getName());
            return null;
        }
        return "Failed to add supplier.";
    }

    public String updateSupplier(Supplier s) {
        if (!PermissionChecker.canManageSuppliers(auth.getCurrentUser())) return "Permission denied.";
        if (s.getName() == null || s.getName().isBlank()) return "Supplier name is required.";
        boolean ok = supplierDAO.update(s);
        return ok ? null : "Failed to update supplier.";
    }

    public String deleteSupplier(int id) {
        if (!PermissionChecker.canManageSuppliers(auth.getCurrentUser())) return "Permission denied.";
        boolean ok = supplierDAO.delete(id);
        if (ok) {
            auth.logAudit(auth.getCurrentUser().getId(), auth.getCurrentUser().getUsername(),
                    "DELETE_SUPPLIER", "Deleted supplier ID: " + id);
            return null;
        }
        return "Failed to delete supplier.";
    }
}
