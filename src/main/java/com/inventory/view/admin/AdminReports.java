package com.inventory.view.admin;

import com.inventory.controller.ReportController;
import com.inventory.model.Product;
import com.inventory.model.Transaction;
import com.inventory.utils.ExportUtils;
import com.inventory.utils.PDFGenerator;
import com.inventory.view.components.ThemeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Reports panel with inventory, low-stock, and transaction reports.
 */
public class AdminReports extends JPanel {

    private final ReportController reportCtrl = new ReportController();
    private DefaultTableModel tableModel;
    private JTable table;
    private JComboBox<String> reportTypeCombo;
    private JSpinner fromSpinner, toSpinner;
    private JLabel summaryLabel;

    public AdminReports() {
        setLayout(new BorderLayout(0, 12));
        setBackground(ThemeUtil.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        // Don't auto-generate on load — wait for user to click Generate
    }

    private void buildUI() {
        // Controls bar
        JPanel controls = ThemeUtil.createCard();
        controls.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));

        controls.add(new JLabel("Report Type:"));
        reportTypeCombo = new JComboBox<>(new String[]{"Inventory Report", "Low Stock Report", "Transaction Report"});
        reportTypeCombo.setFont(ThemeUtil.FONT_BODY);
        controls.add(reportTypeCombo);

        controls.add(new JLabel("  From:"));
        fromSpinner = createDateSpinner(minusMonths(1));
        controls.add(fromSpinner);

        controls.add(new JLabel("To:"));
        toSpinner = createDateSpinner(new Date());
        controls.add(toSpinner);

        JButton generateBtn = ThemeUtil.primaryButton("Generate");
        JButton exportCsvBtn = ThemeUtil.secondaryButton("Export CSV");
        JButton exportPdfBtn = ThemeUtil.warningButton("Export PDF");

        generateBtn.addActionListener(e -> generateReport());
        exportCsvBtn.addActionListener(e -> ExportUtils.exportToCSV(this, tableModel, "report"));
        exportPdfBtn.addActionListener(e -> PDFGenerator.exportToPDF(this, tableModel,
                (String) reportTypeCombo.getSelectedItem(), "report"));

        controls.add(generateBtn); controls.add(exportCsvBtn); controls.add(exportPdfBtn);
        add(controls, BorderLayout.NORTH);

        // Summary
        summaryLabel = new JLabel(" ");
        summaryLabel.setFont(ThemeUtil.FONT_BOLD);
        summaryLabel.setForeground(ThemeUtil.PRIMARY);
        summaryLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        add(summaryLabel, BorderLayout.SOUTH);

        // Table
        tableModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        ThemeUtil.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void generateReport() {
        String type = (String) reportTypeCombo.getSelectedItem();
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                if ("Inventory Report".equals(type)) {
                    List<Product> products = reportCtrl.getInventoryReport();
                    SwingUtilities.invokeLater(() -> {
                        tableModel.setColumnIdentifiers(new String[]{"Code", "Name", "Category", "Supplier", "Qty", "Price", "Value", "Status"});
                        double totalValue = 0;
                        for (Product p : products) {
                            double val = p.getPrice().doubleValue() * p.getQuantity();
                            totalValue += val;
                            tableModel.addRow(new Object[]{
                                p.getCode(), p.getName(), p.getCategoryName(), p.getSupplierName(),
                                p.getQuantity(), String.format("$%.2f", p.getPrice()),
                                String.format("$%.2f", val), p.getStockStatus()
                            });
                        }
                        summaryLabel.setText("Total Products: " + products.size() +
                                "  |  Total Inventory Value: $" + String.format("%.2f", totalValue));
                    });
                } else if ("Low Stock Report".equals(type)) {
                    List<Product> products = reportCtrl.getLowStockReport();
                    SwingUtilities.invokeLater(() -> {
                        tableModel.setColumnIdentifiers(new String[]{"Code", "Name", "Category", "Qty", "Reorder Level", "Status"});
                        for (Product p : products) {
                            tableModel.addRow(new Object[]{
                                p.getCode(), p.getName(), p.getCategoryName(),
                                p.getQuantity(), p.getReorderLevel(), p.getStockStatus()
                            });
                        }
                        summaryLabel.setText("Low/Out of Stock Items: " + products.size());
                    });
                } else {
                    Date from = (Date) fromSpinner.getValue();
                    Date to   = (Date) toSpinner.getValue();

                    // Set from to start of day (00:00:00)
                    java.util.Calendar calFrom = java.util.Calendar.getInstance();
                    calFrom.setTime(from);
                    calFrom.set(java.util.Calendar.HOUR_OF_DAY, 0);
                    calFrom.set(java.util.Calendar.MINUTE, 0);
                    calFrom.set(java.util.Calendar.SECOND, 0);
                    calFrom.set(java.util.Calendar.MILLISECOND, 0);

                    // Set to to end of day (23:59:59)
                    java.util.Calendar calTo = java.util.Calendar.getInstance();
                    calTo.setTime(to);
                    calTo.set(java.util.Calendar.HOUR_OF_DAY, 23);
                    calTo.set(java.util.Calendar.MINUTE, 59);
                    calTo.set(java.util.Calendar.SECOND, 59);
                    calTo.set(java.util.Calendar.MILLISECOND, 999);

                    List<Transaction> txs = reportCtrl.getTransactionReport(
                            new Timestamp(calFrom.getTimeInMillis()),
                            new Timestamp(calTo.getTimeInMillis()));
                    SwingUtilities.invokeLater(() -> {
                        tableModel.setColumnIdentifiers(new String[]{"Code", "Product", "Type", "Qty", "Price", "Total", "User", "Date"});
                        double total = 0;
                        for (Transaction tx : txs) {
                            double amt = tx.getTotalAmount() != null ? tx.getTotalAmount().doubleValue() : 0;
                            total += amt;
                            tableModel.addRow(new Object[]{
                                tx.getTransactionCode(), tx.getProductName(), tx.getType(),
                                tx.getQuantity(),
                                tx.getPrice() != null ? String.format("$%.2f", tx.getPrice()) : "-",
                                String.format("$%.2f", amt),
                                tx.getUsername(), tx.getTransactionDate()
                            });
                        }
                        summaryLabel.setText("Transactions from " +
                                new SimpleDateFormat("dd/MM/yyyy").format(calFrom.getTime()) +
                                " to " + new SimpleDateFormat("dd/MM/yyyy").format(calTo.getTime()) +
                                "  |  Count: " + txs.size() +
                                "  |  Total: $" + String.format("%.2f", total));
                    });
                }
                return null;
            }
        };
        worker.execute();
    }

    private JSpinner createDateSpinner(Date initial) {
        SpinnerDateModel model = new SpinnerDateModel(initial, null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        spinner.setFont(ThemeUtil.FONT_BODY);
        return spinner;
    }

    private Date minusMonths(int months) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.MONTH, -months);
        return cal.getTime();
    }
}
