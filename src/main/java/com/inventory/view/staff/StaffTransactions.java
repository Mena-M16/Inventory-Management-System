package com.inventory.view.staff;

import com.inventory.controller.StockController;
import com.inventory.model.Transaction;
import com.inventory.view.components.ThemeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Staff transaction history view (read-only).
 */
public class StaffTransactions extends JPanel {

    private final StockController stockCtrl = new StockController();
    private DefaultTableModel tableModel;

    private static final String[] COLUMNS = {"Code", "Product", "Type", "Qty", "Total", "User", "Date"};

    public StaffTransactions() {
        setLayout(new BorderLayout(0, 12));
        setBackground(ThemeUtil.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        loadTransactions();
    }

    private void buildUI() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setOpaque(false);
        JButton refreshBtn = ThemeUtil.secondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadTransactions());
        toolbar.add(refreshBtn);
        add(toolbar, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        ThemeUtil.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadTransactions() {
        SwingWorker<List<Transaction>, Void> w = new SwingWorker<>() {
            @Override protected List<Transaction> doInBackground() { return stockCtrl.getAllTransactions(); }
            @Override protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (Transaction tx : get()) {
                        tableModel.addRow(new Object[]{
                            tx.getTransactionCode(), tx.getProductName(), tx.getType(),
                            tx.getQuantity(),
                            tx.getTotalAmount() != null ? String.format("$%.2f", tx.getTotalAmount()) : "-",
                            tx.getUsername(), tx.getTransactionDate()
                        });
                    }
                } catch (Exception ignored) {}
            }
        };
        w.execute();
    }
}
