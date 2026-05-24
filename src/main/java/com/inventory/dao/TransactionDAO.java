package com.inventory.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.inventory.model.Transaction;

/**
 * Data Access Object for Transaction entities.
 */
public class TransactionDAO {

    private static final Logger LOGGER = Logger.getLogger(TransactionDAO.class.getName());
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    private static final String SELECT_BASE =
        "SELECT DISTINCT t.*, p.name AS product_name, p.code AS product_code, u.username " +
        "FROM transactions t " +
        "LEFT JOIN products p ON t.product_id = p.id " +
        "LEFT JOIN users    u ON t.user_id    = u.id ";

    public List<Transaction> findAll() {
        List<Transaction> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = db.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(SELECT_BASE + "ORDER BY t.id ASC");
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findAll transactions failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return list;
    }

    public List<Transaction> findRecent(int limit) {
        List<Transaction> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY t.transaction_date DESC LIMIT ?";
        Connection conn = null;
        try {
            conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findRecent transactions failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return list;
    }

    public List<Transaction> findByDateRange(Timestamp from, Timestamp to) {
        List<Transaction> list = new ArrayList<>();
        String sql = SELECT_BASE + 
            "WHERE DATE(t.transaction_date) >= DATE(?) AND DATE(t.transaction_date) <= DATE(?) " +
            "ORDER BY t.transaction_date ASC";
        Connection conn = null;
        try {
            conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setTimestamp(1, from);
            ps.setTimestamp(2, to);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findByDateRange transactions failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return list;
    }

    public List<Transaction> findByProduct(int productId) {
        List<Transaction> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE t.product_id=? ORDER BY t.transaction_date DESC";
        Connection conn = null;
        try {
            conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findByProduct transactions failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return list;
    }

    public int insert(Transaction t) {
        String sql = "INSERT INTO transactions (transaction_code,type,product_id,quantity,price,total_amount,user_id,notes) VALUES (?,?,?,?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, t.getTransactionCode());
            ps.setString(2, t.getType());
            ps.setInt(3, t.getProductId());
            ps.setInt(4, t.getQuantity());
            ps.setBigDecimal(5, t.getPrice());
            ps.setBigDecimal(6, t.getTotalAmount());
            ps.setInt(7, t.getUserId());
            ps.setString(8, t.getNotes());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "insert transaction failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return -1;
    }

    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM transactions";
        Connection conn = null;
        try {
            conn = db.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "countTotal transactions failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return 0;
    }

    /** Returns monthly transaction counts for the last 6 months (for charts). */
    public List<Object[]> getMonthlySummary() {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(transaction_date,'%b %Y') AS month, COUNT(*) AS cnt " +
                     "FROM transactions WHERE transaction_date >= DATE_SUB(NOW(), INTERVAL 6 MONTH) " +
                     "GROUP BY DATE_FORMAT(transaction_date,'%Y-%m') ORDER BY MIN(transaction_date)";
        Connection conn = null;
        try {
            conn = db.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) result.add(new Object[]{rs.getString("month"), rs.getInt("cnt")});
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "getMonthlySummary failed", e);
        } finally {
            db.releaseConnection(conn);
        }
        return result;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setId(rs.getInt("id"));
        t.setTransactionCode(getStringOrNull(rs, "transaction_code"));
        t.setType(getStringOrNull(rs, "type"));
        t.setProductId(getIntOrZero(rs, "product_id"));
        t.setProductName(getStringOrNull(rs, "product_name"));
        t.setProductCode(getStringOrNull(rs, "product_code"));
        t.setQuantity(getIntOrZero(rs, "quantity"));
        t.setPrice(getBigDecimalOrNull(rs, "price"));
        t.setTotalAmount(getBigDecimalOrNull(rs, "total_amount"));
        t.setUserId(getIntOrZero(rs, "user_id"));
        t.setUsername(getStringOrNull(rs, "username"));
        t.setNotes(getStringOrNull(rs, "notes"));
        t.setTransactionDate(rs.getTimestamp("transaction_date"));
        return t;
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
    
    private BigDecimal getBigDecimalOrNull(ResultSet rs, String column) throws SQLException {
        BigDecimal val = rs.getBigDecimal(column);
        return rs.wasNull() ? null : val;
    }
}
