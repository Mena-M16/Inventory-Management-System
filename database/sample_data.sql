USE inventory_db;

-- Sample Users (passwords are BCrypt hashed; plain: admin123, manager123, staff123)
INSERT INTO users (username, password, fullname, email, role) VALUES
('admin',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'System Administrator', 'admin@inventory.com',   'ADMIN'),
('manager', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Warehouse Manager',    'manager@inventory.com', 'MANAGER'),
('staff',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Inventory Staff',      'staff@inventory.com',   'STAFF');

-- Sample Categories
INSERT INTO categories (name, description) VALUES
('Electronics',  'Electronic devices and accessories'),
('Furniture',    'Office and home furniture'),
('Clothing',     'Apparel and fashion items'),
('Office Supplies', 'Stationery and office consumables');

-- Sample Suppliers
INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES
('Tech Distributors', 'John Smith', '+1-555-0101', 'sales@techdist.com',       '123 Tech Ave, Silicon Valley, CA'),
('Furniture World',   'Jane Doe',   '+1-555-0102', 'orders@furnitureworld.com', '456 Wood St, Portland, OR'),
('Fashion Hub',       'Bob Lee',    '+1-555-0103', 'info@fashionhub.com',       '789 Style Blvd, New York, NY');

-- Sample Products
INSERT INTO products (code, name, description, category_id, supplier_id, quantity, price, cost_price, reorder_level, location) VALUES
('P001', 'Gaming Laptop',    'High-performance gaming laptop 16GB RAM',  1, 1,  50, 1299.99,  950.00, 10, 'Shelf A1'),
('P002', 'Wireless Mouse',   'Ergonomic wireless mouse 2.4GHz',          1, 1, 150,   29.99,   15.00, 20, 'Shelf A2'),
('P003', 'Office Chair',     'Ergonomic office chair with lumbar support',2, 2,  25,  299.99,  180.00,  5, 'Shelf B1'),
('P004', 'Standing Desk',    'Height-adjustable standing desk',           2, 2,  10,  499.99,  320.00,  3, 'Shelf B2'),
('P005', 'USB-C Hub',        '7-in-1 USB-C hub with HDMI',               1, 1,  80,   49.99,   25.00, 15, 'Shelf A3'),
('P006', 'Mechanical Keyboard','Mechanical keyboard RGB backlit',         1, 1,   8,   89.99,   50.00, 10, 'Shelf A4'),
('P007', 'Monitor 27"',      '27 inch 4K IPS monitor',                   1, 1,  15,  399.99,  280.00,  5, 'Shelf A5'),
('P008', 'Notebook A4',      'Ruled notebook 200 pages',                 4, 3, 200,    4.99,    2.00, 50, 'Shelf C1'),
('P009', 'Ballpoint Pens',   'Box of 12 blue ballpoint pens',            4, 3, 300,    6.99,    3.00, 60, 'Shelf C2'),
('P010', 'T-Shirt XL',       'Cotton crew-neck t-shirt XL',              3, 3,   3,   19.99,   10.00, 10, 'Shelf D1');


-- Additional sample products
INSERT INTO products (code, name, description, category_id, supplier_id, quantity, price, cost_price, reorder_level, location) VALUES
('P011', 'Desk Lamp',      'LED desk lamp with USB charging port', 1, 1, 45,  39.99,  20.00, 10, 'Shelf A6'),
('P012', 'Filing Cabinet', '3-drawer steel filing cabinet',        2, 2,  8, 149.99,  90.00,  3, 'Shelf B3'),
('P013', 'Printer Paper',  'A4 80gsm printer paper 500 sheets',    4, 3, 500,  8.99,   4.00, 100, 'Shelf C3'),
('P014', 'Webcam HD',      '1080p HD webcam with microphone',      1, 1,  30,  59.99,  35.00, 10, 'Shelf A7'),
('P015', 'Office Desk',    'L-shaped corner office desk',          2, 2,   5, 349.99, 220.00,  2, 'Shelf B4');

-- Reset ID sequences to be clean and sequential (no gaps)

SET FOREIGN_KEY_CHECKS = 0;

-- Reset users IDs
SET @count = 0;
UPDATE users SET id = (@count := @count + 1) ORDER BY id ASC;
ALTER TABLE users AUTO_INCREMENT = 1;

-- Reset categories IDs
SET @count = 0;
UPDATE categories SET id = (@count := @count + 1) ORDER BY id ASC;
ALTER TABLE categories AUTO_INCREMENT = 1;

-- Reset suppliers IDs
SET @count = 0;
UPDATE suppliers SET id = (@count := @count + 1) ORDER BY id ASC;
ALTER TABLE suppliers AUTO_INCREMENT = 1;

-- Reset products IDs
SET @count = 0;
UPDATE products SET id = (@count := @count + 1) ORDER BY id ASC;
ALTER TABLE products AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;
