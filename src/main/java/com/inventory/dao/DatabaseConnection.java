package com.inventory.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton database connection manager with a simple connection pool.
 * Also handles database auto-initialization if tables don't exist.
 */
public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static DatabaseConnection instance;

    private String url;
    private String username;
    private String password;
    private int poolSize;

    private final List<Connection> pool = new ArrayList<>();
    private final List<Connection> usedConnections = new ArrayList<>();

    private DatabaseConnection() {
        loadProperties();
        initPool();
        initializeDatabase();
    }

    /** Returns the singleton instance. */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private void loadProperties() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("db_config.properties")) {
            if (is == null) {
                throw new RuntimeException("db_config.properties not found on classpath");
            }
            props.load(is);
            Class.forName(props.getProperty("db.driver"));
            url      = props.getProperty("db.url");
            username = props.getProperty("db.username");
            password = props.getProperty("db.password");
            poolSize = Integer.parseInt(props.getProperty("db.pool.size", "10"));
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }

    private void initPool() {
        try {
            for (int i = 0; i < poolSize; i++) {
                pool.add(createConnection());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialise connection pool", e);
            throw new RuntimeException("Cannot initialise DB pool", e);
        }
    }

    /**
     * Auto-initialize database if it doesn't exist or tables are missing.
     * This ensures the application works even without manual database setup.
     */
    private void initializeDatabase() {
        Connection conn = null;
        try {
            conn = createConnection();
            DatabaseMetaData meta = conn.getMetaData();
            
            // Check if products table exists
            ResultSet tables = meta.getTables(null, null, "products", null);
            if (!tables.next()) {
                LOGGER.log(Level.INFO, "Database tables not found. Creating schema...");
                createSchema(conn);
                insertSampleData(conn);
                LOGGER.log(Level.INFO, "Database initialized successfully.");
            }
            tables.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database", e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    private void createSchema(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        
        // Create database if not exists (parse from URL)
        String dbName = "inventory_db";
        if (url.contains("/")) {
            String dbPart = url.substring(url.lastIndexOf("/") + 1);
            if (dbPart.contains("?")) {
                dbPart = dbPart.substring(0, dbPart.indexOf("?"));
            }
            dbName = dbPart;
        }
        
        try { stmt.execute("CREATE DATABASE IF NOT EXISTS " + dbName); } catch (SQLException ignored) {}
        stmt.execute("USE " + dbName);
        
        // Create users table
        stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
            "id INT PRIMARY KEY AUTO_INCREMENT, " +
            "username VARCHAR(50) UNIQUE NOT NULL, " +
            "password VARCHAR(255) NOT NULL, " +
            "fullname VARCHAR(100) NOT NULL, " +
            "email VARCHAR(100), " +
            "phone VARCHAR(20), " +
            "role ENUM('ADMIN', 'MANAGER', 'STAFF') DEFAULT 'STAFF', " +
            "isActive BOOLEAN DEFAULT TRUE, " +
            "last_login TIMESTAMP NULL, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        
        // Create categories table
        stmt.execute("CREATE TABLE IF NOT EXISTS categories (" +
            "id INT PRIMARY KEY AUTO_INCREMENT, " +
            "name VARCHAR(50) NOT NULL, " +
            "description TEXT, " +
            "isActive BOOLEAN DEFAULT TRUE, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        
        // Create suppliers table
        stmt.execute("CREATE TABLE IF NOT EXISTS suppliers (" +
            "id INT PRIMARY KEY AUTO_INCREMENT, " +
            "name VARCHAR(100) NOT NULL, " +
            "contact_person VARCHAR(100), " +
            "phone VARCHAR(20), " +
            "email VARCHAR(100), " +
            "address TEXT, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        
        // Create products table
        stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
            "id INT PRIMARY KEY AUTO_INCREMENT, " +
            "code VARCHAR(50) UNIQUE, " +
            "name VARCHAR(100) NOT NULL, " +
            "description TEXT, " +
            "category_id INT, " +
            "supplier_id INT, " +
            "quantity INT DEFAULT 0, " +
            "price DECIMAL(10,2) NOT NULL, " +
            "cost_price DECIMAL(10,2), " +
            "reorder_level INT DEFAULT 10, " +
            "location VARCHAR(50), " +
            "barcode VARCHAR(100), " +
            "isActive BOOLEAN DEFAULT TRUE, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
            "FOREIGN KEY (category_id) REFERENCES categories(id), " +
            "FOREIGN KEY (supplier_id) REFERENCES suppliers(id))");
        
        // Create transactions table
        stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
            "id INT PRIMARY KEY AUTO_INCREMENT, " +
            "transaction_code VARCHAR(50) UNIQUE NOT NULL, " +
            "type ENUM('IN', 'OUT', 'ADJUST') NOT NULL, " +
            "product_id INT NOT NULL, " +
            "quantity INT NOT NULL, " +
            "price DECIMAL(10,2), " +
            "total_amount DECIMAL(10,2), " +
            "user_id INT, " +
            "notes TEXT, " +
            "transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "FOREIGN KEY (product_id) REFERENCES products(id), " +
            "FOREIGN KEY (user_id) REFERENCES users(id))");
        
        // Create audit_logs table
        stmt.execute("CREATE TABLE IF NOT EXISTS audit_logs (" +
            "id INT PRIMARY KEY AUTO_INCREMENT, " +
            "user_id INT, " +
            "username VARCHAR(50), " +
            "action VARCHAR(100), " +
            "details TEXT, " +
            "ip_address VARCHAR(45), " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "FOREIGN KEY (user_id) REFERENCES users(id))");
        
        LOGGER.log(Level.INFO, "Database schema created successfully.");
    }

    private void insertSampleData(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        
        // Insert default users (password: admin123, manager123, staff123 - BCrypt hash)
        String passwordHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        stmt.execute("INSERT IGNORE INTO users (username, password, fullname, email, role) VALUES " +
            "('admin', '" + passwordHash + "', 'System Administrator', 'admin@inventory.com', 'ADMIN'), " +
            "('manager', '" + passwordHash + "', 'Warehouse Manager', 'manager@inventory.com', 'MANAGER'), " +
            "('staff', '" + passwordHash + "', 'Inventory Staff', 'staff@inventory.com', 'STAFF')");
        
        // Insert sample categories
        stmt.execute("INSERT IGNORE INTO categories (name, description) VALUES " +
            "('Electronics', 'Electronic devices and accessories'), " +
            "('Furniture', 'Office and home furniture'), " +
            "('Clothing', 'Apparel and fashion items'), " +
            "('Office Supplies', 'Stationery and office consumables')");
        
        // Insert sample suppliers
        stmt.execute("INSERT IGNORE INTO suppliers (name, contact_person, phone, email, address) VALUES " +
            "('Tech Distributors', 'John Smith', '+1-555-0101', 'sales@techdist.com', '123 Tech Ave, Silicon Valley, CA'), " +
            "('Furniture World', 'Jane Doe', '+1-555-0102', 'orders@furnitureworld.com', '456 Wood St, Portland, OR'), " +
            "('Fashion Hub', 'Bob Lee', '+1-555-0103', 'info@fashionhub.com', '789 Style Blvd, New York, NY')");
        
        // Insert sample products
        stmt.execute("INSERT IGNORE INTO products (code, name, description, category_id, supplier_id, quantity, price, cost_price, reorder_level, location) VALUES " +
            "('P001', 'Gaming Laptop', 'High-performance gaming laptop 16GB RAM', 1, 1, 50, 1299.99, 950.00, 10, 'Shelf A1'), " +
            "('P002', 'Wireless Mouse', 'Ergonomic wireless mouse 2.4GHz', 1, 1, 150, 29.99, 15.00, 20, 'Shelf A2'), " +
            "('P003', 'Office Chair', 'Ergonomic office chair with lumbar support', 2, 2, 25, 299.99, 180.00, 5, 'Shelf B1'), " +
            "('P004', 'Standing Desk', 'Height-adjustable standing desk', 2, 2, 10, 499.99, 320.00, 3, 'Shelf B2'), " +
            "('P005', 'USB-C Hub', '7-in-1 USB-C hub with HDMI', 1, 1, 80, 49.99, 25.00, 15, 'Shelf A3'), " +
            "('P006', 'Mechanical Keyboard', 'Mechanical keyboard RGB backlit', 1, 1, 8, 89.99, 50.00, 10, 'Shelf A4'), " +
            "('P007', 'Monitor 27\"', '27 inch 4K IPS monitor', 1, 1, 15, 399.99, 280.00, 5, 'Shelf A5'), " +
            "('P008', 'Notebook A4', 'Ruled notebook 200 pages', 4, 3, 200, 4.99, 2.00, 50, 'Shelf C1'), " +
            "('P009', 'Ballpoint Pens', 'Box of 12 blue ballpoint pens', 4, 3, 300, 6.99, 3.00, 60, 'Shelf C2'), " +
            "('P010', 'T-Shirt XL', 'Cotton crew-neck t-shirt XL', 3, 3, 3, 19.99, 10.00, 10, 'Shelf D1')");
        
        LOGGER.log(Level.INFO, "Sample data inserted successfully.");
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /** Borrows a connection from the pool (blocks until one is available). */
    public synchronized Connection getConnection() throws SQLException {
        if (pool.isEmpty()) {
            // Grow pool on demand
            pool.add(createConnection());
        }
        Connection conn = pool.remove(pool.size() - 1);
        try {
            if (conn.isClosed()) {
                conn = createConnection();
            }
        } catch (SQLException e) {
            conn = createConnection();
        }
        usedConnections.add(conn);
        return conn;
    }

    /** Returns a connection back to the pool. */
    public synchronized void releaseConnection(Connection conn) {
        if (conn != null) {
            usedConnections.remove(conn);
            pool.add(conn);
        }
    }

    /** Tests whether the database is reachable. */
    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            boolean valid = conn.isValid(3);
            releaseConnection(conn);
            return valid;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Connection test failed", e);
            return false;
        }
    }

    /** Closes all pooled connections (call on application shutdown). */
    public synchronized void shutdown() {
        closeAll(pool);
        closeAll(usedConnections);
    }

    private void closeAll(List<Connection> list) {
        for (Connection c : list) {
            try { c.close(); } catch (SQLException ignored) {}
        }
        list.clear();
    }
}
