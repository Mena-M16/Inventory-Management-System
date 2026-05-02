package com.inventory.controller;

import com.inventory.dao.UserDAO;
import com.inventory.model.User;
import com.inventory.utils.PermissionChecker;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

/**
 * Business logic for user management (Admin only).
 */
public class UserController {

    private final UserDAO userDAO = new UserDAO();
    private final AuthController auth = AuthController.getInstance();

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public String addUser(User user, String plainPassword) {
        if (!PermissionChecker.canManageUsers(auth.getCurrentUser())) return "Permission denied.";
        String validation = validateUser(user, plainPassword);
        if (validation != null) return validation;

        user.setPassword(BCrypt.hashpw(plainPassword, BCrypt.gensalt(10)));
        user.setActive(true);
        int id = userDAO.insert(user);
        if (id > 0) {
            auth.logAudit(auth.getCurrentUser().getId(), auth.getCurrentUser().getUsername(),
                    "ADD_USER", "Created user: " + user.getUsername() + " role: " + user.getRole());
            return null;
        }
        return "Failed to create user. Username may already exist.";
    }

    public String updateUser(User user) {
        if (!PermissionChecker.canManageUsers(auth.getCurrentUser())) return "Permission denied.";
        if (user.getFullname() == null || user.getFullname().isBlank()) return "Full name is required.";
        boolean ok = userDAO.update(user);
        if (ok) {
            auth.logAudit(auth.getCurrentUser().getId(), auth.getCurrentUser().getUsername(),
                    "UPDATE_USER", "Updated user ID: " + user.getId());
            return null;
        }
        return "Failed to update user.";
    }

    public String resetPassword(int userId, String newPassword) {
        if (!PermissionChecker.canManageUsers(auth.getCurrentUser())) return "Permission denied.";
        if (newPassword == null || newPassword.length() < 6) return "Password must be at least 6 characters.";
        String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt(10));
        boolean ok = userDAO.updatePassword(userId, hashed);
        if (ok) {
            auth.logAudit(auth.getCurrentUser().getId(), auth.getCurrentUser().getUsername(),
                    "RESET_PASSWORD", "Reset password for user ID: " + userId);
            return null;
        }
        return "Failed to reset password.";
    }

    public String deleteUser(int userId) {
        if (!PermissionChecker.canManageUsers(auth.getCurrentUser())) return "Permission denied.";
        if (auth.getCurrentUser().getId() == userId) return "Cannot delete your own account.";
        boolean ok = userDAO.delete(userId);
        if (ok) {
            auth.logAudit(auth.getCurrentUser().getId(), auth.getCurrentUser().getUsername(),
                    "DELETE_USER", "Deactivated user ID: " + userId);
            return null;
        }
        return "Failed to delete user.";
    }

    public int getTotalUsers() { return userDAO.countActive(); }

    private String validateUser(User user, String password) {
        if (user.getUsername() == null || user.getUsername().isBlank()) return "Username is required.";
        if (user.getFullname() == null || user.getFullname().isBlank()) return "Full name is required.";
        if (password == null || password.length() < 6) return "Password must be at least 6 characters.";
        if (user.getRole() == null) return "Role is required.";
        return null;
    }
}
