package com.inventory;

import com.inventory.controller.AuthController;
import com.inventory.dao.DatabaseConnection;
import com.inventory.view.LoginDialog;
import com.inventory.view.MainWindow;
import com.inventory.view.components.ThemeUtil;

import javax.swing.*;

/**
 * Application entry point.
 */
public class Main {

    public static void main(String[] args) {
        // Apply theme before any UI is created
        ThemeUtil.applyTheme();

        SwingUtilities.invokeLater(() -> {
            // Test DB connection
            if (!DatabaseConnection.getInstance().testConnection()) {
                JOptionPane.showMessageDialog(null,
                    "Cannot connect to the database.\n\n" +
                    "Please check:\n" +
                    "  1. MySQL is running\n" +
                    "  2. db_config.properties has correct credentials\n" +
                    "  3. The 'inventory_db' database exists\n\n" +
                    "Run database/schema.sql and database/sample_data.sql first.",
                    "Database Connection Error",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            showLogin();
        });
    }

    private static void showLogin() {
        LoginDialog loginDialog = new LoginDialog(null);
        loginDialog.setVisible(true);

        if (loginDialog.isLoginSuccessful()) {
            new MainWindow(AuthController.getInstance().getCurrentUser());
        } else {
            System.exit(0);
        }
    }
}
