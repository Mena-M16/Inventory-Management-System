package com.inventory.view.components;

import com.inventory.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Collapsible sidebar navigation panel with improved icon visibility.
 */
public class Sidebar extends JPanel {

    private static final int EXPANDED_WIDTH  = 230;
    private static final int COLLAPSED_WIDTH = 64;

    private boolean expanded = true;
    private final List<SidebarItem> items = new ArrayList<>();
    private final JPanel menuPanel;
    private final JButton toggleBtn;
    private final JLabel logoLabel;
    private final JLabel logoIcon;

    public Sidebar(User user) {
        setLayout(new BorderLayout());
        setBackground(ThemeUtil.SIDEBAR_BG);
        setPreferredSize(new Dimension(EXPANDED_WIDTH, 0));

        // Top: logo
        JPanel topPanel = new JPanel(new BorderLayout(8, 0));
        topPanel.setBackground(ThemeUtil.SIDEBAR_BG);
        topPanel.setBorder(BorderFactory.createEmptyBorder(18, 14, 18, 14));

        logoIcon = new JLabel("\uD83D\uDCE6");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        logoIcon.setForeground(ThemeUtil.PRIMARY);

        logoLabel = new JLabel("InventoryPro");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        logoLabel.setForeground(Color.WHITE);

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        logoPanel.setOpaque(false);
        logoPanel.add(logoIcon);
        logoPanel.add(logoLabel);
        topPanel.add(logoPanel, BorderLayout.CENTER);

        toggleBtn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                // Three horizontal lines (hamburger)
                g2.drawLine(cx - 8, cy - 6, cx + 8, cy - 6);
                g2.drawLine(cx - 8, cy,     cx + 8, cy);
                g2.drawLine(cx - 8, cy + 6, cx + 8, cy + 6);
            }
        };
        toggleBtn.setPreferredSize(new Dimension(36, 36));
        toggleBtn.setBackground(ThemeUtil.SIDEBAR_BG);
        toggleBtn.setBorderPainted(false);
        toggleBtn.setFocusPainted(false);
        toggleBtn.setContentAreaFilled(false);
        toggleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleBtn.addActionListener(e -> toggleSidebar());
        topPanel.add(toggleBtn, BorderLayout.EAST);

        // Separator line
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(30, 41, 59));
        sep.setBackground(new Color(30, 41, 59));

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setBackground(ThemeUtil.SIDEBAR_BG);
        topWrapper.add(topPanel, BorderLayout.CENTER);
        topWrapper.add(sep, BorderLayout.SOUTH);
        add(topWrapper, BorderLayout.NORTH);

        // Menu
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(ThemeUtil.SIDEBAR_BG);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        JScrollPane scroll = new JScrollPane(menuPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(ThemeUtil.SIDEBAR_BG);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(8);
        add(scroll, BorderLayout.CENTER);

        // Bottom: user info
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 4));
        bottomPanel.setBackground(new Color(8, 14, 28));
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(30, 41, 59)),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        JLabel avatarLbl = new JLabel("\uD83D\uDC64");
        avatarLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

        JPanel userInfo = new JPanel(new GridLayout(2, 1, 0, 2));
        userInfo.setOpaque(false);
        JLabel nameLbl = new JLabel(user.getFullname());
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLbl.setForeground(Color.WHITE);

        JLabel roleLbl = new JLabel(user.getRole());
        roleLbl.setFont(ThemeUtil.FONT_SMALL);
        roleLbl.setForeground(ThemeUtil.PRIMARY);

        userInfo.add(nameLbl);
        userInfo.add(roleLbl);

        bottomPanel.add(avatarLbl, BorderLayout.WEST);
        bottomPanel.add(userInfo, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void addItem(String icon, String label, String key, ActionListener listener) {
        SidebarItem item = new SidebarItem(icon, label, key, listener);
        items.add(item);
        menuPanel.add(item);
        menuPanel.add(Box.createVerticalStrut(2));
    }

    public void addSeparator() {
        JPanel sepWrapper = new JPanel(new BorderLayout());
        sepWrapper.setOpaque(false);
        sepWrapper.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(30, 41, 59));
        sepWrapper.add(sep);
        menuPanel.add(sepWrapper);
    }

    public void setActiveItem(String key) {
        for (SidebarItem item : items) item.setActive(key.equals(item.getKey()));
    }

    private void toggleSidebar() {
        expanded = !expanded;
        setPreferredSize(new Dimension(expanded ? EXPANDED_WIDTH : COLLAPSED_WIDTH, 0));
        logoLabel.setVisible(expanded);
        for (SidebarItem item : items) item.setExpanded(expanded);
        revalidate(); repaint();
    }

    // ---- Inner class ----
    private static class SidebarItem extends JPanel {
        private final String key;
        private final JLabel iconLabel;
        private final JLabel textLabel;
        private boolean active = false;

        SidebarItem(String icon, String label, String key, ActionListener listener) {
            this.key = key;
            setLayout(new FlowLayout(FlowLayout.LEFT, 14, 10));
            setBackground(ThemeUtil.SIDEBAR_BG);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 17));
            iconLabel.setForeground(Color.WHITE);
            iconLabel.setPreferredSize(new Dimension(24, 24));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

            textLabel = new JLabel(label);
            textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            textLabel.setForeground(ThemeUtil.SIDEBAR_FG);

            add(iconLabel);
            add(textLabel);

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    listener.actionPerformed(new java.awt.event.ActionEvent(this, 0, key));
                }
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!active) {
                        setBackground(ThemeUtil.SIDEBAR_HOVER);
                        textLabel.setForeground(Color.WHITE);
                    }
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    if (!active) {
                        setBackground(ThemeUtil.SIDEBAR_BG);
                        textLabel.setForeground(ThemeUtil.SIDEBAR_FG);
                    }
                }
            });
        }

        String getKey() { return key; }

        void setActive(boolean active) {
            this.active = active;
            setBackground(active ? ThemeUtil.PRIMARY : ThemeUtil.SIDEBAR_BG);
            textLabel.setForeground(active ? Color.WHITE : ThemeUtil.SIDEBAR_FG);
            iconLabel.setForeground(Color.WHITE);
        }

        void setExpanded(boolean expanded) {
            textLabel.setVisible(expanded);
        }
    }
}
