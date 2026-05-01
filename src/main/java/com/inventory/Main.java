package com.inventory;

import com.inventory.view.LoginDialog;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginDialog().setVisible(true));
    }
}
