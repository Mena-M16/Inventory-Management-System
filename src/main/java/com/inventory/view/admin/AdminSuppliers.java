package com.inventory.view.admin;

import com.inventory.controller.SupplierController;
import com.inventory.model.Supplier;
import com.inventory.utils.NotificationUtils;
import com.inventory.view.components.ThemeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Supplier management panel (Admin/Manager).
 */
public class AdminSuppliers extends JPanel {

    private final SupplierController supplierCtrl = new SupplierController();
    private DefaultTableModel tableModel;
    private JTable table;
    private List<Supplier> currentSuppliers;

    private static final String[] COLUMNS = {"ID", "Name", "Contact Person", "Phone", "Email", "Address"};

    public AdminSuppliers() {
        setLayout(new BorderLayout(0, 12));
        setBackground(ThemeUtil.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        loadSuppliers();
    }

    private void buildUI() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setOpaque(false);

        JButton addBtn    = ThemeUtil.successButton("+ Add Supplier");
        JButton editBtn   = ThemeUtil.primaryButton("Edit");
        JButton deleteBtn = ThemeUtil.dangerButton("Delete");
        JButton refreshBtn = ThemeUtil.secondaryButton("Refresh");

        addBtn.addActionListener(e -> showDialog(null));
        editBtn.addActionListener(e -> editSelected());
        deleteBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> loadSuppliers());

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

    private void loadSuppliers() {
        SwingWorker<List<Supplier>, Void> worker = new SwingWorker<>() {
            @Override protected List<Supplier> doInBackground() { return supplierCtrl.getAllSuppliers(); }
            @Override protected void done() {
                try {
                    currentSuppliers = get();
                    tableModel.setRowCount(0);
                    for (Supplier s : currentSuppliers) {
                        tableModel.addRow(new Object[]{s.getId(), s.getName(), s.getContactPerson(),
                                s.getPhone(), s.getEmail(), s.getAddress()});
                    }
                } catch (Exception ex) {
                    NotificationUtils.showError(AdminSuppliers.this, "Failed to load suppliers.");
                }
            }
        };
        worker.execute();
    }

    private void editSelected() {
        int row = getSelectedModelRow();
        if (row < 0) { NotificationUtils.showWarning(this, "Select a supplier to edit."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        currentSuppliers.stream().filter(s -> s.getId() == id).findFirst().ifPresent(this::showDialog);
    }

    private void deleteSelected() {
        int row = getSelectedModelRow();
        if (row < 0) { NotificationUtils.showWarning(this, "Select a supplier to delete."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        if (!NotificationUtils.showConfirm(this, "Delete supplier \"" + name + "\"?")) return;
        String err = supplierCtrl.deleteSupplier(id);
        if (err == null) { NotificationUtils.showSuccess(this, "Supplier deleted."); loadSuppliers(); }
        else NotificationUtils.showError(this, err);
    }

    private void showDialog(Supplier existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit Supplier" : "Add Supplier", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(420, 360);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JTextField nameField    = ThemeUtil.createTextField(15);
        JTextField contactField = ThemeUtil.createTextField(15);
        JTextField phoneField   = ThemeUtil.createTextField(15);
        JTextField emailField   = ThemeUtil.createTextField(15);
        JTextArea  addrArea     = new JTextArea(3, 15);
        addrArea.setFont(ThemeUtil.FONT_BODY);

        if (isEdit) {
            nameField.setText(existing.getName());
            contactField.setText(existing.getContactPerson());
            phoneField.setText(existing.getPhone());
            emailField.setText(existing.getEmail());
            addrArea.setText(existing.getAddress());
        }

        form.add(new JLabel("Name *:"));          form.add(nameField);
        form.add(new JLabel("Contact Person:"));  form.add(contactField);
        form.add(new JLabel("Phone:"));           form.add(phoneField);
        form.add(new JLabel("Email:"));           form.add(emailField);
        form.add(new JLabel("Address:"));         form.add(new JScrollPane(addrArea));

        JButton saveBtn   = ThemeUtil.primaryButton(isEdit ? "Update" : "Save");
        JButton cancelBtn = ThemeUtil.secondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            Supplier s = isEdit ? existing : new Supplier();
            s.setName(nameField.getText().trim());
            s.setContactPerson(contactField.getText().trim());
            s.setPhone(phoneField.getText().trim());
            s.setEmail(emailField.getText().trim());
            s.setAddress(addrArea.getText().trim());

            String err = isEdit ? supplierCtrl.updateSupplier(s) : supplierCtrl.addSupplier(s);
            if (err == null) {
                NotificationUtils.showSuccess(AdminSuppliers.this, isEdit ? "Supplier updated." : "Supplier added.");
                dialog.dispose();
                loadSuppliers();
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
