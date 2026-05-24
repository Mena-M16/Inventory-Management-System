package com.inventory.view.manager;

import com.inventory.controller.ProductController;
import com.inventory.controller.StockController;
import com.inventory.model.Product;
import com.inventory.model.Transaction;
import com.inventory.view.components.ThemeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Manager dashboard with KPI cards and recent transactions.
 */
public class ManagerDashboard extends JPanel {

    private final ProductController productCtrl = new ProductController();
    private final StockController stockCtrl = new StockController();

    private JLabel totalProductsVal, inventoryValueVal, lowStockVal, outOfStockVal, totalTxVal;
    private DefaultTableModel txTableModel;

    public ManagerDashboard() {
        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeUtil.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        loadData();
        new Timer(30_000, e -> loadData()).start();
    }

    private void buildUI() {
        JPanel cardsPanel = new JPanel(new GridLayout(1, 5, 12, 0));
        cardsPanel.setOpaque(false);

        totalProductsVal  = addKpiCard(cardsPanel, "Total Products",  "0", ThemeUtil.PRIMARY);
        inventoryValueVal = addKpiCard(cardsPanel, "Inventory Value", "$0", ThemeUtil.SUCCESS);
        lowStockVal       = addKpiCard(cardsPanel, "Low Stock",       "0", ThemeUtil.WARNING);
        outOfStockVal     = addKpiCard(cardsPanel, "Out of Stock",    "0", ThemeUtil.DANGER);
        totalTxVal        = addKpiCard(cardsPanel, "Transactions",    "0", new Color(103, 58, 183));
        add(cardsPanel, BorderLayout.NORTH);

        // Low stock alerts + recent transactions
        JPanel centre = new JPanel(new GridLayout(1, 2, 16, 0));
        centre.setOpaque(false);
        centre.add(buildLowStockPanel());
        centre.add(buildRecentTxPanel());
        add(centre, BorderLayout.CENTER);
    }

    private JLabel addKpiCard(JPanel parent, String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(ThemeUtil.FONT_SMALL);
        titleLbl.setForeground(ThemeUtil.TEXT_MUTED);
        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLbl.setForeground(accent);
        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        parent.add(card);
        return valueLbl;
    }

    private JPanel buildLowStockPanel() {
        JPanel card = ThemeUtil.createCard();
        card.setLayout(new BorderLayout(0, 8));
        card.add(ThemeUtil.sectionTitle("Low Stock Alerts"), BorderLayout.NORTH);
        DefaultTableModel model = new DefaultTableModel(new String[]{"Product", "Qty", "Reorder"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        ThemeUtil.styleTable(table);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        SwingWorker<List<Product>, Void> w = new SwingWorker<>() {
            @Override protected List<Product> doInBackground() { return productCtrl.getLowStockProducts(); }
            @Override protected void done() {
                try {
                    for (Product p : get()) {
                        model.addRow(new Object[]{p.getName(), p.getQuantity(), p.getReorderLevel()});
                    }
                } catch (Exception ignored) {}
            }
        };
        w.execute();
        return card;
    }

    private JPanel buildRecentTxPanel() {
        JPanel card = ThemeUtil.createCard();
        card.setLayout(new BorderLayout(0, 8));
        card.add(ThemeUtil.sectionTitle("Recent Transactions"), BorderLayout.NORTH);
        txTableModel = new DefaultTableModel(new String[]{"Code", "Product", "Type", "Qty", "Date"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(txTableModel);
        ThemeUtil.styleTable(table);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private void loadData() {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            int products, lowStock, outOfStock, transactions;
            BigDecimal value;
            List<Transaction> recent;

            @Override protected Void doInBackground() {
                products     = productCtrl.getTotalProducts();
                value        = productCtrl.getTotalInventoryValue();
                lowStock     = productCtrl.getLowStockCount();
                outOfStock   = productCtrl.getOutOfStockCount();
                transactions = stockCtrl.getTotalTransactions();
                recent       = stockCtrl.getRecentTransactions(10);
                return null;
            }

            @Override protected void done() {
                NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
                totalProductsVal.setText(String.valueOf(products));
                inventoryValueVal.setText(currency.format(value));
                lowStockVal.setText(String.valueOf(lowStock));
                outOfStockVal.setText(String.valueOf(outOfStock));
                totalTxVal.setText(String.valueOf(transactions));
                txTableModel.setRowCount(0);
                for (Transaction tx : recent) {
                    txTableModel.addRow(new Object[]{tx.getTransactionCode(), tx.getProductName(),
                            tx.getType(), tx.getQuantity(), tx.getTransactionDate()});
                }
            }
        };
        w.execute();
    }
}
