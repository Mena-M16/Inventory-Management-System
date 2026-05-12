package com.inventory.view.admin;

import com.inventory.controller.CategoryController;
import com.inventory.model.Category;
import com.inventory.utils.NotificationUtils;
import com.inventory.view.components.ThemeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Category management panel (Admin/Manager).
 */
public class AdminCategories extends JPanel {

    private final CategoryController categoryCtrl = new CategoryController();
    private DefaultTableModel tableModel;
    private JTable table;
    private List<Category> currentCategories;

    private static final String[] COLUMNS = {"ID", "Name", "Description", "Active", "Created"};

    public AdminCategories() {
        setLayout(new BorderLayout(0, 12));
        setBackground(ThemeUtil.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        loadCategories();
    }

    private void buildUI() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setOpaque(false);

        JButton addBtn    = ThemeUtil.successButton("+ Add Category");
        JButton editBtn   = ThemeUtil.primaryButton("Edit");
        JButton deleteBtn = ThemeUtil.dangerButton("Deactivate");
        JButton refreshBtn = ThemeUtil.secondaryButton("Refresh");

        addBtn.addActionListener(e -> showDialog(null));
        editBtn.addActionListener(e -> editSelected());
        deleteBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> loadCategories());

        toolbar.add(refreshBtn); toolbar.add(addBtn); toolbar.add(editBtn); toolbar.add(deleteBtn);
        add(toolbar, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        ThemeUtil.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadCategories() {
        SwingWorker<List<Category>, Void> worker = new SwingWorker<>() {
            @Override protected List<Category> doInBackground() { return categoryCtrl.getAllCategories(); }
            @Override protected void done() {
                try {
                    currentCategories = get();
                    tableModel.setRowCount(0);
                    for (Category c : currentCategories) {
                        tableModel.addRow(new Object[]{c.getId(), c.getName(), c.getDescription(),
                                c.isActive() ? "Yes" : "No", c.getCreatedAt()});
                    }
                } catch (Exception ex) {
                    NotificationUtils.showError(AdminCategories.this, "Failed to load categories.");
                }
            }
        };
        worker.execute();
    }

    private void editSelected() {
        int row = getSelectedModelRow();
        if (row < 0) { NotificationUtils.showWarning(this, "Select a category to edit."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        currentCategories.stream().filter(c -> c.getId() == id).findFirst().ifPresent(this::showDialog);
    }

    private void deleteSelected() {
        int row = getSelectedModelRow();
        if (row < 0) { NotificationUtils.showWarning(this, "Select a category to deactivate."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        if (!NotificationUtils.showConfirm(this, "Deactivate category \"" + name + "\"?")) return;
        String err = categoryCtrl.deleteCategory(id);
        if (err == null) { NotificationUtils.showSuccess(this, "Category deactivated."); loadCategories(); }
        else NotificationUtils.showError(this, err);
    }

    private void showDialog(Category existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit Category" : "Add Category", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(380, 260);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JTextField nameField = ThemeUtil.createTextField(15);
        JTextArea  descArea  = new JTextArea(3, 15);
        descArea.setFont(ThemeUtil.FONT_BODY);
        JCheckBox activeCheck = new JCheckBox("Active", true);

        if (isEdit) {
            nameField.setText(existing.getName());
            descArea.setText(existing.getDescription());
            activeCheck.setSelected(existing.isActive());
        }

        form.add(new JLabel("Name *:"));      form.add(nameField);
        form.add(new JLabel("Description:")); form.add(new JScrollPane(descArea));
        form.add(new JLabel("Status:"));      form.add(activeCheck);

        JButton saveBtn   = ThemeUtil.primaryButton(isEdit ? "Update" : "Save");
        JButton cancelBtn = ThemeUtil.secondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            Category c = isEdit ? existing : new Category();
            c.setName(nameField.getText().trim());
            c.setDescription(descArea.getText().trim());
            c.setActive(activeCheck.isSelected());

            String err = isEdit ? categoryCtrl.updateCategory(c) : categoryCtrl.addCategory(c);
            if (err == null) {
                NotificationUtils.showSuccess(AdminCategories.this, isEdit ? "Category updated." : "Category added.");
                dialog.dispose();
                loadCategories();
            } else {
                NotificationUtils.showError(dialog, err);
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.add(cancelBtn); btnRow.add(saveBtn);

        dialog.setLayout(new BorderLayout());
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnRow, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private int getSelectedModelRow() {
        int row = table.getSelectedRow();
        return row < 0 ? -1 : table.convertRowIndexToModel(row);
    }
}
