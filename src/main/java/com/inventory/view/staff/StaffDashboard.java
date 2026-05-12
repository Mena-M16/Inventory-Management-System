package com.inventory.view.staff;

import com.inventory.controller.ProductController;
import com.inventory.controller.StockController;
import com.inventory.model.Transaction;
import com.inventory.view.components.ThemeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Staff dashboard showing basic KPIs and recent transactions with a refresh button.
 */
public class StaffDashboard extends JPanel {

    private final ProductController productCtrl = new ProductController();
    private final StockController stockCtrl = new StockController();

    private JLabel totalProductsVal;
    private JLabel lowStockVal;
    private JLabel outOfStockVal;
    private DefaultTableModel txModel;

    public StaffDashboard() {
        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeUtil.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        loadData();
    }

    private void buildUI() {
        // Top: title + refresh button
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel titleLbl = new JLabel("Dashboard");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(ThemeUtil.TEXT_PRIMARY);
        topBar.add(titleLbl, BorderLayout.WEST);

        JButton refreshBtn = ThemeUtil.primaryButton("⟳  Refresh");
        refreshBtn.setPreferredSize(new Dimension(120, 36));
        refreshBtn.addActionListener(e -> {
            refreshBtn.setEnabled(false);
            refreshBtn.setText("Refreshing...");
            loadData();
            Timer t = new Timer(1000, ev -> {
                refreshBtn.setEnabled(true);
                refreshBtn.setText("⟳  Refresh");
            });
            t.setRepeats(false);
            t.start();
        });
        topBar.add(refreshBtn, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // KPI cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setPreferredSize(new Dimension(0, 100));

        totalProductsVal = addKpiCard(cardsPanel, "Total Products",  "—", ThemeUtil.PRIMARY);
        lowStockVal      = addKpiCard(cardsPanel, "Low Stock Items", "—", ThemeUtil.WARNING);
        outOfStockVal    = addKpiCard(cardsPanel, "Out of Stock",    "—", ThemeUtil.DANGER);

        // Wrap cards in a panel so they don't stretch
        JPanel northWrapper = new JPanel(new BorderLayout(0, 12));
        northWrapper.setOpaque(false);
        northWrapper.add(topBar, BorderLayout.NORTH);
        northWrapper.add(cardsPanel, BorderLayout.CENTER);
        add(northWrapper, BorderLayout.NORTH);

        // Recent transactions card
        JPanel card = ThemeUtil.createCard();
        card.setLayout(new BorderLayout(0, 8));

        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        cardHeader.add(ThemeUtil.sectionTitle("Recent Transactions"), BorderLayout.WEST);

        card.add(cardHeader, BorderLayout.NORTH);

        txModel = new DefaultTableModel(
                new String[]{"Code", "Product", "Type", "Qty", "Date"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(txModel);
        ThemeUtil.styleTable(table);

        // Color-code transaction type
        table.getColumnModel().getColumn(2).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                String type = value == null ? "" : value.toString();
                if ("IN".equals(type))     setForeground(ThemeUtil.SUCCESS);
                else if ("OUT".equals(type)) setForeground(ThemeUtil.DANGER);
                else                         setForeground(ThemeUtil.WARNING);
                setFont(ThemeUtil.FONT_BOLD);
                return this;
            }
        });

        card.add(new JScrollPane(table), BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
    }

    private void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            int total, lowStock, outOfStock;
            List<Transaction> recent;

            @Override protected Void doInBackground() {
                total      = productCtrl.getTotalProducts();
                lowStock   = productCtrl.getLowStockCount();
                outOfStock = productCtrl.getOutOfStockCount();
                recent     = stockCtrl.getRecentTransactions(20);
                return null;
            }

            @Override protected void done() {
                totalProductsVal.setText(String.valueOf(total));
                lowStockVal.setText(String.valueOf(lowStock));
                outOfStockVal.setText(String.valueOf(outOfStock));

                txModel.setRowCount(0);
                for (Transaction tx : recent) {
                    txModel.addRow(new Object[]{
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
}
