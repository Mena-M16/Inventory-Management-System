package com.inventory.view.admin;

import com.inventory.controller.CategoryController;
import com.inventory.controller.ProductController;
import com.inventory.controller.StockController;
import com.inventory.controller.SupplierController;
import com.inventory.model.Category;
import com.inventory.model.Product;
import com.inventory.model.Supplier;
import com.inventory.utils.ExportUtils;
import com.inventory.utils.NotificationUtils;
import com.inventory.view.components.PaginatedTable;
import com.inventory.view.components.ThemeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Admin product management panel with full CRUD and improved UI.
 */
public class AdminProducts extends JPanel {

    private final ProductController productCtrl = new ProductController();
    private final CategoryController categoryCtrl = new CategoryController();
    private final SupplierController supplierCtrl = new SupplierController();
    private final StockController stockCtrl = new StockController();

    private PaginatedTable paginatedTable;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private List<Product> currentProducts;

    private static final String[] COLUMNS = {"Code", "Name", "Category", "Supplier", "Qty", "Price", "Status"};

    public AdminProducts() {
        setLayout(new BorderLayout(0, 12));
        setBackground(ThemeUtil.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        loadProducts();
    }

    private void buildUI() {
        // Toolbar - search left, buttons right, with clear separation
        JPanel toolbar = new JPanel(new BorderLayout(20, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        // Search on the left - fixed width
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        searchPanel.setOpaque(false);
        JLabel searchIcon = new JLabel("\uD83D\uDD0D");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        searchField = new JTextField(22);
        searchField.setFont(ThemeUtil.FONT_BODY);
        searchField.setPreferredSize(new Dimension(160, 36));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeUtil.BORDER_COLOR),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });
        searchPanel.add(searchIcon);
        searchPanel.add(searchField);
        toolbar.add(searchPanel, BorderLayout.WEST);

        // Buttons on the right
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        JButton addBtn     = ThemeUtil.successButton("+ Product");
        JButton addStockBtn = ThemeUtil.primaryButton("+ Stock");
        JButton sellBtn    = ThemeUtil.warningButton("Sell");
        JButton editBtn    = ThemeUtil.primaryButton("Edit");
        JButton deleteBtn  = ThemeUtil.dangerButton("Delete");
        JButton exportBtn  = ThemeUtil.secondaryButton("Export");
        JButton refreshBtn = ThemeUtil.secondaryButton("Refresh");

        // Make all buttons same compact size
        Dimension btnSize = new Dimension(85, 32);
        addBtn.setPreferredSize(btnSize);
        addStockBtn.setPreferredSize(btnSize);
        sellBtn.setPreferredSize(btnSize);
        editBtn.setPreferredSize(btnSize);
        deleteBtn.setPreferredSize(btnSize);
        exportBtn.setPreferredSize(btnSize);
        refreshBtn.setPreferredSize(btnSize);

        addBtn.addActionListener(e -> showProductDialog(null));
        addStockBtn.addActionListener(e -> showAddStockDialog());
        sellBtn.addActionListener(e -> navigateToSales());
        editBtn.addActionListener(e -> editSelected());
        deleteBtn.addActionListener(e -> deleteSelected());
        exportBtn.addActionListener(e -> ExportUtils.exportToCSV(this, tableModel, "products"));
        refreshBtn.addActionListener(e -> loadProducts());

        btnPanel.add(refreshBtn);
        btnPanel.add(exportBtn);
        btnPanel.add(addBtn);
        btnPanel.add(addStockBtn);
        btnPanel.add(sellBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        toolbar.add(btnPanel, BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        ThemeUtil.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        // Paginated table
        paginatedTable = new PaginatedTable(COLUMNS);
        paginatedTable.setColumnPreferredWidth(0, 80);
        paginatedTable.setColumnPreferredWidth(1, 180);
        paginatedTable.setColumnMaxWidth(4, 70);
        paginatedTable.setColumnMaxWidth(5, 90);
        table = paginatedTable.getTable();
        tableModel = paginatedTable.getModel();

        // Status column badge renderer
        paginatedTable.setColumnRenderer(6, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                JLabel lbl = new JLabel(value == null ? "" : value.toString());
                lbl.setFont(ThemeUtil.FONT_BOLD);
                lbl.setOpaque(true);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                String status = value == null ? "" : value.toString();
                if (!sel) {
                    lbl.setBackground(ThemeUtil.stockStatusBg(status));
                    lbl.setForeground(ThemeUtil.stockStatusColor(status));
                } else {
                    lbl.setBackground(ThemeUtil.PRIMARY_LIGHT);
                    lbl.setForeground(ThemeUtil.stockStatusColor(status));
                }
                lbl.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return lbl;
            }
        });

        add(paginatedTable, BorderLayout.CENTER);

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F5"), "refresh");
        getActionMap().put("refresh", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { loadProducts(); }
        });
    }

    private void loadProducts() {
        SwingWorker<List<Product>, Void> worker = new SwingWorker<>() {
            @Override protected List<Product> doInBackground() { return productCtrl.getActiveProducts(); }
            @Override protected void done() {
                try {
                    currentProducts = get();
                    populateTable(currentProducts);
                } catch (Exception ex) {
                    NotificationUtils.showError(AdminProducts.this, "Failed to load products.");
                }
            }
        };
        worker.execute();
    }

    public void refresh() { loadProducts(); }

    private String generateNextCode() {
        if (currentProducts == null || currentProducts.isEmpty()) return "P001";
        int maxNum = 0;
        for (Product p : currentProducts) {
            String code = p.getCode();
            if (code != null && code.matches("P\\d+")) {
                try {
                    int num = Integer.parseInt(code.substring(1));
                    if (num > maxNum) maxNum = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("P%03d", maxNum + 1);
    }

    private void populateTable(List<Product> products) {
        paginatedTable.clearData();
        for (Product p : products) {
            paginatedTable.addRow(new Object[]{
                p.getCode(), p.getName(),
                p.getCategoryName(), p.getSupplierName(),
                p.getQuantity(), String.format("$%.2f", p.getPrice()),
                p.getStockStatus()
            });
        }
        paginatedTable.renderPage();
    }

    private void filterTable() {
        String text = searchField.getText().trim();
        paginatedTable.setFilter(text, 0, 1, 2);
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { NotificationUtils.showWarning(this, "Select a product to edit."); return; }
        int modelRow = table.convertRowIndexToModel(row);
        if (modelRow < currentProducts.size()) showProductDialog(currentProducts.get(modelRow));
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { NotificationUtils.showWarning(this, "Select a product to delete."); return; }
        int modelRow = table.convertRowIndexToModel(row);
        if (modelRow >= currentProducts.size()) return;
        Product p = currentProducts.get(modelRow);
        if (!NotificationUtils.showConfirm(this, "Delete product \"" + p.getName() + "\"?")) return;
        String err = productCtrl.deleteProduct(p.getId());
        if (err == null) { NotificationUtils.showSuccess(this, "Product deleted."); loadProducts(); }
        else NotificationUtils.showError(this, err);
    }

    private void showProductDialog(Product existing) {
        boolean isEdit = existing != null;
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog overlay = ThemeUtil.showBlurOverlay(parentWindow);

        JDialog dialog = new JDialog(parentWindow,
                isEdit ? "Edit Product" : "Add New Product", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(620, 620);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { overlay.dispose(); }
            @Override public void windowClosing(java.awt.event.WindowEvent e) { overlay.dispose(); }
        });

        // Header bar
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeUtil.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        JLabel headerTitle = new JLabel(isEdit ? "\u270F  Edit Product" : "\u2795  Add New Product");
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerTitle.setForeground(Color.WHITE);
        JLabel headerSub = new JLabel(isEdit ? "Update product information" : "Fill in the details to add a new product");
        headerSub.setFont(ThemeUtil.FONT_SMALL);
        headerSub.setForeground(new Color(200, 210, 255));
        JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 2));
        headerText.setOpaque(false);
        headerText.add(headerTitle);
        headerText.add(headerSub);
        header.add(headerText, BorderLayout.CENTER);
        dialog.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        JTextField codeField    = ThemeUtil.createTextField(15);
        JTextField nameField    = ThemeUtil.createTextField(15);
        JTextArea  descArea     = new JTextArea(3, 15);
        descArea.setFont(ThemeUtil.FONT_BODY);
        descArea.setLineWrap(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeUtil.BORDER_COLOR),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        JTextField priceField   = ThemeUtil.createTextField(10);
        JTextField costField    = ThemeUtil.createTextField(10);
        JTextField reorderField = ThemeUtil.createTextField(5);
        JTextField locationField = ThemeUtil.createTextField(10);

        List<Category> categories = categoryCtrl.getActiveCategories();
        JComboBox<Category> catCombo = new JComboBox<>(categories.toArray(new Category[0]));
        catCombo.setFont(ThemeUtil.FONT_BODY);

        List<Supplier> suppliers = supplierCtrl.getAllSuppliers();
        JComboBox<Supplier> supCombo = new JComboBox<>(suppliers.toArray(new Supplier[0]));
        supCombo.setFont(ThemeUtil.FONT_BODY);

        if (!isEdit) {
            codeField.setText(generateNextCode());
            codeField.setEditable(false);
            codeField.setBackground(new Color(248, 250, 252));
            reorderField.setText("10");
        } else {
            codeField.setText(existing.getCode());
            nameField.setText(existing.getName());
            descArea.setText(existing.getDescription());
            priceField.setText(existing.getPrice() != null ? existing.getPrice().toPlainString() : "");
            costField.setText(existing.getCostPrice() != null ? existing.getCostPrice().toPlainString() : "");
            reorderField.setText(String.valueOf(existing.getReorderLevel()));
            locationField.setText(existing.getLocation());
            for (int i = 0; i < categories.size(); i++)
                if (categories.get(i).getId() == existing.getCategoryId()) { catCombo.setSelectedIndex(i); break; }
            for (int i = 0; i < suppliers.size(); i++)
                if (suppliers.get(i).getId() == existing.getSupplierId()) { supCombo.setSelectedIndex(i); break; }
        }

        // Two-column layout
        Object[][] rows = {
            {"Product Code", codeField,    "Product Name *", nameField},
            {"Category",     catCombo,     "Supplier",       supCombo},
            {"Selling Price *", priceField,"Cost Price",     costField},
            {"Reorder Level",reorderField, "Location",       locationField},
        };

        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < 4; c += 2) {
                gbc.gridx = c; gbc.gridy = r * 2; gbc.weightx = 0.5;
                JLabel lbl = new JLabel((String) rows[r][c]);
                lbl.setFont(ThemeUtil.FONT_BOLD);
                lbl.setForeground(ThemeUtil.TEXT_PRIMARY);
                form.add(lbl, gbc);
                gbc.gridy = r * 2 + 1;
                form.add((Component) rows[r][c + 1], gbc);
            }
        }

        // Description full width
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 4; gbc.weightx = 1.0;
        JLabel descLbl = new JLabel("Description");
        descLbl.setFont(ThemeUtil.FONT_BOLD);
        descLbl.setForeground(ThemeUtil.TEXT_PRIMARY);
        form.add(descLbl, gbc);
        gbc.gridy = 9;
        form.add(new JScrollPane(descArea), gbc);

        dialog.add(new JScrollPane(form) {{ setBorder(null); }}, BorderLayout.CENTER);

        // Footer buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(new Color(248, 250, 252));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeUtil.BORDER_COLOR));
        JButton cancelBtn = ThemeUtil.secondaryButton("Cancel");
        JButton saveBtn   = ThemeUtil.primaryButton(isEdit ? "Update Product" : "Save Product");
        saveBtn.setPreferredSize(new Dimension(150, 36));
        cancelBtn.addActionListener(e -> { overlay.dispose(); dialog.dispose(); });

        saveBtn.addActionListener(e -> {
            Product p = isEdit ? existing : new Product();
            p.setCode(codeField.getText().trim());
            p.setName(nameField.getText().trim());
            p.setDescription(descArea.getText().trim());
            try { p.setPrice(new BigDecimal(priceField.getText().trim())); }
            catch (NumberFormatException ex) { NotificationUtils.showError(dialog, "Invalid price."); return; }
            try { if (!costField.getText().isBlank()) p.setCostPrice(new BigDecimal(costField.getText().trim())); }
            catch (NumberFormatException ex) { NotificationUtils.showError(dialog, "Invalid cost price."); return; }
            try { p.setReorderLevel(Integer.parseInt(reorderField.getText().trim())); }
            catch (NumberFormatException ex) { p.setReorderLevel(10); }
            p.setLocation(locationField.getText().trim());
            if (catCombo.getSelectedItem() != null) p.setCategoryId(((Category) catCombo.getSelectedItem()).getId());
            if (supCombo.getSelectedItem() != null) p.setSupplierId(((Supplier) supCombo.getSelectedItem()).getId());
            p.setActive(true);

            String err = isEdit ? productCtrl.updateProduct(p) : productCtrl.addProduct(p);
            if (err == null) {
                NotificationUtils.showSuccess(AdminProducts.this, isEdit ? "Product updated." : "Product added.");
                overlay.dispose();
                dialog.dispose();
                loadProducts();
            } else {
                NotificationUtils.showError(dialog, err);
            }
        });

        footer.add(cancelBtn);
        footer.add(saveBtn);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /** Navigates to the Sales page by firing an action up to MainWindow. */
    private void navigateToSales() {
        // Walk up the component tree to find MainWindow and trigger showPanel("sales")
        Container parent = getParent();
        while (parent != null) {
            if (parent instanceof com.inventory.view.MainWindow) {
                ((com.inventory.view.MainWindow) parent).showSalesPanel();
                return;
            }
            parent = parent.getParent();
        }
    }

    /** Shows Add Stock dialog — records a Stock IN transaction for selected product. */
    private void showAddStockDialog() {
        int row = table.getSelectedRow();
        Product preselected = null;
        if (row >= 0) {
            int modelRow = table.convertRowIndexToModel(row);
            if (modelRow < currentProducts.size()) preselected = currentProducts.get(modelRow);
        }

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog stockOverlay = ThemeUtil.showBlurOverlay(parentWindow);

        JDialog dialog = new JDialog(parentWindow, "Add Stock", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(420, 340);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { stockOverlay.dispose(); }
            @Override public void windowClosing(java.awt.event.WindowEvent e) { stockOverlay.dispose(); }
        });

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeUtil.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel title = new JLabel("\uD83D\uDCE6  Add Stock (Stock IN)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(Color.WHITE);
        header.add(title);
        dialog.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 4, 8, 4);

        JComboBox<Product> productCombo = new JComboBox<>(
            currentProducts != null ? currentProducts.toArray(new Product[0]) : new Product[0]);
        productCombo.setFont(ThemeUtil.FONT_BODY);
        if (preselected != null) {
            for (int i = 0; i < currentProducts.size(); i++) {
                if (currentProducts.get(i).getId() == preselected.getId()) {
                    productCombo.setSelectedIndex(i); break;
                }
            }
        }

        JTextField qtyField = ThemeUtil.createTextField(10);
        JTextField notesField = ThemeUtil.createTextField(15);

        JLabel stockLbl = new JLabel();
        stockLbl.setFont(ThemeUtil.FONT_BOLD);
        productCombo.addActionListener(e -> {
            Product p = (Product) productCombo.getSelectedItem();
            if (p != null) stockLbl.setText("Current Stock: " + p.getQuantity() + " units");
        });
        Product sel = (Product) productCombo.getSelectedItem();
        if (sel != null) stockLbl.setText("Current Stock: " + sel.getQuantity() + " units");
        stockLbl.setForeground(ThemeUtil.PRIMARY);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        form.add(new JLabel("Product:") {{ setFont(ThemeUtil.FONT_BOLD); }}, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        form.add(productCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        form.add(stockLbl, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.35;
        form.add(new JLabel("Quantity:") {{ setFont(ThemeUtil.FONT_BOLD); }}, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        form.add(qtyField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.35;
        form.add(new JLabel("Notes:") {{ setFont(ThemeUtil.FONT_BOLD); }}, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        form.add(notesField, gbc);

        dialog.add(form, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(new Color(248, 250, 252));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeUtil.BORDER_COLOR));
        JButton cancelBtn = ThemeUtil.secondaryButton("Cancel");
        JButton addBtn    = ThemeUtil.primaryButton("Add Stock");
        cancelBtn.addActionListener(e -> dialog.dispose());
        addBtn.addActionListener(e -> {
            Product p = (Product) productCombo.getSelectedItem();
            if (p == null) { NotificationUtils.showError(dialog, "Select a product."); return; }
            int qty;
            try { qty = Integer.parseInt(qtyField.getText().trim()); if (qty <= 0) throw new NumberFormatException(); }
            catch (NumberFormatException ex) { NotificationUtils.showError(dialog, "Enter a valid quantity."); return; }
            String err = stockCtrl.processStock(p.getId(), "IN", qty, notesField.getText().trim());
            if (err == null) {
                NotificationUtils.showSuccess(AdminProducts.this, "Stock added: +" + qty + " units for " + p.getName());
                dialog.dispose();
                loadProducts();
            } else {
                NotificationUtils.showError(dialog, err);
            }
        });
        footer.add(cancelBtn);
        footer.add(addBtn);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

}
