package com.inventory.dao;

import com.inventory.model.Sale;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Sale entities.
 */
public class SaleDAO {

    private static final Logger LOGGER = Logger.getLogger(SaleDAO.class.getName());
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    private static final String SELECT_BASE =
        "SELECT s.*, p.name AS product_name, p.code AS product_code, u.username " +
        "FROM sales s " +
        "LEFT JOIN products p ON s.product_id = p.id " +
        "LEFT JOIN users    u ON s.user_id    = u.id ";

    public List<Sale> findAll() {
        List<Sale> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = db.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(SELECT_BASE + "ORDER BY s.id ASC");
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findAll sales failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return list;
    }

    public List<Sale> findByDateRange(Timestamp from, Timestamp to) {
        List<Sale> list = new ArrayList<>();
        String sql = SELECT_BASE +
            "WHERE DATE(s.sale_date) >= DATE(?) AND DATE(s.sale_date) <= DATE(?) " +
            "ORDER BY s.sale_date ASC";
        Connection conn = null;
        try {
            conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setTimestamp(1, from);
            ps.setTimestamp(2, to);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findByDateRange sales failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return list;
    }

    public int insert(Sale sale) {
        String sql = "INSERT INTO sales (sale_code, product_id, quantity, unit_price, total_amount, discount_percent, vat_percent, final_amount, customer_name, user_id, notes) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, sale.getSaleCode());
            ps.setInt(2, sale.getProductId());
            ps.setInt(3, sale.getQuantity());
            ps.setBigDecimal(4, sale.getUnitPrice());
            ps.setBigDecimal(5, sale.getTotalAmount());
            ps.setBigDecimal(6, sale.getDiscountPercent() != null ? sale.getDiscountPercent() : BigDecimal.ZERO);
            ps.setBigDecimal(7, sale.getVatPercent() != null ? sale.getVatPercent() : BigDecimal.ZERO);
            ps.setBigDecimal(8, sale.getFinalAmount());
            ps.setString(9, sale.getCustomerName());
            ps.setInt(10, sale.getUserId());
            ps.setString(11, sale.getNotes());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "insert sale failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return -1;
    }

    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM sales";
        Connection conn = null;
        try {
            conn = db.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "countTotal sales failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return 0;
    }

    public BigDecimal getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM sales";
        Connection conn = null;
        try {
            conn = db.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "getTotalRevenue failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return BigDecimal.ZERO;
    }

    private Sale mapRow(ResultSet rs) throws SQLException {
        Sale s = new Sale();
        s.setId(rs.getInt("id"));
        s.setSaleCode(rs.getString("sale_code"));
        s.setProductId(rs.getInt("product_id"));
        s.setProductName(rs.getString("product_name"));
        s.setProductCode(rs.getString("product_code"));
        s.setQuantity(rs.getInt("quantity"));
        s.setUnitPrice(rs.getBigDecimal("unit_price"));
        s.setTotalAmount(rs.getBigDecimal("total_amount"));
        try { s.setDiscountPercent(rs.getBigDecimal("discount_percent")); } catch (Exception ignored) {}
        try { s.setVatPercent(rs.getBigDecimal("vat_percent")); } catch (Exception ignored) {}
        try { s.setFinalAmount(rs.getBigDecimal("final_amount")); } catch (Exception ignored) {}
        s.setCustomerName(rs.getString("customer_name"));
        s.setUserId(rs.getInt("user_id"));
        s.setUsername(rs.getString("username"));
        s.setNotes(rs.getString("notes"));
        s.setSaleDate(rs.getTimestamp("sale_date"));
        return s;
    }
}
