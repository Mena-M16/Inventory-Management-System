package com.inventory.view.admin;

import com.inventory.controller.UserController;
import com.inventory.model.User;
import com.inventory.utils.NotificationUtils;
import com.inventory.view.components.ThemeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Admin user management panel.
 */
public class AdminUsers extends JPanel {

    private final UserController userCtrl = new UserController();
    private DefaultTableModel tableModel;
    private JTable table;
    private List<User> currentUsers;

    private static final String[] COLUMNS = {"ID", "Username", "Full Name", "Email", "Phone", "Role", "Active", "Last Login"};

    public AdminUsers() {
        setLayout(new BorderLayout(0, 12));
        setBackground(ThemeUtil.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        loadUsers();
    }

    private void buildUI() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setOpaque(false);

        JButton addBtn    = ThemeUtil.successButton("+ Add User");
        JButton editBtn   = ThemeUtil.primaryButton("Edit");
        JButton deleteBtn = ThemeUtil.dangerButton("Deactivate");
        JButton resetBtn  = ThemeUtil.warningButton("Reset Password");
        JButton refreshBtn = ThemeUtil.secondaryButton("Refresh");

        addBtn.addActionListener(e -> showUserDialog(null));
        editBtn.addActionListener(e -> editSelected());
        deleteBtn.addActionListener(e -> deleteSelected());
        resetBtn.addActionListener(e -> resetPassword());
        refreshBtn.addActionListener(e -> loadUsers());

        toolbar.add(refreshBtn); toolbar.add(addBtn); toolbar.add(editBtn);
        toolbar.add(resetBtn); toolbar.add(deleteBtn);
        add(toolbar, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        ThemeUtil.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadUsers() {
        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            @Override protected List<User> doInBackground() { return userCtrl.getAllUsers(); }
            @Override protected void done() {
                try {
                    currentUsers = get();
                    tableModel.setRowCount(0);
                    for (User u : currentUsers) {
                        tableModel.addRow(new Object[]{
                            u.getId(), u.getUsername(), u.getFullname(), u.getEmail(),
                            u.getPhone(), u.getRole(), u.isActive() ? "Yes" : "No",
                            u.getLastLogin()
                        });
                    }
                } catch (Exception ex) {
                    NotificationUtils.showError(AdminUsers.this, "Failed to load users.");
                }
            }
        };
        worker.execute();
    }

    /** Public refresh callable from MainWindow F5. */
    public void refresh() { loadUsers(); }

    private void editSelected() {
        int row = getSelectedModelRow();
        if (row < 0) { NotificationUtils.showWarning(this, "Select a user to edit."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        User u = currentUsers.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
        if (u != null) showUserDialog(u);
    }

    private void deleteSelected() {
        int row = getSelectedModelRow();
        if (row < 0) { NotificationUtils.showWarning(this, "Select a user to deactivate."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 2);
        if (!NotificationUtils.showConfirm(this, "Deactivate user \"" + name + "\"?")) return;
        String err = userCtrl.deleteUser(id);
        if (err == null) { NotificationUtils.showSuccess(this, "User deactivated."); loadUsers(); }
        else NotificationUtils.showError(this, err);
    }

    private void resetPassword() {
        int row = getSelectedModelRow();
        if (row < 0) { NotificationUtils.showWarning(this, "Select a user."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        String newPass = JOptionPane.showInputDialog(this, "Enter new password (min 6 chars):", "Reset Password", JOptionPane.PLAIN_MESSAGE);
        if (newPass == null || newPass.isBlank()) return;
        String err = userCtrl.resetPassword(id, newPass);
        if (err == null) NotificationUtils.showSuccess(this, "Password reset successfully.");
        else NotificationUtils.showError(this, err);
    }

    private void showUserDialog(User existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit User" : "Add User", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(420, 400);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JTextField usernameField = ThemeUtil.createTextField(15);
        JTextField fullnameField = ThemeUtil.createTextField(15);
        JTextField emailField    = ThemeUtil.createTextField(15);
        JTextField phoneField    = ThemeUtil.createTextField(15);
        JPasswordField passField = new JPasswordField(15);
        passField.setFont(ThemeUtil.FONT_BODY);
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"ADMIN", "MANAGER", "STAFF"});
        JCheckBox activeCheck = new JCheckBox("Active", true);

        if (isEdit) {
            usernameField.setText(existing.getUsername());
            fullnameField.setText(existing.getFullname());
            emailField.setText(existing.getEmail());
            phoneField.setText(existing.getPhone());
            roleCombo.setSelectedItem(existing.getRole());
            activeCheck.setSelected(existing.isActive());
            passField.setEnabled(false);
            passField.setText("(unchanged)");
        }

        form.add(new JLabel("Username *:")); form.add(usernameField);
        form.add(new JLabel("Full Name *:")); form.add(fullnameField);
        form.add(new JLabel("Email:"));      form.add(emailField);
        form.add(new JLabel("Phone:"));      form.add(phoneField);
        if (!isEdit) { form.add(new JLabel("Password *:")); form.add(passField); }
        form.add(new JLabel("Role:"));       form.add(roleCombo);
        form.add(new JLabel("Status:"));     form.add(activeCheck);

        JButton saveBtn   = ThemeUtil.primaryButton(isEdit ? "Update" : "Save");
        JButton cancelBtn = ThemeUtil.secondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            User u = isEdit ? existing : new User();
            u.setUsername(usernameField.getText().trim());
            u.setFullname(fullnameField.getText().trim());
            u.setEmail(emailField.getText().trim());
            u.setPhone(phoneField.getText().trim());
            u.setRole((String) roleCombo.getSelectedItem());
            u.setActive(activeCheck.isSelected());

            String err;
            if (isEdit) {
                err = userCtrl.updateUser(u);
            } else {
                err = userCtrl.addUser(u, new String(passField.getPassword()));
            }
            if (err == null) {
                NotificationUtils.showSuccess(AdminUsers.this, isEdit ? "User updated." : "User created.");
                dialog.dispose();
                loadUsers();
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
