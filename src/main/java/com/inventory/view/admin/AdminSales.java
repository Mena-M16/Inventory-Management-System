package com.inventory.view.admin;

import com.inventory.controller.ProductController;
import com.inventory.controller.SaleController;
import com.inventory.model.Product;
import com.inventory.model.Sale;
import com.inventory.utils.ExportUtils;
import com.inventory.utils.NotificationUtils;
import com.inventory.view.components.ThemeUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Sales management panel — supports selling multiple items in one transaction.
 */
public class AdminSales extends JPanel {

    private final SaleController saleCtrl = new SaleController();
    private final ProductController productCtrl = new ProductController();

    private DefaultTableModel salesHistoryModel;
    private JLabel revenueLabel;
    private JLabel totalSalesLabel;

    // Cart items: {Product, qty, discount%, vat%}
    private final List<Object[]> cartItems = new ArrayList<>();
    private DefaultTableModel cartModel;
    private JLabel cartTotalLabel;

    private static final String[] HISTORY_COLS = {
        "Sale Code", "Product", "Qty", "Unit Price", "Subtotal",
        "Discount", "VAT", "Final", "Customer", "By", "Date"
    };
    private static final String[] CART_COLS = {"Product", "Code", "Qty", "Unit Price", "Discount%", "VAT%", "Line Total"};

    public AdminSales() {
        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeUtil.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        loadSalesHistory();
    }

    private void buildUI() {
        // Top bar
        JPanel topBar = new JPanel(new BorderLayout(16, 0));
        topBar.setOpaque(false);

        JPanel kpiPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        kpiPanel.setOpaque(false);
        totalSalesLabel = addKpiCard(kpiPanel, "Total Sales",   "0",     ThemeUtil.PRIMARY);
        revenueLabel    = addKpiCard(kpiPanel, "Total Revenue", "$0.00", ThemeUtil.SUCCESS);
        topBar.add(kpiPanel, BorderLayout.CENTER);

        JButton newSaleBtn = ThemeUtil.successButton("+ New Sale");
        newSaleBtn.setPreferredSize(new Dimension(120, 46));
        newSaleBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        newSaleBtn.addActionListener(e -> showSaleDialog(null));
        topBar.add(newSaleBtn, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Sales history
        JPanel histCard = ThemeUtil.createCard();
        histCard.setLayout(new BorderLayout(0, 8));

        JPanel histHeader = new JPanel(new BorderLayout());
        histHeader.setOpaque(false);
        histHeader.add(ThemeUtil.sectionTitle("Sales History"), BorderLayout.WEST);
        JPanel histBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        histBtns.setOpaque(false);
        JButton refreshBtn = ThemeUtil.secondaryButton("Refresh");
        JButton exportBtn  = ThemeUtil.secondaryButton("Export CSV");
        refreshBtn.addActionListener(e -> loadSalesHistory());
        exportBtn.addActionListener(e -> ExportUtils.exportToCSV(this, salesHistoryModel, "sales"));
        histBtns.add(refreshBtn); histBtns.add(exportBtn);
        histHeader.add(histBtns, BorderLayout.EAST);
        histCard.add(histHeader, BorderLayout.NORTH);

        salesHistoryModel = new DefaultTableModel(HISTORY_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable histTable = new JTable(salesHistoryModel);
        ThemeUtil.styleTable(histTable);
        histCard.add(new JScrollPane(histTable), BorderLayout.CENTER);
        add(histCard, BorderLayout.CENTER);
    }

    /** Opens the multi-item sale dialog with optional preselected product. */
    public void showSaleDialog(Product preselectedProduct) {
        cartItems.clear();

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog overlay = new JDialog((Frame) null, false);
        overlay.setUndecorated(true);
        overlay.setBackground(new Color(0, 0, 0, 0));
        if (parentWindow != null) {
            overlay.setSize(parentWindow.getSize());
            overlay.setLocation(parentWindow.getLocationOnScreen());
        }
        JPanel glass = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 140));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        glass.setOpaque(false);
        overlay.setContentPane(glass);
        overlay.setVisible(true);

        JDialog dialog = new JDialog(parentWindow, "New Sale", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(780, 620);
        dialog.setLocationRelativeTo(parentWindow);
        dialog.setLayout(new BorderLayout());
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { overlay.dispose(); }
            @Override public void windowClosing(java.awt.event.WindowEvent e) { overlay.dispose(); }
        });

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeUtil.PRIMARY);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel titleLbl = new JLabel("\uD83D\uDED2  New Sale  —  Add multiple items to cart");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(Color.WHITE);
        header.add(titleLbl);
        dialog.add(header, BorderLayout.NORTH);

        // Main split: left=add item form, right=cart
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
        gbc.insets = new Insets(6, 4, 6, 4);

        List<Product> products = productCtrl.getActiveProducts();
        JComboBox<Product> productCombo = new JComboBox<>(products.toArray(new Product[0]));
        productCombo.setFont(ThemeUtil.FONT_BODY);
        if (preselectedProduct != null) {
            for (int i = 0; i < products.size(); i++) {
                if (products.get(i).getId() == preselectedProduct.getId()) {
                    productCombo.setSelectedIndex(i); break;
                }
            }
        }

        JLabel stockInfoLbl = new JLabel("Stock: —");
        stockInfoLbl.setFont(ThemeUtil.FONT_BOLD);
        stockInfoLbl.setForeground(ThemeUtil.PRIMARY);

        JTextField qtyField      = ThemeUtil.createTextField(8);
        JTextField discountField = ThemeUtil.createTextField(6);
        JTextField vatField      = ThemeUtil.createTextField(6);
        discountField.setText("0");
        vatField.setText("15");

        productCombo.addActionListener(e -> {
            Product p = (Product) productCombo.getSelectedItem();
            if (p != null) stockInfoLbl.setText("Stock: " + p.getQuantity() + "  |  Price: $" + p.getPrice());
        });
        Product initP = (Product) productCombo.getSelectedItem();
        if (initP != null) stockInfoLbl.setText("Stock: " + initP.getQuantity() + "  |  Price: $" + initP.getPrice());

        addRow(addItemCard, gbc, 0, "Product:", productCombo);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        addItemCard.add(stockInfoLbl, gbc);
        gbc.gridwidth = 1;
        addRow(addItemCard, gbc, 2, "Quantity:", qtyField);
        addRow(addItemCard, gbc, 3, "Discount %:", discountField);
        addRow(addItemCard, gbc, 4, "VAT %:", vatField);

        JButton addToCartBtn = ThemeUtil.primaryButton("Add to Cart");
        addToCartBtn.setPreferredSize(new Dimension(0, 36));
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        addItemCard.add(addToCartBtn, gbc);

        mainPanel.add(addItemCard);

        // RIGHT: Cart
        JPanel cartCard = new JPanel(new BorderLayout(0, 8));
        cartCard.setBackground(Color.WHITE);
        cartCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeUtil.BORDER_COLOR),
            new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel cartTitle = new JLabel("Cart Items");
        cartTitle.setFont(ThemeUtil.FONT_HEADING);
        cartTitle.setForeground(ThemeUtil.TEXT_PRIMARY);
        cartCard.add(cartTitle, BorderLayout.NORTH);

        cartModel = new DefaultTableModel(CART_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable cartTable = new JTable(cartModel);
        ThemeUtil.styleTable(cartTable);
        cartCard.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel cartBottom = new JPanel(new BorderLayout(8, 0));
        cartBottom.setOpaque(false);
        cartTotalLabel = new JLabel("Total: $0.00");
        cartTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        cartTotalLabel.setForeground(ThemeUtil.SUCCESS);
        JButton removeBtn = ThemeUtil.dangerButton("Remove");
        removeBtn.setPreferredSize(new Dimension(90, 32));
        removeBtn.addActionListener(e -> {
            int row = cartTable.getSelectedRow();
            if (row >= 0) {
                cartItems.remove(row);
                refreshCart();
            }
        });
        cartBottom.add(cartTotalLabel, BorderLayout.CENTER);
        cartBottom.add(removeBtn, BorderLayout.EAST);
        cartCard.add(cartBottom, BorderLayout.SOUTH);

        mainPanel.add(cartCard);
        dialog.add(mainPanel, BorderLayout.CENTER);

        // Add to cart action
        addToCartBtn.addActionListener(e -> {
            Product p = (Product) productCombo.getSelectedItem();
            if (p == null) { NotificationUtils.showError(dialog, "Select a product."); return; }
            int qty;
            try { qty = Integer.parseInt(qtyField.getText().trim()); if (qty <= 0) throw new NumberFormatException(); }
            catch (NumberFormatException ex) { NotificationUtils.showError(dialog, "Enter a valid quantity."); return; }
            if (p.getQuantity() < qty) {
                NotificationUtils.showWarning(dialog, "Insufficient stock! Available: " + p.getQuantity()); return;
            }
            double disc = 0, vat = 0;
            try { disc = Double.parseDouble(discountField.getText().trim()); } catch (NumberFormatException ignored) {}
            try { vat  = Double.parseDouble(vatField.getText().trim()); }      catch (NumberFormatException ignored) {}
            cartItems.add(new Object[]{p, qty, disc, vat});
            refreshCart();
            qtyField.setText("");
        });

        // Footer: customer + complete
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
            if (cartItems.isEmpty()) { NotificationUtils.showError(dialog, "Cart is empty. Add items first."); return; }
            String customer = customerField.getText().trim();
            if (customer.isBlank()) customer = "Walk-in Customer";

            // Process each cart item as a separate sale record
            int successCount = 0;
            StringBuilder errors = new StringBuilder();
            for (Object[] item : cartItems) {
                Product p   = (Product) item[0];
                int qty     = (int) item[1];
                BigDecimal disc = BigDecimal.valueOf((double) item[2]);
                BigDecimal vat  = BigDecimal.valueOf((double) item[3]);
                String err = saleCtrl.processSale(p.getId(), qty, customer, disc, vat, "");
                if (err == null) successCount++;
                else errors.append(p.getName()).append(": ").append(err).append("\n");
            }

            if (errors.length() == 0) {
                NotificationUtils.showSuccess(AdminSales.this,
                    successCount + " item(s) sold successfully! Total: " + cartTotalLabel.getText());
                overlay.dispose();
                dialog.dispose();
                loadSalesHistory();
            } else {
                NotificationUtils.showError(dialog, "Some items failed:\n" + errors);
                loadSalesHistory();
            }
        });

        footerBtns.add(cancelBtn);
        footerBtns.add(completeBtn);
        footer.add(footerBtns, BorderLayout.EAST);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(ThemeUtil.FONT_BOLD);
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        panel.add(comp, gbc);
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        double grandTotal = 0;
        for (Object[] item : cartItems) {
            Product p   = (Product) item[0];
            int qty     = (int) item[1];
            double disc = (double) item[2];
            double vat  = (double) item[3];
            double sub  = p.getPrice().doubleValue() * qty;
            double discAmt = sub * disc / 100;
            double afterDisc = sub - discAmt;
            double vatAmt = afterDisc * vat / 100;
            double lineTotal = afterDisc + vatAmt;
            grandTotal += lineTotal;
            cartModel.addRow(new Object[]{
                p.getName(), p.getCode(), qty,
                String.format("$%.2f", p.getPrice()),
                disc + "%", vat + "%",
                String.format("$%.2f", lineTotal)
            });
        }
        cartTotalLabel.setText("Total: $" + String.format("%.2f", grandTotal));
    }

    private JLabel addKpiCard(JPanel parent, String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
            new EmptyBorder(14, 14, 14, 14)
        ));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(ThemeUtil.FONT_SMALL);
        titleLbl.setForeground(ThemeUtil.TEXT_MUTED);
        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLbl.setForeground(accent);
        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        parent.add(card);
        return valueLbl;
    }

    private void loadSalesHistory() {
        SwingWorker<List<Sale>, Void> worker = new SwingWorker<>() {
            @Override protected List<Sale> doInBackground() { return saleCtrl.getAllSales(); }
            @Override protected void done() {
                try {
                    List<Sale> sales = get();
                    salesHistoryModel.setRowCount(0);
                    for (Sale s : sales) {
                        salesHistoryModel.addRow(new Object[]{
                            s.getSaleCode(), s.getProductName(), s.getQuantity(),
                            String.format("$%.2f", s.getUnitPrice()),
                            String.format("$%.2f", s.getTotalAmount()),
                            s.getDiscountPercent() != null ? s.getDiscountPercent() + "%" : "0%",
                            s.getVatPercent() != null ? s.getVatPercent() + "%" : "0%",
                            s.getFinalAmount() != null ? String.format("$%.2f", s.getFinalAmount()) : "-",
                            s.getCustomerName(), s.getUsername(), s.getSaleDate()
                        });
                    }
                    totalSalesLabel.setText(String.valueOf(saleCtrl.getTotalSales()));
                    revenueLabel.setText(NumberFormat.getCurrencyInstance(Locale.US).format(saleCtrl.getTotalRevenue()));
                } catch (Exception ex) {
                    NotificationUtils.showError(AdminSales.this, "Failed to load sales.");
                }
            }
        };
        worker.execute();
    }
}
