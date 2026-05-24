package com.inventory.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import com.inventory.controller.AuthController;
import com.inventory.view.components.ThemeUtil;

/**
 * Modern login screen with gradient background and card-style form.
 */
public class LoginDialog extends JDialog {

    private final AuthController authController = AuthController.getInstance();
    private boolean loginSuccessful = false;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel errorLabel;
    private JCheckBox showPasswordCheck;

    public LoginDialog(Frame parent) {
        super(parent, "Inventory Management System", true);
        setSize(900, 580);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new GridLayout(1, 2));
        setContentPane(root);

        // LEFT: branding panel
        JPanel brandPanel = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(67, 97, 238),
                        getWidth(), getHeight(), new Color(139, 92, 246));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        JPanel brandContent = new JPanel();
        brandContent.setLayout(new BoxLayout(brandContent, BoxLayout.Y_AXIS));
        brandContent.setOpaque(false);

        JLabel iconLbl = new JLabel("\uD83D\uDCE6");
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appName = new JLabel("InventoryPro");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 28));
        appName.setForeground(Color.WHITE);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("Smart Inventory Management");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tagline.setForeground(new Color(200, 210, 255));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        brandContent.add(iconLbl);
        brandContent.add(Box.createVerticalStrut(12));
        brandContent.add(appName);
        brandContent.add(Box.createVerticalStrut(8));
        brandContent.add(tagline);
        brandContent.add(Box.createVerticalStrut(32));

        // Feature bullets
        String[] features = {"Real-time stock tracking", "Role-based access control", "Transaction history & reports"};
        for (String f : features) {
            JLabel fl = new JLabel("  \u2713  " + f);
            fl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            fl.setForeground(new Color(200, 220, 255));
            fl.setAlignmentX(Component.CENTER_ALIGNMENT);
            brandContent.add(fl);
            brandContent.add(Box.createVerticalStrut(8));
        }

        brandPanel.add(brandContent);
        root.add(brandPanel);

        // RIGHT: login form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(340, 420));

        JLabel welcomeLbl = new JLabel("Welcome back");
        welcomeLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLbl.setForeground(ThemeUtil.TEXT_PRIMARY);
        welcomeLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("Sign in to your account");
        subLbl.setFont(ThemeUtil.FONT_BODY);
        subLbl.setForeground(ThemeUtil.TEXT_MUTED);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Fields panel with labels parallel to inputs
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        fieldsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 4, 8, 4);

        // Username row
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel userLbl = new JLabel("Username");
        userLbl.setFont(ThemeUtil.FONT_BOLD);
        userLbl.setForeground(ThemeUtil.TEXT_PRIMARY);
        fieldsPanel.add(userLbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        usernameField = new JTextField(20);
        usernameField.setFont(ThemeUtil.FONT_BODY);
        usernameField.setPreferredSize(new Dimension(0, 40));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeUtil.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        fieldsPanel.add(usernameField, gbc);

        // Password row
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(ThemeUtil.FONT_BOLD);
        passLbl.setForeground(ThemeUtil.TEXT_PRIMARY);
        fieldsPanel.add(passLbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        passwordField = new JPasswordField(20);
        passwordField.setFont(ThemeUtil.FONT_BODY);
        passwordField.setPreferredSize(new Dimension(0, 40));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeUtil.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        fieldsPanel.add(passwordField, gbc);

        // Show password
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.7;
        showPasswordCheck = new JCheckBox("Show password");
        showPasswordCheck.setFont(ThemeUtil.FONT_SMALL);
        showPasswordCheck.setOpaque(false);
        showPasswordCheck.setForeground(ThemeUtil.TEXT_MUTED);
        showPasswordCheck.addActionListener(e ->
            passwordField.setEchoChar(showPasswordCheck.isSelected() ? (char) 0 : '\u2022'));
        fieldsPanel.add(showPasswordCheck, gbc);

        // Error label
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        errorLabel = new JLabel(" ");
        errorLabel.setFont(ThemeUtil.FONT_SMALL);
        errorLabel.setForeground(ThemeUtil.DANGER);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        fieldsPanel.add(errorLabel, gbc);

        // Login button
        gbc.gridy = 4;
        loginButton = new JButton("Sign In");
        loginButton.setBackground(ThemeUtil.PRIMARY);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setOpaque(true);
        loginButton.addActionListener(e -> attemptLogin());
        loginButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { loginButton.setBackground(ThemeUtil.PRIMARY_DARK); }
            @Override public void mouseExited(MouseEvent e)  { loginButton.setBackground(ThemeUtil.PRIMARY); }
        });
        fieldsPanel.add(loginButton, gbc);

        form.add(welcomeLbl);
        form.add(Box.createVerticalStrut(4));
        form.add(subLbl);
        form.add(Box.createVerticalStrut(24));
        form.add(fieldsPanel);

        formPanel.add(form);
        root.add(formPanel);

        getRootPane().setDefaultButton(loginButton);
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");
        errorLabel.setText(" ");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() {
                return authController.login(username, password);
            }
            @Override protected void done() {
                try {
                    String error = get();
                    if (error == null) {
                        loginSuccessful = true;
                        dispose();
                    } else {
                        errorLabel.setText(error);
                        passwordField.setText("");
                        passwordField.requestFocus();
                    }
                } catch (Exception ex) {
                    errorLabel.setText("Login error. Please try again.");
                } finally {
                    loginButton.setEnabled(true);
                    loginButton.setText("Sign In");
                }
            }
        };
        worker.execute();
    }

    public boolean isLoginSuccessful() { return loginSuccessful; }
}
