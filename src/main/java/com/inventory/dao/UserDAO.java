package com.inventory.dao;

import com.inventory.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for User entities.
 */
public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    /** Finds a user by username (used during login). */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ? AND isActive = TRUE";
        Connection conn = null;
        try {
            conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findByUsername failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return null;
    }

    /** Returns all users. */
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT DISTINCT * FROM users ORDER BY id ASC";
        Connection conn = null;
        try {
            conn = db.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findAll users failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return list;
    }

    /** Inserts a new user and returns the generated id. */
    public int insert(User user) {
        String sql = "INSERT INTO users (username, password, fullname, email, phone, role, isActive) VALUES (?,?,?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullname());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getRole());
            ps.setBoolean(7, user.isActive());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "insert user failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return -1;
    }

    /** Updates an existing user. */
    public boolean update(User user) {
        String sql = "UPDATE users SET username=?, fullname=?, email=?, phone=?, role=?, isActive=? WHERE id=?";
        Connection conn = null;
        try {
            conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getFullname());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getRole());
            ps.setBoolean(6, user.isActive());
            ps.setInt(7, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "update user failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return false;
    }

    /** Updates the password for a user. */
    public boolean updatePassword(int userId, String hashedPassword) {
        String sql = "UPDATE users SET password=? WHERE id=?";
        Connection conn = null;
        try {
            conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "updatePassword failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return false;
    }

    /** Soft-deletes a user by setting isActive = false. */
    public boolean delete(int userId) {
        String sql = "UPDATE users SET isActive=FALSE WHERE id=?";
        Connection conn = null;
        try {
            conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "delete user failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return false;
    }

    /** Updates the last_login timestamp. */
    public void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login=NOW() WHERE id=?";
        Connection conn = null;
        try {
            conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "updateLastLogin failed", e);
        } finally {
            db.releaseConnection(conn);
        }
    }

    /** Returns total active user count. */
    public int countActive() {
        String sql = "SELECT COUNT(*) FROM users WHERE isActive=TRUE";
        Connection conn = null;
        try {
            conn = db.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "countActive users failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return 0;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setFullname(rs.getString("fullname"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setRole(rs.getString("role"));
        u.setActive(rs.getBoolean("isActive"));
        u.setLastLogin(rs.getTimestamp("last_login"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}
