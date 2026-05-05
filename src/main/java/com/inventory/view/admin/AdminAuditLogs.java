package com.inventory.view.admin;

import com.inventory.dao.DatabaseConnection;
import com.inventory.utils.ExportUtils;
import com.inventory.utils.NotificationUtils;
import com.inventory.view.components.ThemeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Audit log viewer for Admin users.
 */
public class AdminAuditLogs extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(AdminAuditLogs.class.getName());
    private DefaultTableModel tableModel;
    private JTextField filterField;

    private static final String[] COLUMNS = {"ID", "User", "Action", "Details", "IP", "Timestamp"};

    public AdminAuditLogs() {
        setLayout(new BorderLayout(0, 12));
        setBackground(ThemeUtil.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        loadLogs(null);
    }

    private void buildUI() {
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setOpaque(false);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);
        filterField = ThemeUtil.createTextField(20);
        JButton filterBtn = ThemeUtil.primaryButton("Filter");
        filterBtn.addActionListener(e -> loadLogs(filterField.getText().trim()));
        leftPanel.add(new JLabel("Filter by user/action:"));
        leftPanel.add(filterField);
        leftPanel.add(filterBtn);
        toolbar.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);
        JButton exportBtn  = ThemeUtil.secondaryButton("Export CSV");
        JButton refreshBtn = ThemeUtil.secondaryButton("Refresh");
        exportBtn.addActionListener(e -> ExportUtils.exportToCSV(this, tableModel, "audit_logs"));
        refreshBtn.addActionListener(e -> loadLogs(filterField.getText().trim()));
        rightPanel.add(exportBtn); rightPanel.add(refreshBtn);
        toolbar.add(rightPanel, BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        ThemeUtil.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(3).setPreferredWidth(300);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadLogs(String filter) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                String sql = "SELECT id, username, action, details, ip_address, created_at FROM audit_logs";
                if (filter != null && !filter.isBlank()) {
                    sql += " WHERE username LIKE ? OR action LIKE ?";
                }
                sql += " ORDER BY created_at DESC LIMIT 500";

                Connection conn = null;
                try {
                    conn = DatabaseConnection.getInstance().getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql);
                    if (filter != null && !filter.isBlank()) {
                        String kw = "%" + filter + "%";
                        ps.setString(1, kw); ps.setString(2, kw);
                    }
                    ResultSet rs = ps.executeQuery();
                    tableModel.setRowCount(0);
                    while (rs.next()) {
                        tableModel.addRow(new Object[]{
                            rs.getInt("id"), rs.getString("username"),
                            rs.getString("action"), rs.getString("details"),
                            rs.getString("ip_address"), rs.getTimestamp("created_at")
                        });
                    }
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Load audit logs failed", e);
                    SwingUtilities.invokeLater(() ->
                        NotificationUtils.showError(AdminAuditLogs.this, "Failed to load audit logs."));
                } finally {
                    DatabaseConnection.getInstance().releaseConnection(conn);
                }
                return null;
            }
        };
        worker.execute();
    }
}
