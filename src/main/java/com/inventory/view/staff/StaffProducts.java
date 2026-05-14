package com.inventory.view.staff;

import com.inventory.controller.CategoryController;
import com.inventory.controller.ProductController;
import com.inventory.controller.SupplierController;
import com.inventory.model.Category;
import com.inventory.model.Product;
import com.inventory.model.Supplier;
import com.inventory.utils.NotificationUtils;
import com.inventory.view.components.ThemeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Staff product view — view all products and add new ones (no edit/delete).
 */
public class StaffProducts extends JPanel {

    private final ProductController productCtrl = new ProductController();
    private final CategoryController categoryCtrl = new CategoryController();
    private final SupplierController supplierCtrl = new SupplierController();

    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;

    private static final String[] COLUMNS = {"Code", "Name", "Category", "Qty", "Price", "Status"};

    public StaffProducts() {
        setLayout(new BorderLayout(0, 12));
        setBackground(ThemeUtil.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        loadProducts();
    }

    private void buildUI() {
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setOpaque(false);

        searchField = ThemeUtil.createTextField(25);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(new JLabel("Search: ")); left.add(searchField);
        toolbar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton addBtn     = ThemeUtil.successButton("+ Add Product");
        JButton refreshBtn = ThemeUtil.secondaryButton("Refresh");
        addBtn.addActionListener(e -> showAddDialog());
        refreshBtn.addActionListener(e -> loadProducts());
        right.add(refreshBtn); right.add(addBtn);
        toolbar.add(right, BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        ThemeUtil.styleTable(table);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        table.getColumnModel().getColumn(5).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
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
    }

    private void loadProducts() {
        SwingWorker<List<Product>, Void> w = new SwingWorker<>() {
            @Override protected List<Product> doInBackground() { return productCtrl.getActiveProducts(); }
            @Override protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (Product p : get()) {
                        tableModel.addRow(new Object[]{p.getCode(), p.getName(),
                                p.getCategoryName(), p.getQuantity(),
                                String.format("$%.2f", p.getPrice()), p.getStockStatus()});
                    }
                } catch (Exception ex) {
                    NotificationUtils.showError(StaffProducts.this, "Failed to load products.");
                }
            }
        };
        w.execute();
    }

    private void filterTable() {
        String text = searchField.getText().trim();
        sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text, 0, 1, 2));
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Add Product", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 440);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField codeField  = ThemeUtil.createTextField(15);
        JTextField nameField  = ThemeUtil.createTextField(15);
        JTextField priceField = ThemeUtil.createTextField(10);
        JTextField reorderField = ThemeUtil.createTextField(5);
        reorderField.setText("10");

        List<Category> categories = categoryCtrl.getActiveCategories();
        JComboBox<Category> catCombo = new JComboBox<>(categories.toArray(new Category[0]));

        List<Supplier> suppliers = supplierCtrl.getAllSuppliers();
        JComboBox<Supplier> supCombo = new JComboBox<>(suppliers.toArray(new Supplier[0]));

        String[] labels = {"Code *:", "Name *:", "Category:", "Supplier:", "Price *:", "Reorder Level:"};
        Component[] inputs = {codeField, nameField, catCombo, supCombo, priceField, reorderField};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.3;
            form.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.7;
            form.add(inputs[i], gbc);
        }

        JButton saveBtn   = ThemeUtil.primaryButton("Save");
        JButton cancelBtn = ThemeUtil.secondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            Product p = new Product();
            p.setCode(codeField.getText().trim());
            p.setName(nameField.getText().trim());
            try { p.setPrice(new BigDecimal(priceField.getText().trim())); }
            catch (NumberFormatException ex) { NotificationUtils.showError(dialog, "Invalid price."); return; }
            try { p.setReorderLevel(Integer.parseInt(reorderField.getText().trim())); }
            catch (NumberFormatException ex) { p.setReorderLevel(10); }
            if (catCombo.getSelectedItem() != null) p.setCategoryId(((Category) catCombo.getSelectedItem()).getId());
            if (supCombo.getSelectedItem() != null) p.setSupplierId(((Supplier) supCombo.getSelectedItem()).getId());
            p.setActive(true);

            String err = productCtrl.addProduct(p);
            if (err == null) {
                NotificationUtils.showSuccess(StaffProducts.this, "Product added.");
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
