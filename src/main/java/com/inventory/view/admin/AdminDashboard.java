package com.inventory.view.admin;

import com.inventory.controller.*;
import com.inventory.model.Transaction;
import com.inventory.view.components.ThemeUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Admin dashboard with KPI cards, recent transactions, and a bar chart.
 */
public class AdminDashboard extends JPanel {

    private final ProductController productCtrl = new ProductController();
    private final StockController stockCtrl = new StockController();
    private final UserController userCtrl = new UserController();

    private JLabel totalProductsVal, inventoryValueVal, lowStockVal, outOfStockVal, totalUsersVal, totalTxVal;
    private DefaultTableModel txTableModel;

    public AdminDashboard() {
        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeUtil.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        loadData();

        // Auto-refresh every 30 seconds
        Timer refreshTimer = new Timer(30_000, e -> loadData());
        refreshTimer.start();
    }

    private void buildUI() {
        // KPI cards row
        JPanel cardsPanel = new JPanel(new GridLayout(1, 6, 12, 0));
        cardsPanel.setOpaque(false);

        totalProductsVal  = addKpiCard(cardsPanel, "Total Products",    "0", ThemeUtil.PRIMARY);
        inventoryValueVal = addKpiCard(cardsPanel, "Inventory Value",   "$0", ThemeUtil.SUCCESS);
        lowStockVal       = addKpiCard(cardsPanel, "Low Stock",         "0", ThemeUtil.WARNING);
        outOfStockVal     = addKpiCard(cardsPanel, "Out of Stock",      "0", ThemeUtil.DANGER);
        totalUsersVal     = addKpiCard(cardsPanel, "Total Users",       "0", ThemeUtil.PRIMARY_DARK);
        totalTxVal        = addKpiCard(cardsPanel, "Transactions",      "0", new Color(103, 58, 183));

        add(cardsPanel, BorderLayout.NORTH);

        // Centre: chart + recent transactions
        JPanel centrePanel = new JPanel(new GridLayout(1, 2, 16, 0));
        centrePanel.setOpaque(false);
        centrePanel.add(buildChartPanel());
        centrePanel.add(buildRecentTransactionsPanel());
        add(centrePanel, BorderLayout.CENTER);
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

    private JPanel buildChartPanel() {
        JPanel wrapper = ThemeUtil.createCard();
        wrapper.setLayout(new BorderLayout());
        wrapper.add(ThemeUtil.sectionTitle("Monthly Transactions"), BorderLayout.NORTH);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<Object[]> summary = stockCtrl.getMonthlySummary();
        for (Object[] row : summary) {
            dataset.addValue((Number) row[1], "Transactions", (String) row[0]);
        }

        JFreeChart chart = ChartFactory.createBarChart(null, "Month", "Count", dataset);
        chart.setBackgroundPaint(Color.WHITE);
        chart.removeLegend();
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(ThemeUtil.BORDER_COLOR);
        ((BarRenderer) plot.getRenderer()).setSeriesPaint(0, ThemeUtil.PRIMARY);

        ChartPanel cp = new ChartPanel(chart);
        cp.setPreferredSize(new Dimension(0, 260));
        wrapper.add(cp, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildRecentTransactionsPanel() {
        JPanel wrapper = ThemeUtil.createCard();
        wrapper.setLayout(new BorderLayout(0, 8));
        wrapper.add(ThemeUtil.sectionTitle("Recent Transactions"), BorderLayout.NORTH);

        String[] cols = {"Code", "Product", "Type", "Qty", "Date"};
        txTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(txTableModel);
        ThemeUtil.styleTable(table);
        wrapper.add(new JScrollPane(table), BorderLayout.CENTER);
        return wrapper;
    }

    private void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            int products, lowStock, outOfStock, users, transactions;
            BigDecimal value;
            List<Transaction> recent;

            @Override protected Void doInBackground() {
                products     = productCtrl.getTotalProducts();
                value        = productCtrl.getTotalInventoryValue();
                lowStock     = productCtrl.getLowStockCount();
                outOfStock   = productCtrl.getOutOfStockCount();
                users        = userCtrl.getTotalUsers();
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
                totalUsersVal.setText(String.valueOf(users));
                totalTxVal.setText(String.valueOf(transactions));

                txTableModel.setRowCount(0);
                for (Transaction tx : recent) {
                    txTableModel.addRow(new Object[]{
                        tx.getTransactionCode(),
                        tx.getProductName(),
                        tx.getType(),
                        tx.getQuantity(),
                        tx.getTransactionDate()
                    });
                }
            }
        };
        worker.execute();
    }
}
