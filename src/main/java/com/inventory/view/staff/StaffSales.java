package com.inventory.view.staff;

import com.inventory.controller.AuthController;
import com.inventory.controller.ProductController;
import com.inventory.controller.SaleController;
import com.inventory.model.Product;
import com.inventory.model.Sale;
import com.inventory.utils.NotificationUtils;
import com.inventory.view.components.ThemeUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Staff sales page — record sales at fixed price, no discount or VAT control.
 * Shows only the current staff member's own sales history.
 */
public class StaffSales extends JPanel {

    private final SaleController saleCtrl = new SaleController();
    private final ProductController productCtrl = new ProductController();
    private final AuthController auth = AuthController.getInstance();

    private DefaultTableModel salesHistoryModel;
    private final List<Object[]> cartItems = new ArrayList<>();
    private DefaultTableModel cartModel;
    private JLabel cartTotalLabel;

    private static final String[] HISTORY_COLS = {
        "Sale Code", "Product", "Qty", "Unit Price", "Total", "Customer", "Date"
    };
    private static final String[] CART_COLS = {"Product", "Code", "Qty", "Unit Price", "Line Total"};

    public StaffSales() {
        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeUtil.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        loadMyHistory();
    }

    private void buildUI() {
        // Top bar
        JPanel topBar = new JPanel(new BorderLayout(16, 0));
        topBar.setOpaque(false);

        JLabel titleLbl = new JLabel("Sales");
        titleLbl.setFont(ThemeUtil.FONT_TITLE);
        titleLbl.setForeground(ThemeUtil.TEXT_PRIMARY);
        topBar.add(titleLbl, BorderLayout.WEST);

        JButton newSaleBtn = ThemeUtil.successButton("+ New Sale");
        newSaleBtn.setPreferredSize(new Dimension(120, 40));
        newSaleBtn.addActionListener(e -> showSaleDialog());
        topBar.add(newSaleBtn, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // My sales history
        JPanel histCard = ThemeUtil.createCard();
        histCard.setLayout(new BorderLayout(0, 8));

        JPanel histHeader = new JPanel(new BorderLayout());
        histHeader.setOpaque(false);
        histHeader.add(ThemeUtil.sectionTitle("My Sales History"), BorderLayout.WEST);
        JButton refreshBtn = ThemeUtil.secondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadMyHistory());
        histHeader.add(refreshBtn, BorderLayout.EAST);
        histCard.add(histHeader, BorderLayout.NORTH);

        salesHistoryModel = new DefaultTableModel(HISTORY_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable histTable = new JTable(salesHistoryModel);
        ThemeUtil.styleTable(histTable);
        histCard.add(new JScrollPane(histTable), BorderLayout.CENTER);
        add(histCard, BorderLayout.CENTER);
    }

    private void showSaleDialog() {
        cartItems.clear();

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog overlay = ThemeUtil.showBlurOverlay(parentWindow);

        JDialog dialog = new JDialog(parentWindow, "New Sale", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(720, 560);
        dialog.setLocationRelativeTo(parentWindow);
        dialog.setLayout(new BorderLayout());
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { overlay.dispose(); }
            @Override public void windowClosing(java.awt.event.WindowEvent e) { overlay.dispose(); }
        });

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeUtil.SUCCESS);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel titleLbl = new JLabel("\uD83D\uDED2  New Sale  —  Fixed Price (No Discount)");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(Color.WHITE);
        header.add(titleLbl);
        dialog.add(header, BorderLayout.NORTH);

        // Main split
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        mainPanel.setBorder(new EmptyBorder(16, 16, 8, 16));
        mainPanel.setBackground(ThemeUtil.BG_LIGHT);

        // LEFT: Add item form
        JPanel addItemCard = new JPanel(new GridBagLayout());
        addItemCard.setBackground(Color.WHITE);
        addItemCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeUtil.BORDER_COLOR),
            new EmptyBorder(16, 16, 16, 16)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 4, 8, 4);

        List<Product> products = productCtrl.getActiveProducts();
        JComboBox<Product> productCombo = new JComboBox<>(products.toArray(new Product[0]));
        productCombo.setFont(ThemeUtil.FONT_BODY);

        JLabel stockInfoLbl = new JLabel("Stock: —");
        stockInfoLbl.setFont(ThemeUtil.FONT_BOLD);
        stockInfoLbl.setForeground(ThemeUtil.PRIMARY);

        JTextField qtyField = ThemeUtil.createTextField(8);

        // VAT info label (read-only, set by admin — default 15%)
        JLabel vatInfoLbl = new JLabel("VAT: 15% (set by admin)");
        vatInfoLbl.setFont(ThemeUtil.FONT_SMALL);
        vatInfoLbl.setForeground(ThemeUtil.TEXT_MUTED);

        productCombo.addActionListener(e -> {
            Product p = (Product) productCombo.getSelectedItem();
            if (p != null) {
                stockInfoLbl.setText("Stock: " + p.getQuantity() + "  |  Price: $" + p.getPrice());
                stockInfoLbl.setForeground(ThemeUtil.stockStatusColor(p.getStockStatus()));
            }
        });
        Product initP = (Product) productCombo.getSelectedItem();
        if (initP != null) stockInfoLbl.setText("Stock: " + initP.getQuantity() + "  |  Price: $" + initP.getPrice());

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        addItemCard.add(new JLabel("Product:") {{ setFont(ThemeUtil.FONT_BOLD); }}, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        addItemCard.add(productCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        addItemCard.add(stockInfoLbl, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.35;
        addItemCard.add(new JLabel("Quantity:") {{ setFont(ThemeUtil.FONT_BOLD); }}, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        addItemCard.add(qtyField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        addItemCard.add(vatInfoLbl, gbc);
        gbc.gridwidth = 1;

        JButton addToCartBtn = ThemeUtil.primaryButton("Add to Cart");
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        addItemCard.add(addToCartBtn, gbc);

        mainPanel.add(addItemCard);

        // RIGHT: Cart
        JPanel cartCard = new JPanel(new BorderLayout(0, 8));
        cartCard.setBackground(Color.WHITE);
        cartCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeUtil.BORDER_COLOR),
            new EmptyBorder(12, 12, 12, 12)
        ));
        cartCard.add(new JLabel("Cart") {{ setFont(ThemeUtil.FONT_HEADING); }}, BorderLayout.NORTH);

        cartModel = new DefaultTableModel(CART_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable cartTable = new JTable(cartModel);
        ThemeUtil.styleTable(cartTable);
        cartCard.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel cartBottom = new JPanel(new BorderLayout(8, 0));
        cartBottom.setOpaque(false);
        cartTotalLabel = new JLabel("Total: $0.00");
        cartTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        cartTotalLabel.setForeground(ThemeUtil.SUCCESS);
        JButton removeBtn = ThemeUtil.dangerButton("Remove");
        removeBtn.setPreferredSize(new Dimension(85, 30));
        removeBtn.addActionListener(e -> {
            int row = cartTable.getSelectedRow();
            if (row >= 0) { cartItems.remove(row); refreshCart(); }
        });
        cartBottom.add(cartTotalLabel, BorderLayout.CENTER);
        cartBottom.add(removeBtn, BorderLayout.EAST);
        cartCard.add(cartBottom, BorderLayout.SOUTH);
        mainPanel.add(cartCard);
        dialog.add(mainPanel, BorderLayout.CENTER);

        // Add to cart
        addToCartBtn.addActionListener(e -> {
            Product p = (Product) productCombo.getSelectedItem();
            if (p == null) { NotificationUtils.showError(dialog, "Select a product."); return; }
            int qty;
            try { qty = Integer.parseInt(qtyField.getText().trim()); if (qty <= 0) throw new NumberFormatException(); }
            catch (NumberFormatException ex) { NotificationUtils.showError(dialog, "Enter a valid quantity."); return; }
            if (p.getQuantity() < qty) {
                NotificationUtils.showWarning(dialog, "Insufficient stock! Available: " + p.getQuantity()); return;
            }
            cartItems.add(new Object[]{p, qty});
            refreshCart();
            qtyField.setText("");
        });

        // Footer
        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setBackground(new Color(248, 250, 252));
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeUtil.BORDER_COLOR),
            new EmptyBorder(10, 16, 10, 16)
        ));

        JPanel customerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        customerPanel.setOpaque(false);
        customerPanel.add(new JLabel("Customer:") {{ setFont(ThemeUtil.FONT_BOLD); }});
        JTextField customerField = ThemeUtil.createTextField(18);
        customerField.setText("Walk-in Customer");
        customerField.setPreferredSize(new Dimension(200, 34));
        customerPanel.add(customerField);
        footer.add(customerPanel, BorderLayout.WEST);

        JPanel footerBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footerBtns.setOpaque(false);
        JButton cancelBtn   = ThemeUtil.secondaryButton("Cancel");
        JButton completeBtn = ThemeUtil.successButton("Complete Sale");
        completeBtn.setPreferredSize(new Dimension(150, 36));
        cancelBtn.addActionListener(e -> { overlay.dispose(); dialog.dispose(); });

        completeBtn.addActionListener(e -> {
            if (cartItems.isEmpty()) { NotificationUtils.showError(dialog, "Cart is empty."); return; }
            String customer = customerField.getText().trim();
            if (customer.isBlank()) customer = "Walk-in Customer";
            int successCount = 0;
            for (Object[] item : cartItems) {
                Product p = (Product) item[0];
                int qty   = (int) item[1];
                // Staff: no discount, VAT fixed at 15%
                String err = saleCtrl.processSale(p.getId(), qty, customer,
                        BigDecimal.ZERO, new BigDecimal("15"), "");
                if (err == null) successCount++;
            }
            NotificationUtils.showSuccess(StaffSales.this,
                successCount + " item(s) sold! Total: " + cartTotalLabel.getText());
            overlay.dispose();
            dialog.dispose();
            loadMyHistory();
        });

        footerBtns.add(cancelBtn);
        footerBtns.add(completeBtn);
        footer.add(footerBtns, BorderLayout.EAST);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        double grandTotal = 0;
        for (Object[] item : cartItems) {
            Product p = (Product) item[0];
            int qty   = (int) item[1];
            double sub = p.getPrice().doubleValue() * qty;
            double vat = sub * 0.15;
            double lineTotal = sub + vat;
            grandTotal += lineTotal;
            cartModel.addRow(new Object[]{
                p.getName(), p.getCode(), qty,
                String.format("$%.2f", p.getPrice()),
                String.format("$%.2f", lineTotal)
            });
        }
        cartTotalLabel.setText("Total (incl. 15% VAT): $" + String.format("%.2f", grandTotal));
    }

    private void loadMyHistory() {
        String myUsername = auth.getCurrentUser() != null ? auth.getCurrentUser().getUsername() : "";
        SwingWorker<List<Sale>, Void> worker = new SwingWorker<>() {
            @Override protected List<Sale> doInBackground() { return saleCtrl.getAllSales(); }
            @Override protected void done() {
                try {
                    salesHistoryModel.setRowCount(0);
                    for (Sale s : get()) {
                        // Show only this staff member's sales
                        if (myUsername.equals(s.getUsername())) {
                            salesHistoryModel.addRow(new Object[]{
                                s.getSaleCode(), s.getProductName(), s.getQuantity(),
                                String.format("$%.2f", s.getUnitPrice()),
                                s.getFinalAmount() != null ? String.format("$%.2f", s.getFinalAmount()) : "-",
                                s.getCustomerName(), s.getSaleDate()
                            });
                        }
                    }
                } catch (Exception ex) {
                    NotificationUtils.showError(StaffSales.this, "Failed to load sales.");
                }
            }
        };
        worker.execute();
    }
}
