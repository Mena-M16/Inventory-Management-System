package com.inventory.view.admin;

import com.inventory.controller.StockController;
import com.inventory.model.Transaction;
import com.inventory.utils.ExportUtils;
import com.inventory.utils.NotificationUtils;
import com.inventory.view.components.PaginatedTable;
import com.inventory.view.components.ThemeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;


public class AdminStock extends JPanel {

    private final StockController stockCtrl = new StockController();
    private PaginatedTable paginatedTable;
    private DefaultTableModel tableModel;

    private static final String[] TX_COLS = {
        "Transaction Code", "Product", "Type", "Qty", "Total", "User", "Date"
    };

    public AdminStock() {
        setLayout(new BorderLayout(0, 12));
        setBackground(ThemeUtil.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        loadTransactions();
    }

    private void buildUI() {
        // Header bar
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setOpaque(false);
        headerBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JLabel titleLbl = new JLabel("Stock Transaction History");
        titleLbl.setFont(ThemeUtil.FONT_TITLE);
        titleLbl.setForeground(ThemeUtil.TEXT_PRIMARY);
        headerBar.add(titleLbl, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        JButton refreshBtn = ThemeUtil.secondaryButton("Refresh");
        JButton exportBtn  = ThemeUtil.secondaryButton("Export CSV");
        refreshBtn.addActionListener(e -> refresh());
        exportBtn.addActionListener(e -> ExportUtils.exportToCSV(this, tableModel, "stock_history"));
        btnPanel.add(refreshBtn);
        btnPanel.add(exportBtn);
        headerBar.add(btnPanel, BorderLayout.EAST);
        add(headerBar, BorderLayout.NORTH);

        // Info banner
        JPanel infoBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        infoBanner.setBackground(ThemeUtil.PRIMARY_LIGHT);
        infoBanner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(147, 197, 253), 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        JLabel infoLbl = new JLabel(
            "\u2139  Stock IN is recorded from the Products page using the \"Add Stock\" button.  " +
            "Stock OUT is recorded automatically when a sale is completed."
        );
        infoLbl.setFont(ThemeUtil.FONT_SMALL);
        infoLbl.setForeground(ThemeUtil.PRIMARY);
        infoBanner.add(infoLbl);
        add(infoBanner, BorderLayout.NORTH);

        // Wrap both in a north panel
        JPanel northPanel = new JPanel(new BorderLayout(0, 8));
        northPanel.setOpaque(false);
        northPanel.add(headerBar, BorderLayout.NORTH);
        northPanel.add(infoBanner, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(TX_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        ThemeUtil.styleTable(table);

        // Color-code Type column
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                JLabel lbl = new JLabel(value == null ? "" : value.toString());
                lbl.setFont(ThemeUtil.FONT_BOLD);
                lbl.setOpaque(true);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                String type = value == null ? "" : value.toString();
                if (!sel) {
                    switch (type) {
                        case "IN":     lbl.setBackground(new Color(220, 252, 231)); lbl.setForeground(ThemeUtil.SUCCESS); break;
                        case "OUT":    lbl.setBackground(new Color(254, 226, 226)); lbl.setForeground(ThemeUtil.DANGER);  break;
                        case "ADJUST": lbl.setBackground(new Color(254, 243, 199)); lbl.setForeground(ThemeUtil.WARNING); break;
                        default:       lbl.setBackground(Color.WHITE); lbl.setForeground(ThemeUtil.TEXT_PRIMARY);
                    }
                } else {
                    lbl.setBackground(ThemeUtil.PRIMARY_LIGHT);
                    lbl.setForeground(ThemeUtil.TEXT_PRIMARY);
                }
                lbl.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return lbl;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeUtil.BORDER_COLOR));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadTransactions() {
        SwingWorker<List<Transaction>, Void> worker = new SwingWorker<>() {
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
                } catch (Exception ex) {
                    NotificationUtils.showError(AdminStock.this, "Failed to load transactions.");
                }
            }
        };
        worker.execute();
    }

    public void refresh() { loadTransactions(); }
}
