package com.inventory.view.staff;

import com.inventory.controller.ProductController;
import com.inventory.controller.StockController;
import com.inventory.model.Product;
import com.inventory.utils.NotificationUtils;
import com.inventory.view.components.ThemeUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Staff stock panel — Stock IN and Stock OUT only with improved UI.
 */
public class StaffStock extends JPanel {

    private final StockController stockCtrl = new StockController();
    private final ProductController productCtrl = new ProductController();

    private JComboBox<Product> productCombo;
    private JComboBox<String> typeCombo;
    private JTextField qtyField;
    private JTextArea notesArea;
    private JLabel currentQtyLabel;
    private JLabel statusLabel;
    private JLabel productInfoLabel;

    public StaffStock() {
        setLayout(new BorderLayout());
        setBackground(ThemeUtil.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        buildUI();
    }

    private void buildUI() {
        // Page title
        JLabel pageTitle = new JLabel("Stock Management");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        pageTitle.setForeground(ThemeUtil.TEXT_PRIMARY);
        pageTitle.setBorder(new EmptyBorder(0, 0, 16, 0));
        add(pageTitle, BorderLayout.NORTH);

        // Main content - two columns
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        mainPanel.setOpaque(false);

        // LEFT: Stock form card
        JPanel formCard = new JPanel(new BorderLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeUtil.BORDER_COLOR, 1),
            new EmptyBorder(24, 24, 24, 24)
        ));

        // Form title
        JLabel formTitle = new JLabel("Apply Stock Movement");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitle.setForeground(ThemeUtil.PRIMARY);
        formTitle.setBorder(new EmptyBorder(0, 0, 20, 0));
        formCard.add(formTitle, BorderLayout.NORTH);

        // Form fields
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        // Product label
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel productLbl = new JLabel("Select Product");
        productLbl.setFont(ThemeUtil.FONT_BOLD);
        fieldsPanel.add(productLbl, gbc);

        // Product combo
        gbc.gridy = 1;
        List<Product> products = productCtrl.getActiveProducts();
        productCombo = new JComboBox<>(products.toArray(new Product[0]));
        productCombo.setFont(ThemeUtil.FONT_BODY);
        productCombo.setPreferredSize(new Dimension(0, 38));
        productCombo.addActionListener(e -> updateProductInfo());
        fieldsPanel.add(productCombo, gbc);

        // Product info card
        gbc.gridy = 2;
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        infoPanel.setBackground(new Color(232, 245, 253));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(187, 222, 251), 1),
            new EmptyBorder(10, 14, 10, 14)
        ));
        currentQtyLabel = new JLabel("Current Stock: —");
        currentQtyLabel.setFont(ThemeUtil.FONT_BOLD);
        statusLabel = new JLabel("Status: —");
        statusLabel.setFont(ThemeUtil.FONT_BODY);
        infoPanel.add(currentQtyLabel);
        infoPanel.add(statusLabel);
        fieldsPanel.add(infoPanel, gbc);

        // Type label
        gbc.gridy = 3; gbc.insets = new Insets(16, 0, 8, 0);
        JLabel typeLbl = new JLabel("Transaction Type");
        typeLbl.setFont(ThemeUtil.FONT_BOLD);
        fieldsPanel.add(typeLbl, gbc);

        // Type combo with descriptions
        gbc.gridy = 4; gbc.insets = new Insets(0, 0, 8, 0);
        typeCombo = new JComboBox<>(new String[]{"IN  —  Receive stock from supplier", "OUT  —  Remove stock from warehouse"});
        typeCombo.setFont(ThemeUtil.FONT_BODY);
        typeCombo.setPreferredSize(new Dimension(0, 38));
        fieldsPanel.add(typeCombo, gbc);

        // Quantity label
        gbc.gridy = 5; gbc.insets = new Insets(16, 0, 8, 0);
        JLabel qtyLbl = new JLabel("Quantity");
        qtyLbl.setFont(ThemeUtil.FONT_BOLD);
        fieldsPanel.add(qtyLbl, gbc);

        // Quantity field
        gbc.gridy = 6; gbc.insets = new Insets(0, 0, 8, 0);
        qtyField = ThemeUtil.createTextField(10);
        qtyField.setPreferredSize(new Dimension(0, 38));
        qtyField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        fieldsPanel.add(qtyField, gbc);

        // Notes label
        gbc.gridy = 7; gbc.insets = new Insets(16, 0, 8, 0);
        JLabel notesLbl = new JLabel("Notes (optional)");
        notesLbl.setFont(ThemeUtil.FONT_BOLD);
        fieldsPanel.add(notesLbl, gbc);

        // Notes area
        gbc.gridy = 8; gbc.insets = new Insets(0, 0, 0, 0);
        notesArea = new JTextArea(3, 10);
        notesArea.setFont(ThemeUtil.FONT_BODY);
        notesArea.setLineWrap(true);
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeUtil.BORDER_COLOR),
            new EmptyBorder(6, 8, 6, 8)
        ));
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(0, 80));
        fieldsPanel.add(notesScroll, gbc);

        formCard.add(fieldsPanel, BorderLayout.CENTER);

        // Apply button - normal size, centered
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        btnPanel.setOpaque(false);
        JButton applyBtn = ThemeUtil.primaryButton("Apply Stock Movement");
        applyBtn.setPreferredSize(new Dimension(200, 36));
        applyBtn.addActionListener(e -> applyStock());
        btnPanel.add(applyBtn);
        formCard.add(btnPanel, BorderLayout.SOUTH);

        mainPanel.add(formCard);

        // RIGHT: Quick guide card
        JPanel guideCard = new JPanel(new BorderLayout());
        guideCard.setBackground(Color.WHITE);
        guideCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeUtil.BORDER_COLOR, 1),
            new EmptyBorder(24, 24, 24, 24)
        ));

        JLabel guideTitle = new JLabel("Quick Guide");
        guideTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        guideTitle.setForeground(ThemeUtil.TEXT_PRIMARY);
        guideTitle.setBorder(new EmptyBorder(0, 0, 16, 0));
        guideCard.add(guideTitle, BorderLayout.NORTH);

        JPanel guideContent = new JPanel();
        guideContent.setLayout(new BoxLayout(guideContent, BoxLayout.Y_AXIS));
        guideContent.setOpaque(false);

        addGuideItem(guideContent, "📦  Stock IN",
            "Use when receiving new stock from a supplier.\nThe quantity will be ADDED to current stock.",
            ThemeUtil.SUCCESS);

        guideContent.add(Box.createVerticalStrut(16));

        addGuideItem(guideContent, "📤  Stock OUT",
            "Use when stock leaves the warehouse.\nThe quantity will be SUBTRACTED from current stock.",
            ThemeUtil.DANGER);

        guideContent.add(Box.createVerticalStrut(16));

        addGuideItem(guideContent, "⚠  Low Stock Warning",
            "Products shown in orange are running low.\nConsider doing a Stock IN to replenish.",
            ThemeUtil.WARNING);

        guideContent.add(Box.createVerticalStrut(16));

        addGuideItem(guideContent, "📋  Transaction Record",
            "Every stock movement is automatically recorded\nin the transaction history for audit purposes.",
            ThemeUtil.PRIMARY);

        guideCard.add(guideContent, BorderLayout.CENTER);
        mainPanel.add(guideCard);

        JScrollPane mainScroll = new JScrollPane(mainPanel);
        mainScroll.setBorder(null);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        mainScroll.getVerticalScrollBar().setUnitIncrement(12);
        add(mainScroll, BorderLayout.CENTER);

        // Initialize product info
        updateProductInfo();
    }

    private void addGuideItem(JPanel parent, String title, String desc, Color color) {
        JPanel item = new JPanel(new BorderLayout(0, 6));
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(color);

        JTextArea descArea = new JTextArea(desc);
        descArea.setFont(ThemeUtil.FONT_SMALL);
        descArea.setForeground(ThemeUtil.TEXT_MUTED);
        descArea.setEditable(false);
        descArea.setOpaque(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(null);

        item.add(titleLbl, BorderLayout.NORTH);
        item.add(descArea, BorderLayout.CENTER);
        parent.add(item);
    }

    private void updateProductInfo() {
        Product p = (Product) productCombo.getSelectedItem();
        if (p != null) {
            currentQtyLabel.setText("Current Stock:  " + p.getQuantity() + " units");
            currentQtyLabel.setForeground(ThemeUtil.stockStatusColor(p.getStockStatus()));
            statusLabel.setText("Status:  " + p.getStockStatus());
            statusLabel.setForeground(ThemeUtil.stockStatusColor(p.getStockStatus()));
        }
    }

    private void applyStock() {
        Product p = (Product) productCombo.getSelectedItem();
        if (p == null) { NotificationUtils.showWarning(this, "Select a product."); return; }

        int qty;
        try {
            qty = Integer.parseInt(qtyField.getText().trim());
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            NotificationUtils.showError(this, "Enter a valid quantity greater than 0.");
            return;
        }

        // Get type from combo (strip description)
        String typeSelected = (String) typeCombo.getSelectedItem();
        String type = typeSelected != null && typeSelected.startsWith("IN") ? "IN" : "OUT";

        if ("OUT".equals(type) && p.getQuantity() < qty) {
            NotificationUtils.showWarning(this,
                "Insufficient stock!\nAvailable: " + p.getQuantity() + " units\nRequested: " + qty + " units");
            return;
        }

        String confirmMsg = String.format(
            "Confirm Stock %s\n\nProduct: %s\nQuantity: %d units\nCurrent Stock: %d → New Stock: %d",
            type, p.getName(), qty, p.getQuantity(),
            type.equals("IN") ? p.getQuantity() + qty : p.getQuantity() - qty
        );

        if (!NotificationUtils.showConfirm(this, confirmMsg)) return;

        String err = stockCtrl.processStock(p.getId(), type, qty, notesArea.getText().trim());
        if (err == null) {
            NotificationUtils.showSuccess(this, "Stock " + type + " applied successfully.");
            qtyField.setText("");
            notesArea.setText("");
            // Refresh product list
            SwingWorker<List<Product>, Void> w = new SwingWorker<>() {
                @Override protected List<Product> doInBackground() { return productCtrl.getActiveProducts(); }
                @Override protected void done() {
                    try {
                        productCombo.removeAllItems();
                        for (Product prod : get()) productCombo.addItem(prod);
                        updateProductInfo();
                    } catch (Exception ex) { /* ignore */ }
                }
            };
            w.execute();
        } else {
            NotificationUtils.showError(this, err);
        }
    }
}
