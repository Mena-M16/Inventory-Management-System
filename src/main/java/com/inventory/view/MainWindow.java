package com.inventory.view;

import com.inventory.controller.AuthController;
import com.inventory.model.User;
import com.inventory.view.admin.*;
import com.inventory.view.components.HeaderPanel;
import com.inventory.view.components.Sidebar;
import com.inventory.view.components.StatusBar;
import com.inventory.view.components.ThemeUtil;
import com.inventory.view.manager.*;
import com.inventory.view.staff.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main application window — builds the sidebar, header, content area, and status bar
 * based on the logged-in user's role.
 */
public class MainWindow extends JFrame {

    private final User currentUser;
    private final AuthController auth = AuthController.getInstance();

    private Sidebar sidebar;
    private HeaderPanel header;
    private StatusBar statusBar;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    public MainWindow(User user) {
        this.currentUser = user;
        setTitle("Inventory Management System  —  " + user.getFullname() + " (" + user.getRole() + ")");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 700));
        setSize(1400, 800);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { confirmExit(); }
        });

        buildUI();
        registerKeyboardShortcuts();
        setVisible(true);

        // Session timeout checker
        Timer sessionTimer = new Timer(60_000, e -> {
            if (auth.isSessionExpired()) {
                JOptionPane.showMessageDialog(this, "Session expired. Please log in again.", "Session Timeout", JOptionPane.WARNING_MESSAGE);
                logout();
            }
        });
        sessionTimer.start();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        // Sidebar
        sidebar = new Sidebar(currentUser);
        buildSidebarItems();
        add(sidebar, BorderLayout.WEST);

        // Right side
        JPanel rightPanel = new JPanel(new BorderLayout());
        header = new HeaderPanel(currentUser);
        rightPanel.add(header, BorderLayout.NORTH);

        // Content area
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(ThemeUtil.BG_LIGHT);
        buildContentPanels();
        rightPanel.add(contentPanel, BorderLayout.CENTER);

        statusBar = new StatusBar();
        rightPanel.add(statusBar, BorderLayout.SOUTH);

        add(rightPanel, BorderLayout.CENTER);

        // Show default panel
        showPanel("dashboard");
    }

    private void buildSidebarItems() {
        String role = currentUser.getRole();

        sidebar.addItem("\uD83C\uDFE0", "Dashboard",    "dashboard",   e -> showPanel("dashboard"));
        sidebar.addItem("\uD83D\uDCE6", "Products",     "products",    e -> showPanel("products"));
        sidebar.addItem("\uD83D\uDCCA", "Stock",        "stock",       e -> showPanel("stock"));

        if ("ADMIN".equals(role) || "MANAGER".equals(role)) {
            sidebar.addItem("\uD83D\uDED2", "Sales",       "sales",       e -> showPanel("sales"));
            sidebar.addItem("\uD83D\uDCC8", "Reports",     "reports",     e -> showPanel("reports"));
            sidebar.addItem("\uD83D\uDE9A", "Suppliers",   "suppliers",   e -> showPanel("suppliers"));
            sidebar.addItem("\uD83C\uDFF7", "Categories",  "categories",  e -> showPanel("categories"));
        }

        if ("STAFF".equals(role)) {
            sidebar.addItem("\uD83D\uDED2", "Sales",       "sales",       e -> showPanel("sales"));
            sidebar.addItem("\uD83D\uDCCB", "Transactions","transactions",e -> showPanel("transactions"));
        }

        if ("ADMIN".equals(role)) {
            sidebar.addSeparator();
            sidebar.addItem("\uD83D\uDC65", "Users",       "users",       e -> showPanel("users"));
            sidebar.addItem("\uD83D\uDCDD", "Audit Logs",  "auditlogs",   e -> showPanel("auditlogs"));
            sidebar.addItem("\u2699",        "Settings",    "settings",    e -> showPanel("settings"));
        }

        sidebar.addSeparator();
        sidebar.addItem("\uD83D\uDEAA", "Logout",       "logout",      e -> logout());
    }

    private void buildContentPanels() {
        String role = currentUser.getRole();

        switch (role) {
            case "ADMIN":
                contentPanel.add(new AdminDashboard(),   "dashboard");
                contentPanel.add(new AdminProducts(),    "products");
                contentPanel.add(new AdminStock(),       "stock");
                contentPanel.add(new AdminSales(),       "sales");
                contentPanel.add(new AdminReports(),     "reports");
                contentPanel.add(new AdminSuppliers(),   "suppliers");
                contentPanel.add(new AdminCategories(),  "categories");
                contentPanel.add(new AdminUsers(),       "users");
                contentPanel.add(new AdminAuditLogs(),   "auditlogs");
                contentPanel.add(new AdminSettings(),    "settings");
                break;
            case "MANAGER":
                contentPanel.add(new ManagerDashboard(),   "dashboard");
                contentPanel.add(new ManagerProducts(),    "products");
                contentPanel.add(new ManagerStock(),       "stock");
                contentPanel.add(new AdminSales(),         "sales");
                contentPanel.add(new ManagerReports(),     "reports");
                contentPanel.add(new ManagerSuppliers(),   "suppliers");
                contentPanel.add(new ManagerCategories(),  "categories");
                break;
            default: // STAFF
                contentPanel.add(new StaffDashboard(),     "dashboard");
                contentPanel.add(new StaffProducts(),      "products");
                contentPanel.add(new StaffStock(),         "stock");
                contentPanel.add(new StaffSales(),         "sales");
                contentPanel.add(new StaffTransactions(),  "transactions");
                break;
        }
    }

    /** Public method so other panels can navigate to the Sales page. */
    public void showSalesPanel() {
        showPanel("sales");
    }

    private void showPanel(String key) {
        auth.refreshSession();
        cardLayout.show(contentPanel, key);
        sidebar.setActiveItem(key);

        String title;
        if ("dashboard".equals(key))         title = "Dashboard";
        else if ("products".equals(key))     title = "Product Management";
        else if ("stock".equals(key))        title = "Stock Management";
        else if ("sales".equals(key))        title = "Sales";
        else if ("reports".equals(key))      title = "Reports";
        else if ("suppliers".equals(key))    title = "Supplier Management";
        else if ("categories".equals(key))   title = "Category Management";
        else if ("users".equals(key))        title = "User Management";
        else if ("auditlogs".equals(key))    title = "Audit Logs";
        else if ("settings".equals(key))     title = "System Settings";
        else if ("transactions".equals(key)) title = "Transaction History";
        else                                 title = "Inventory Management System";
        header.setTitle(title);
        statusBar.setMessage("Viewing: " + title);

        // F5 refresh for current panel
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F5"), "refresh");
        getRootPane().getActionMap().put("refresh", new javax.swing.AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Component visible = getVisiblePanel();
                if (visible instanceof AdminProducts)    ((AdminProducts) visible).refresh();
                else if (visible instanceof AdminStock)  ((AdminStock) visible).refresh();
                else if (visible instanceof AdminUsers)  ((AdminUsers) visible).refresh();
            }
        });
    }

    private Component getVisiblePanel() {
        for (Component c : contentPanel.getComponents()) {
            if (c.isVisible()) return c;
        }
        return null;
    }

    private void registerKeyboardShortcuts() {
        // F5 — refresh (handled per panel)
        // Ctrl+L — logout
        KeyStroke ctrlL = KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK);
        getRootPane().registerKeyboardAction(e -> logout(), ctrlL, JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Ctrl+F — focus search (best-effort: send focus to first text field in current panel)
        KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK);
        getRootPane().registerKeyboardAction(e -> {
            Component focused = contentPanel.getComponent(0);
            if (focused != null) focused.requestFocus();
        }, ctrlF, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void logout() {
        if (NotificationUtils.showConfirm(this, "Are you sure you want to log out?")) {
            auth.logout();
            dispose();
            showLogin();
        }
    }

    private void confirmExit() {
        if (NotificationUtils.showConfirm(this, "Exit the application?")) {
            auth.logout();
            System.exit(0);
        }
    }

    private void showLogin() {
        SwingUtilities.invokeLater(() -> {
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);
            if (login.isLoginSuccessful()) {
                new MainWindow(AuthController.getInstance().getCurrentUser());
            }
        });
    }

    // Inner helper to avoid import cycle
    private static class NotificationUtils {
        static boolean showConfirm(Component parent, String msg) {
            return JOptionPane.showConfirmDialog(parent, msg, "Confirm",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        }
    }
}
