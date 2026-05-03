package com.inventory.view.admin;

import com.inventory.controller.AuthController;
import com.inventory.utils.BackupUtils;
import com.inventory.utils.NotificationUtils;
import com.inventory.view.components.ThemeUtil;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;

/**
 * System settings panel for Admin users (DB config, backup/restore).
 */
public class AdminSettings extends JPanel {

    private JTextField dbHostField, dbPortField, dbNameField, dbUserField;
    private JPasswordField dbPassField;

    public AdminSettings() {
        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeUtil.BG_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
        loadCurrentConfig();
    }

    private void buildUI() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);

        // DB Config card
        JPanel dbCard = ThemeUtil.createCard();
        dbCard.setLayout(new GridBagLayout());
        dbCard.setMaximumSize(new Dimension(600, 300));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = ThemeUtil.sectionTitle("Database Configuration");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        dbCard.add(title, gbc);
        gbc.gridwidth = 1;

        dbHostField = ThemeUtil.createTextField(15);
        dbPortField = ThemeUtil.createTextField(6);
        dbNameField = ThemeUtil.createTextField(15);
        dbUserField = ThemeUtil.createTextField(15);
        dbPassField = new JPasswordField(15);
        dbPassField.setFont(ThemeUtil.FONT_BODY);

        String[][] rows = {{"Host:", null}, {"Port:", null}, {"Database:", null}, {"Username:", null}, {"Password:", null}};
        Component[] inputs = {dbHostField, dbPortField, dbNameField, dbUserField, dbPassField};
        for (int i = 0; i < rows.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1; gbc.weightx = 0.3;
            dbCard.add(new JLabel(rows[i][0]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.7;
            dbCard.add(inputs[i], gbc);
        }

        JButton saveConfigBtn = ThemeUtil.primaryButton("Save Config");
        saveConfigBtn.addActionListener(e -> saveConfig());
        gbc.gridx = 1; gbc.gridy = rows.length + 1;
        dbCard.add(saveConfigBtn, gbc);

        wrapper.add(dbCard);
        wrapper.add(Box.createVerticalStrut(16));

        // Backup card
        JPanel backupCard = ThemeUtil.createCard();
        backupCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        backupCard.setMaximumSize(new Dimension(600, 80));
        backupCard.add(ThemeUtil.sectionTitle("Database Backup & Restore"));

        JButton backupBtn  = ThemeUtil.successButton("Backup Now");
        JButton restoreBtn = ThemeUtil.warningButton("Restore");

        backupBtn.addActionListener(e -> {
            Properties p = loadProperties();
            BackupUtils.backup(this,
                p.getProperty("db.host", "localhost"),
                p.getProperty("db.port", "3306"),
                p.getProperty("db.name", "inventory_db"),
                p.getProperty("db.username", "root"),
                p.getProperty("db.password", ""));
            AuthController.getInstance().logAudit(
                AuthController.getInstance().getCurrentUser().getId(),
                AuthController.getInstance().getCurrentUser().getUsername(),
                "DB_BACKUP", "Manual database backup triggered");
        });

        restoreBtn.addActionListener(e -> {
            Properties p = loadProperties();
            BackupUtils.restore(this,
                p.getProperty("db.host", "localhost"),
                p.getProperty("db.port", "3306"),
                p.getProperty("db.name", "inventory_db"),
                p.getProperty("db.username", "root"),
                p.getProperty("db.password", ""));
        });

        backupCard.add(backupBtn);
        backupCard.add(restoreBtn);
        wrapper.add(backupCard);

        add(new JScrollPane(wrapper) {{ setBorder(null); setOpaque(false); getViewport().setOpaque(false); }}, BorderLayout.CENTER);
    }

    private void loadCurrentConfig() {
        Properties p = loadProperties();
        String url = p.getProperty("db.url", "jdbc:mysql://localhost:3306/inventory_db");
        // Parse host/port/dbname from URL
        try {
            String stripped = url.replace("jdbc:mysql://", "");
            String[] parts = stripped.split("/");
            String[] hostPort = parts[0].split(":");
            dbHostField.setText(hostPort[0]);
            dbPortField.setText(hostPort.length > 1 ? hostPort[1].split("\\?")[0] : "3306");
            dbNameField.setText(parts.length > 1 ? parts[1].split("\\?")[0] : "inventory_db");
        } catch (Exception ignored) {
            dbHostField.setText("localhost");
            dbPortField.setText("3306");
            dbNameField.setText("inventory_db");
        }
        dbUserField.setText(p.getProperty("db.username", "root"));
        dbPassField.setText(p.getProperty("db.password", ""));
    }

    private void saveConfig() {
        try {
            Properties p = loadProperties();
            String host = dbHostField.getText().trim();
            String port = dbPortField.getText().trim();
            String name = dbNameField.getText().trim();
            p.setProperty("db.url", "jdbc:mysql://" + host + ":" + port + "/" + name +
                    "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
            p.setProperty("db.username", dbUserField.getText().trim());
            p.setProperty("db.password", new String(dbPassField.getPassword()));

            File file = new File("src/main/resources/db_config.properties");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                p.store(fos, "Database Configuration");
            }
            NotificationUtils.showSuccess(this, "Configuration saved. Restart to apply.");
        } catch (Exception e) {
            NotificationUtils.showError(this, "Failed to save config: " + e.getMessage());
        }
    }

    private Properties loadProperties() {
        Properties p = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("db_config.properties")) {
            if (is != null) p.load(is);
        } catch (Exception ignored) {}
        return p;
    }
}
