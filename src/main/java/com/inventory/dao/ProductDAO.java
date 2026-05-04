package com.inventory.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.inventory.model.Product;

/**
 * Data Access Object for Product entities.
 */
public class ProductDAO {

    private static final Logger LOGGER = Logger.getLogger(ProductDAO.class.getName());
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    private static final String SELECT_BASE =
        "SELECT DISTINCT p.*, c.name AS category_name, s.name AS supplier_name " +
        "FROM products p " +
        "LEFT JOIN categories c ON p.category_id = c.id " +
        "LEFT JOIN suppliers  s ON p.supplier_id  = s.id ";

    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = db.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(SELECT_BASE + "ORDER BY p.id ASC");
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findAll products failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return list;
    }

    public List<Product> findActive() {
        List<Product> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = db.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(
                SELECT_BASE + "WHERE (p.isActive=TRUE OR p.isActive IS NULL) ORDER BY p.id ASC");
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findActive products failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return list;
    }

    public List<Product> search(String keyword) {
        List<Product> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE (p.isActive=TRUE OR p.isActive IS NULL) AND (p.name LIKE ? OR p.code LIKE ? OR c.name LIKE ?) ORDER BY p.name";
        Connection conn = null;
        try {
            conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            String kw = "%" + keyword + "%";
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "search products failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return list;
    }

    public List<Product> findLowStock() {
        List<Product> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE (p.isActive=TRUE OR p.isActive IS NULL) AND p.quantity <= p.reorder_level ORDER BY p.quantity";
        Connection conn = null;
        try {
            conn = db.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findLowStock failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return list;
    }

    public Product findById(int id) {
        String sql = SELECT_BASE + "WHERE p.id=?";
        Connection conn = null;
        try {
            conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findById product failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return null;
    }

    public Product findByCode(String code) {
        String sql = SELECT_BASE + "WHERE p.code=?";
        Connection conn = null;
        try {
            conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "findByCode product failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return null;
    }

    public int insert(Product p) {
        String sql = "INSERT INTO products (code,name,description,category_id,supplier_id,quantity,price,cost_price,reorder_level,location,barcode,isActive) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, p.getCode());
            ps.setString(2, p.getName());
            ps.setString(3, p.getDescription());
            ps.setInt(4, p.getCategoryId());
            ps.setInt(5, p.getSupplierId());
            ps.setInt(6, p.getQuantity());
            ps.setBigDecimal(7, p.getPrice());
            ps.setBigDecimal(8, p.getCostPrice());
            ps.setInt(9, p.getReorderLevel());
            ps.setString(10, p.getLocation());
            ps.setString(11, p.getBarcode());
            ps.setBoolean(12, p.isActive());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "insert product failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return -1;
    }

    public boolean update(Product p) {
        String sql = "UPDATE products SET code=?,name=?,description=?,category_id=?,supplier_id=?,price=?,cost_price=?,reorder_level=?,location=?,barcode=?,isActive=? WHERE id=?";
        Connection conn = null;
        try {
            conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, p.getCode());
            ps.setString(2, p.getName());
            ps.setString(3, p.getDescription());
            ps.setInt(4, p.getCategoryId());
            ps.setInt(5, p.getSupplierId());
            ps.setBigDecimal(6, p.getPrice());
            ps.setBigDecimal(7, p.getCostPrice());
            ps.setInt(8, p.getReorderLevel());
            ps.setString(9, p.getLocation());
            ps.setString(10, p.getBarcode());
            ps.setBoolean(11, p.isActive());
            ps.setInt(12, p.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "update product failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return false;
    }

    public boolean updateQuantity(int productId, int newQuantity) {
        String sql = "UPDATE products SET quantity=? WHERE id=?";
        Connection conn = null;
        try {
            conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, newQuantity);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "updateQuantity failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "UPDATE products SET isActive=FALSE WHERE id=?";
        Connection conn = null;
        try {
            conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "delete product failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return false;
    }

    public int countActive() {
        String sql = "SELECT COUNT(*) FROM products WHERE isActive=TRUE OR isActive IS NULL";
        Connection conn = null;
        try {
            conn = db.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "countActive products failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return 0;
    }

    public int countOutOfStock() {
        String sql = "SELECT COUNT(*) FROM products WHERE (isActive=TRUE OR isActive IS NULL) AND quantity=0";
        Connection conn = null;
        try {
            conn = db.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "countOutOfStock failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return 0;
    }

    public int countLowStock() {
        String sql = "SELECT COUNT(*) FROM products WHERE (isActive=TRUE OR isActive IS NULL) AND quantity > 0 AND quantity <= reorder_level";
        Connection conn = null;
        try {
            conn = db.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "countLowStock failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return 0;
    }

    public BigDecimal getTotalInventoryValue() {
        String sql = "SELECT COALESCE(SUM(quantity * price), 0) FROM products WHERE isActive=TRUE OR isActive IS NULL";
        Connection conn = null;
        try {
            conn = db.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "getTotalInventoryValue failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return BigDecimal.ZERO;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setCode(getStringOrNull(rs, "code"));
        p.setName(getStringOrNull(rs, "name"));
        p.setDescription(getStringOrNull(rs, "description"));
        p.setCategoryId(getIntOrZero(rs, "category_id"));
        p.setCategoryName(getStringOrNull(rs, "category_name"));
        p.setSupplierId(getIntOrZero(rs, "supplier_id"));
        p.setSupplierName(getStringOrNull(rs, "supplier_name"));
        p.setQuantity(getIntOrZero(rs, "quantity"));
        p.setPrice(getBigDecimalOrNull(rs, "price"));
        p.setCostPrice(getBigDecimalOrNull(rs, "cost_price"));
        p.setReorderLevel(getIntOrDefault(rs, "reorder_level", 10));
        p.setLocation(getStringOrNull(rs, "location"));
        p.setBarcode(getStringOrNull(rs, "barcode"));
        p.setActive(rs.getBoolean("isActive"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setUpdatedAt(rs.getTimestamp("updated_at"));
        return p;
    }
    
    // Helper methods to handle null values safely
    private String getStringOrNull(ResultSet rs, String column) throws SQLException {
        String val = rs.getString(column);
        return rs.wasNull() ? null : val;
    }
    
    private int getIntOrZero(ResultSet rs, String column) throws SQLException {
        int val = rs.getInt(column);
        return rs.wasNull() ? 0 : val;
    }
    
    private int getIntOrDefault(ResultSet rs, String column, int defaultVal) throws SQLException {
        int val = rs.getInt(column);
        return rs.wasNull() ? defaultVal : val;
    }
    
    private BigDecimal getBigDecimalOrNull(ResultSet rs, String column) throws SQLException {
        BigDecimal val = rs.getBigDecimal(column);
        return rs.wasNull() ? null : val;
    }
}
