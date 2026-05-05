package com.inventory.controller;

import com.inventory.dao.DatabaseConnection;
import com.inventory.dao.UserDAO;
import com.inventory.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles authentication, session management, and login-attempt tracking.
 */
public class AuthController {

    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());
    private static final int MAX_ATTEMPTS = 5;
    private static final long SESSION_TIMEOUT_MS = 30 * 60 * 1000L; // 30 minutes

    private static AuthController instance;
    private final UserDAO userDAO = new UserDAO();

    /** Currently logged-in user. */
    private User currentUser;
    private long lastActivityTime;

    /** Tracks failed login attempts per username. */
    private final Map<String, Integer> failedAttempts = new HashMap<>();

    private AuthController() {}

    public static synchronized AuthController getInstance() {
        if (instance == null) instance = new AuthController();
        return instance;
    }

    /**
     * Attempts to log in with the given credentials.
     *
     * @return null on success, or an error message string on failure.
     */
    public String login(String username, String plainPassword) {
        if (username == null || username.isBlank()) return "Username is required.";
        if (plainPassword == null || plainPassword.isBlank()) return "Password is required.";

        int attempts = failedAttempts.getOrDefault(username, 0);
        if (attempts >= MAX_ATTEMPTS) {
            return "Account locked after " + MAX_ATTEMPTS + " failed attempts. Contact administrator.";
        }

        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            incrementFailedAttempts(username);
            return "Invalid username or password.";
        }

        boolean passwordMatch;
        try {
            passwordMatch = BCrypt.checkpw(plainPassword, user.getPassword());
        } catch (Exception e) {
            // Fallback for plain-text passwords in dev/test environments
            passwordMatch = plainPassword.equals(user.getPassword());
        }

        if (!passwordMatch) {
            incrementFailedAttempts(username);
            int remaining = MAX_ATTEMPTS - failedAttempts.getOrDefault(username, 0);
            return "Invalid username or password. " + remaining + " attempt(s) remaining.";
        }

        // Successful login
        failedAttempts.remove(username);
        currentUser = user;
        lastActivityTime = System.currentTimeMillis();
        userDAO.updateLastLogin(user.getId());
        logAudit(user.getId(), user.getUsername(), "LOGIN", "User logged in successfully");
        return null;
    }

    /** Logs out the current user. */
    public void logout() {
        if (currentUser != null) {
            logAudit(currentUser.getId(), currentUser.getUsername(), "LOGOUT", "User logged out");
        }
        currentUser = null;
        lastActivityTime = 0;
    }

    /** Returns the currently logged-in user, or null if not logged in. */
    public User getCurrentUser() { return currentUser; }

    /** Updates the last-activity timestamp (call on any user interaction). */
    public void refreshSession() { lastActivityTime = System.currentTimeMillis(); }

    /** Returns true if the session has timed out. */
    public boolean isSessionExpired() {
        if (currentUser == null) return true;
        return (System.currentTimeMillis() - lastActivityTime) > SESSION_TIMEOUT_MS;
    }

    private void incrementFailedAttempts(String username) {
        failedAttempts.merge(username, 1, Integer::sum);
    }

    /** Writes an entry to the audit_logs table. */
    public void logAudit(int userId, String username, String action, String details) {
        String sql = "INSERT INTO audit_logs (user_id, username, action, details, ip_address) VALUES (?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setString(2, username);
            ps.setString(3, action);
            ps.setString(4, details);
            ps.setString(5, "127.0.0.1");
            ps.executeUpdate();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Audit log insert failed", e);
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }
}
