package com.inventory.view.admin;

import com.inventory.controller.CategoryController;
import com.inventory.controller.ProductController;
import com.inventory.controller.SupplierController;
import com.inventory.model.Category;
import com.inventory.model.Product;
import com.inventory.model.Supplier;
import com.inventory.utils.ExportUtils;
import com.inventory.utils.NotificationUtils;
import com.inventory.view.components.ThemeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Admin product management panel with full CRUD.
 */
public class AdminProducts extends JPanel {

    private final ProductController productCtrl = new ProductController();
    private final CategoryController categoryCtrl = new CategoryController();
    private final SupplierController supplierCtrl = new SupplierController();

    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private List<Product> currentProducts;

    private static final String[] COLUMNS = {"ID", "Code", "Name", "Category", "Supplier", "Qty", "Price", "Status"};

    public AdminProducts() {
        setLayout(new BorderLayout(0, 12));
        setBackground(ThemeUtil.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        loadProducts();
    }

    private void buildUI() {
        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setOpaque(false);

        searchField = ThemeUtil.createTextField(25);
        searchField.putClientProperty("JTextField.placeholderText", "Search by name, code or category...");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);
        toolbar.add(searchPanel, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        JButton addBtn    = ThemeUtil.successButton("+ Add Product");
        JButton editBtn   = ThemeUtil.primaryButton("Edit");
        JButton deleteBtn = ThemeUtil.dangerButton("Delete");
        JButton exportBtn = ThemeUtil.secondaryButton("Export CSV");
        JButton refreshBtn = ThemeUtil.secondaryButton("Refresh");

        addBtn.addActionListener(e -> showProductDialog(null));
        editBtn.addActionListener(e -> editSelected());
        deleteBtn.addActionListener(e -> deleteSelected());
        exportBtn.addActionListener(e -> ExportUtils.exportToCSV(this, tableModel, "products"));
        refreshBtn.addActionListener(e -> loadProducts());

        btnPanel.add(refreshBtn); btnPanel.add(exportBtn);
        btnPanel.add(addBtn); btnPanel.add(editBtn); btnPanel.add(deleteBtn);
        toolbar.add(btnPanel, BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        ThemeUtil.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setMaxWidth(60);
        table.getColumnModel().getColumn(6).setMaxWidth(80);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Colour-code status column
        table.getColumnModel().getColumn(7).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                String status = value == null ? "" : value.toString();
                setForeground(ThemeUtil.stockStatusColor(status));
                setFont(ThemeUtil.FONT_BOLD);
                return this;
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        // F5 refresh
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F5"), "refresh");
        getActionMap().put("refresh", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { loadProducts(); }
        });
    }

    private void loadProducts() {
        SwingWorker<List<Product>, Void> worker = new SwingWorker<>() {
            @Override protected List<Product> doInBackground() { return productCtrl.getAllProducts(); }
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

    /** Public refresh method callable from MainWindow F5. */
    public void refresh() { loadProducts(); }

    private void populateTable(List<Product> products) {
        tableModel.setRowCount(0);
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                p.getId(), p.getCode(), p.getName(),
                p.getCategoryName(), p.getSupplierName(),
                p.getQuantity(), String.format("$%.2f", p.getPrice()),
                p.getStockStatus()
            });
        }
    }

    private void filterTable() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1, 2, 3));
        }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { NotificationUtils.showWarning(this, "Select a product to edit."); return; }
        int id = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
        Product p = productCtrl.getProductById(id);
        if (p != null) showProductDialog(p);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { NotificationUtils.showWarning(this, "Select a product to delete."); return; }
        int id = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
        String name = (String) tableModel.getValueAt(table.convertRowIndexToModel(row), 2);
        if (!NotificationUtils.showConfirm(this, "Delete product \"" + name + "\"?")) return;
        String err = productCtrl.deleteProduct(id);
        if (err == null) { NotificationUtils.showSuccess(this, "Product deleted."); loadProducts(); }
        else NotificationUtils.showError(this, err);
    }

    private void showProductDialog(Product existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit Product" : "Add Product", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(520, 560);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField codeField = ThemeUtil.createTextField(15);
        JTextField nameField = ThemeUtil.createTextField(15);
        JTextArea descArea = new JTextArea(3, 15);
        descArea.setFont(ThemeUtil.FONT_BODY);
        JTextField priceField = ThemeUtil.createTextField(10);
        JTextField costField = ThemeUtil.createTextField(10);
        JTextField reorderField = ThemeUtil.createTextField(5);
        JTextField locationField = ThemeUtil.createTextField(10);

        List<Category> categories = categoryCtrl.getActiveCategories();
        JComboBox<Category> catCombo = new JComboBox<>(categories.toArray(new Category[0]));

        List<Supplier> suppliers = supplierCtrl.getAllSuppliers();
        JComboBox<Supplier> supCombo = new JComboBox<>(suppliers.toArray(new Supplier[0]));

        if (isEdit) {
            codeField.setText(existing.getCode());
            nameField.setText(existing.getName());
            descArea.setText(existing.getDescription());
            priceField.setText(existing.getPrice() != null ? existing.getPrice().toPlainString() : "");
            costField.setText(existing.getCostPrice() != null ? existing.getCostPrice().toPlainString() : "");
            reorderField.setText(String.valueOf(existing.getReorderLevel()));
            locationField.setText(existing.getLocation());
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getId() == existing.getCategoryId()) { catCombo.setSelectedIndex(i); break; }
            }
            for (int i = 0; i < suppliers.size(); i++) {
                if (suppliers.get(i).getId() == existing.getSupplierId()) { supCombo.setSelectedIndex(i); break; }
            }
        }

        String[][] fields = {{"Code *", null}, {"Name *", null}, {"Description", null},
                {"Category", null}, {"Supplier", null}, {"Price *", null},
                {"Cost Price", null}, {"Reorder Level", null}, {"Location", null}};
        Component[] inputs = {codeField, nameField, new JScrollPane(descArea),
                catCombo, supCombo, priceField, costField, reorderField, locationField};

        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.3;
            form.add(new JLabel(fields[i][0]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.7;
            form.add(inputs[i], gbc);
        }

        JButton saveBtn = ThemeUtil.primaryButton(isEdit ? "Update" : "Save");
        JButton cancelBtn = ThemeUtil.secondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

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
                dialog.dispose();
                loadProducts();
            } else {
                NotificationUtils.showError(dialog, err);
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.add(cancelBtn); btnRow.add(saveBtn);

        dialog.setLayout(new BorderLayout());
        dialog.add(new JScrollPane(form), BorderLayout.CENTER);
        dialog.add(btnRow, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
