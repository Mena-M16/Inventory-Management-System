package com.inventory.utils;

import com.inventory.model.User;

/**
 * Centralised role-based permission checks.
 */
public class PermissionChecker {

    private PermissionChecker() {}

    public static boolean isAdmin(User user) {
        return user != null && "ADMIN".equals(user.getRole());
    }

    public static boolean isManager(User user) {
        return user != null && "MANAGER".equals(user.getRole());
    }

    public static boolean isStaff(User user) {
        return user != null && "STAFF".equals(user.getRole());
    }

    public static boolean isAdminOrManager(User user) {
        return isAdmin(user) || isManager(user);
    }

    public static boolean canManageUsers(User user) { return isAdmin(user); }
    public static boolean canManageProducts(User user) { return isAdminOrManager(user); }
    public static boolean canDeleteProducts(User user) { return isAdminOrManager(user); }
    public static boolean canViewReports(User user) { return isAdminOrManager(user); }
    public static boolean canManageSuppliers(User user) { return isAdminOrManager(user); }
    public static boolean canManageCategories(User user) { return isAdminOrManager(user); }
    public static boolean canAdjustStock(User user) { return isAdminOrManager(user); }
    public static boolean canStockIn(User user) { return user != null; }
    public static boolean canStockOut(User user) { return user != null; }
    public static boolean canViewAuditLogs(User user) { return isAdmin(user); }
    public static boolean canBackupDatabase(User user) { return isAdmin(user); }
    public static boolean canChangeSettings(User user) { return isAdmin(user); }
}
