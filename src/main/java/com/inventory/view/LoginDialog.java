package com.inventory.view;

import com.inventory.controller.AuthController;
import com.inventory.view.components.ThemeUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Login screen with gradient background and card-style form.
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
        super(parent, "Inventory Management System - Login", true);
        setSize(480, 560);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        // Gradient background panel
        JPanel bg = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(21, 101, 192),
                        0, getHeight(), new Color(33, 150, 243));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bg.setLayout(new GridBagLayout());
        setContentPane(bg);

        // Card panel
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(36, 40, 36, 40));
        card.setPreferredSize(new Dimension(360, 460));

        // Icon / title
        JLabel iconLabel = new JLabel("\uD83D\uDCE6", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Inventory System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(ThemeUtil.PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Sign in to continue", SwingConstants.CENTER);
        subtitleLabel.setFont(ThemeUtil.FONT_SMALL);
        subtitleLabel.setForeground(ThemeUtil.TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username
        JLabel userLbl = new JLabel("Username");
        userLbl.setFont(ThemeUtil.FONT_BOLD);
        userLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameField = ThemeUtil.createTextField(20);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameField.setHorizontalAlignment(JTextField.CENTER);

        // Password
        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(ThemeUtil.FONT_BOLD);
        passLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField = new JPasswordField(20);
        passwordField.setFont(ThemeUtil.FONT_BODY);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.setHorizontalAlignment(JTextField.CENTER);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeUtil.BORDER_COLOR),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        // Show password
        showPasswordCheck = new JCheckBox("Show password");
        showPasswordCheck.setFont(ThemeUtil.FONT_SMALL);
        showPasswordCheck.setOpaque(false);
        showPasswordCheck.setAlignmentX(Component.CENTER_ALIGNMENT);
        showPasswordCheck.setHorizontalAlignment(SwingConstants.CENTER);
        showPasswordCheck.addActionListener(e -> {
            passwordField.setEchoChar(showPasswordCheck.isSelected() ? (char) 0 : '\u2022');
        });

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(ThemeUtil.FONT_SMALL);
        errorLabel.setForeground(ThemeUtil.DANGER);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Login button
        loginButton = ThemeUtil.primaryButton("Sign In");
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.addActionListener(e -> attemptLogin());

        // Demo credentials hint
        JLabel demoLabel = new JLabel("<html><center><font color='#9E9E9E' size='2'>" +
            "Demo: admin/admin123 &nbsp;|&nbsp; manager/manager123 &nbsp;|&nbsp; staff/staff123</font></center></html>");
        demoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Assemble card
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitleLabel);
        card.add(Box.createVerticalStrut(28));
        card.add(userLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(14));
        card.add(passLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(6));
        card.add(showPasswordCheck);
        card.add(Box.createVerticalStrut(8));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(loginButton);
        card.add(Box.createVerticalStrut(20));
        card.add(demoLabel);

        bg.add(card);

        // Enter key triggers login
        getRootPane().setDefaultButton(loginButton);

        // Keyboard shortcut: Ctrl+L focuses username
        KeyStroke ctrlL = KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK);
        getRootPane().registerKeyboardAction(e -> usernameField.requestFocus(),
                ctrlL, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");
        errorLabel.setText(" ");

        // Run in background to avoid blocking EDT
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
