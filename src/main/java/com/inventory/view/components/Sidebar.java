package com.inventory.view.components;

import com.inventory.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Collapsible sidebar navigation panel.
 */
public class Sidebar extends JPanel {

    private static final int EXPANDED_WIDTH = 220;
    private static final int COLLAPSED_WIDTH = 60;

    private boolean expanded = true;
    private final List<SidebarItem> items = new ArrayList<>();
    private final JPanel menuPanel;
    private final JButton toggleBtn;
    private final JLabel logoLabel;

    public Sidebar(User user) {
        setLayout(new BorderLayout());
        setBackground(ThemeUtil.SIDEBAR_BG);
        setPreferredSize(new Dimension(EXPANDED_WIDTH, 0));

        // Logo / title area
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(ThemeUtil.SIDEBAR_BG);
        topPanel.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));

        logoLabel = new JLabel("IMS");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoLabel.setForeground(ThemeUtil.PRIMARY);
        topPanel.add(logoLabel, BorderLayout.CENTER);

        toggleBtn = new JButton("\u2630");
        toggleBtn.setForeground(ThemeUtil.SIDEBAR_FG);
        toggleBtn.setBackground(ThemeUtil.SIDEBAR_BG);
        toggleBtn.setBorderPainted(false);
        toggleBtn.setFocusPainted(false);
        toggleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleBtn.addActionListener(e -> toggleSidebar());
        topPanel.add(toggleBtn, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Menu items
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(ThemeUtil.SIDEBAR_BG);
        add(new JScrollPane(menuPanel) {{
            setBorder(null);
            getViewport().setBackground(ThemeUtil.SIDEBAR_BG);
            setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        }}, BorderLayout.CENTER);

        // User info at bottom
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(20, 20, 20));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JLabel userLabel = new JLabel("\uD83D\uDC64  " + user.getFullname());
        userLabel.setForeground(ThemeUtil.SIDEBAR_FG);
        userLabel.setFont(ThemeUtil.FONT_SMALL);
        JLabel roleLabel = new JLabel(user.getRole());
        roleLabel.setForeground(ThemeUtil.PRIMARY);
        roleLabel.setFont(ThemeUtil.FONT_SMALL);
        bottomPanel.add(userLabel, BorderLayout.CENTER);
        bottomPanel.add(roleLabel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /** Adds a navigation item to the sidebar. */
    public void addItem(String icon, String label, String key, ActionListener listener) {
        SidebarItem item = new SidebarItem(icon, label, key, listener);
        items.add(item);
        menuPanel.add(item);
        menuPanel.add(Box.createVerticalStrut(2));
    }

    /** Adds a visual separator. */
    public void addSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 60, 60));
        sep.setBackground(ThemeUtil.SIDEBAR_BG);
        menuPanel.add(Box.createVerticalStrut(4));
        menuPanel.add(sep);
        menuPanel.add(Box.createVerticalStrut(4));
    }

    /** Highlights the active menu item. */
    public void setActiveItem(String key) {
        for (SidebarItem item : items) {
            item.setActive(key.equals(item.getKey()));
        }
    }

    private void toggleSidebar() {
        expanded = !expanded;
        int width = expanded ? EXPANDED_WIDTH : COLLAPSED_WIDTH;
        setPreferredSize(new Dimension(width, 0));
        logoLabel.setVisible(expanded);
        for (SidebarItem item : items) item.setExpanded(expanded);
        revalidate();
        repaint();
    }

    // ---- Inner class ----

    private static class SidebarItem extends JPanel {
        private final String key;
        private final JLabel iconLabel;
        private final JLabel textLabel;
        private boolean active = false;

        SidebarItem(String icon, String label, String key, ActionListener listener) {
            this.key = key;
            setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
            setBackground(ThemeUtil.SIDEBAR_BG);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            iconLabel = new JLabel(icon);
            iconLabel.setForeground(ThemeUtil.SIDEBAR_FG);
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

            textLabel = new JLabel(label);
            textLabel.setForeground(ThemeUtil.SIDEBAR_FG);
            textLabel.setFont(ThemeUtil.FONT_BODY);

            add(iconLabel);
            add(textLabel);

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    listener.actionPerformed(new java.awt.event.ActionEvent(this, 0, key));
                }
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!active) setBackground(ThemeUtil.SIDEBAR_SEL);
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    if (!active) setBackground(ThemeUtil.SIDEBAR_BG);
                }
            });
        }

        String getKey() { return key; }

        void setActive(boolean active) {
            this.active = active;
            setBackground(active ? ThemeUtil.PRIMARY_DARK : ThemeUtil.SIDEBAR_BG);
            textLabel.setForeground(active ? Color.WHITE : ThemeUtil.SIDEBAR_FG);
        }

        void setExpanded(boolean expanded) {
            textLabel.setVisible(expanded);
        }
    }
}
