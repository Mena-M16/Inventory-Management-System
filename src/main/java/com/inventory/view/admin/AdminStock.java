package com.inventory.view.admin;

import com.inventory.controller.ProductController;
import com.inventory.controller.StockController;
import com.inventory.model.Product;
import com.inventory.model.Transaction;
import com.inventory.utils.ExportUtils;
import com.inventory.utils.NotificationUtils;
import com.inventory.view.components.ThemeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Stock management panel: IN / OUT / ADJUST with transaction history.
 */
public class AdminStock extends JPanel {

    private final StockController stockCtrl = new StockController();
    private final ProductController productCtrl = new ProductController();

    private DefaultTableModel txTableModel;
    private JComboBox<Product> productCombo;
    private JComboBox<String> typeCombo;
    private JTextField qtyField;
    private JTextArea notesArea;
    private JLabel currentQtyLabel;

    private static final String[] TX_COLS = {"Code", "Product", "Type", "Qty", "Total", "User", "Date"};

    public AdminStock() {
        setLayout(new BorderLayout(16, 0));
        setBackground(ThemeUtil.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        loadTransactions();
    }

    private void buildUI() {
        // Left: stock form
        JPanel formCard = ThemeUtil.createCard();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setPreferredSize(new Dimension(320, 0));

        formCard.add(ThemeUtil.sectionTitle("Stock Adjustment"));
        formCard.add(Box.createVerticalStrut(16));

        List<Product> products = productCtrl.getActiveProducts();
        productCombo = new JComboBox<>(products.toArray(new Product[0]));
        productCombo.setFont(ThemeUtil.FONT_BODY);
        productCombo.addActionListener(e -> updateCurrentQty());

        typeCombo = new JComboBox<>(new String[]{"IN", "OUT", "ADJUST"});
        typeCombo.setFont(ThemeUtil.FONT_BODY);

        qtyField = ThemeUtil.createTextField(10);
        notesArea = new JTextArea(3, 10);
        notesArea.setFont(ThemeUtil.FONT_BODY);
        notesArea.setBorder(BorderFactory.createLineBorder(ThemeUtil.BORDER_COLOR));

        currentQtyLabel = new JLabel("Current Stock: -");
        currentQtyLabel.setFont(ThemeUtil.FONT_BOLD);
        currentQtyLabel.setForeground(ThemeUtil.PRIMARY);

        addFormRow(formCard, "Product:", productCombo);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(currentQtyLabel);
        formCard.add(Box.createVerticalStrut(10));
        addFormRow(formCard, "Type:", typeCombo);
        addFormRow(formCard, "Quantity:", qtyField);
        addFormRow(formCard, "Notes:", new JScrollPane(notesArea));

        formCard.add(Box.createVerticalStrut(16));

        JButton applyBtn = ThemeUtil.primaryButton("Apply");
        applyBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        applyBtn.addActionListener(e -> applyStock());
        formCard.add(applyBtn);

        updateCurrentQty();
        add(formCard, BorderLayout.WEST);

        // Right: transaction history
        JPanel historyCard = ThemeUtil.createCard();
        historyCard.setLayout(new BorderLayout(0, 8));

        JPanel histHeader = new JPanel(new BorderLayout());
        histHeader.setOpaque(false);
        histHeader.add(ThemeUtil.sectionTitle("Transaction History"), BorderLayout.WEST);

        JPanel histBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        histBtns.setOpaque(false);
        JButton refreshBtn = ThemeUtil.secondaryButton("⟳  Refresh");
        JButton exportBtn  = ThemeUtil.secondaryButton("Export CSV");
        refreshBtn.addActionListener(e -> {
            refreshBtn.setEnabled(false);
            refreshBtn.setText("Refreshing...");
            // Refresh product combo and transactions
            SwingWorker<List<Product>, Void> w = new SwingWorker<>() {
                @Override protected List<Product> doInBackground() { return productCtrl.getActiveProducts(); }
                @Override protected void done() {
                    try {
                        productCombo.removeAllItems();
                        for (Product prod : get()) productCombo.addItem(prod);
                        updateCurrentQty();
                        loadTransactions();
                    } catch (Exception ex) { /* ignore */ } finally {
                        refreshBtn.setEnabled(true);
                        refreshBtn.setText("⟳  Refresh");
                    }
                }
            };
            w.execute();
        });
        exportBtn.addActionListener(e -> ExportUtils.exportToCSV(this, txTableModel, "transactions"));
        histBtns.add(refreshBtn);
        histBtns.add(exportBtn);
        histHeader.add(histBtns, BorderLayout.EAST);
        historyCard.add(histHeader, BorderLayout.NORTH);

        txTableModel = new DefaultTableModel(TX_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(txTableModel);
        ThemeUtil.styleTable(table);
        historyCard.add(new JScrollPane(table), BorderLayout.CENTER);
        add(historyCard, BorderLayout.CENTER);
    }

    private void addFormRow(JPanel panel, String label, Component comp) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(ThemeUtil.FONT_BOLD);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(3));
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, comp.getPreferredSize().height + 4));
        ((JComponent) comp).setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(comp);
        panel.add(Box.createVerticalStrut(10));
    }

    private void updateCurrentQty() {
        Product p = (Product) productCombo.getSelectedItem();
        if (p != null) {
            currentQtyLabel.setText("Current Stock: " + p.getQuantity() + "  |  Status: " + p.getStockStatus());
            currentQtyLabel.setForeground(ThemeUtil.stockStatusColor(p.getStockStatus()));
        }
    }

    private void applyStock() {
        Product p = (Product) productCombo.getSelectedItem();
        if (p == null) { NotificationUtils.showWarning(this, "Select a product."); return; }

        int qty;
        try { qty = Integer.parseInt(qtyField.getText().trim()); }
        catch (NumberFormatException ex) { NotificationUtils.showError(this, "Enter a valid quantity."); return; }

        String type = (String) typeCombo.getSelectedItem();
        String notes = notesArea.getText().trim();

        if ("OUT".equals(type) && p.getQuantity() < qty) {
            NotificationUtils.showWarning(this, "Insufficient stock! Available: " + p.getQuantity());
            return;
        }

        if (!NotificationUtils.showConfirm(this, "Apply " + type + " of " + qty + " units for \"" + p.getName() + "\"?")) return;

        String err = stockCtrl.processStock(p.getId(), type, qty, notes);
        if (err == null) {
            NotificationUtils.showSuccess(this, "Stock " + type + " applied successfully.");
            qtyField.setText("");
            notesArea.setText("");
            // Refresh product list without duplicates
            SwingWorker<List<Product>, Void> w = new SwingWorker<>() {
                @Override protected List<Product> doInBackground() { return productCtrl.getActiveProducts(); }
                @Override protected void done() {
                    try {
                        productCombo.removeAllItems();
                        for (Product prod : get()) productCombo.addItem(prod);
                        loadTransactions();
                    } catch (Exception ex) { /* ignore */ }
                }
            };
            w.execute();
        } else {
            NotificationUtils.showError(this, err);
        }
    }

    private void loadTransactions() {
        SwingWorker<List<Transaction>, Void> worker = new SwingWorker<>() {
            @Override protected List<Transaction> doInBackground() { return stockCtrl.getAllTransactions(); }
            @Override protected void done() {
                try {
                    txTableModel.setRowCount(0);
                    for (Transaction tx : get()) {
                        txTableModel.addRow(new Object[]{
                            tx.getTransactionCode(), tx.getProductName(), tx.getType(),
                            tx.getQuantity(),
                            tx.getTotalAmount() != null ? String.format("$%.2f", tx.getTotalAmount()) : "-",
                            tx.getUsername(), tx.getTransactionDate()
                        });
                    }
                } catch (Exception ex) {
                    NotificationUtils.showError(AdminStock.this, "Failed to load transactions.");
                }
            }
        };
        worker.execute();
    }

    /** Public refresh callable from MainWindow F5. */
    public void refresh() { loadTransactions(); }
}

