package com.inventory.dao;

import com.inventory.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Category entities.
 */
public class CategoryDAO {

    private static final Logger LOGGER = Logger.getLogger(CategoryDAO.class.getName());
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT DISTINCT * FROM categories ORDER BY id ASC";
        Connection conn = null;
        try {
            conn = db.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findAll categories failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return list;
    }

    public List<Category> findActive() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT DISTINCT * FROM categories WHERE isActive=TRUE ORDER BY id ASC";
        Connection conn = null;
        try {
            conn = db.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findActive categories failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return list;
    }

    public int insert(Category cat) {
        String sql = "INSERT INTO categories (name, description, isActive) VALUES (?,?,?)";
        Connection conn = null;
        try {
            conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, cat.getName());
            ps.setString(2, cat.getDescription());
            ps.setBoolean(3, cat.isActive());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "insert category failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return -1;
    }

    public boolean update(Category cat) {
        String sql = "UPDATE categories SET name=?, description=?, isActive=? WHERE id=?";
        Connection conn = null;
        try {
            conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, cat.getName());
            ps.setString(2, cat.getDescription());
            ps.setBoolean(3, cat.isActive());
            ps.setInt(4, cat.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "update category failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "UPDATE categories SET isActive=FALSE WHERE id=?";
        Connection conn = null;
        try {
            conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "delete category failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return false;
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        return new Category(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getBoolean("isActive"),
            rs.getTimestamp("created_at")
        );
    }
}
