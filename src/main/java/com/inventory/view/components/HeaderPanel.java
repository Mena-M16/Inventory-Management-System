package com.inventory.view.components;

import com.inventory.model.User;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Top header bar with page title, user info, and live clock.
 */
public class HeaderPanel extends JPanel {

    private final JLabel titleLabel;
    private final JLabel clockLabel;

    public HeaderPanel(User user) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeUtil.BORDER_COLOR),
            BorderFactory.createEmptyBorder(0, 24, 0, 24)
        ));
        setPreferredSize(new Dimension(0, 60));

        titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ThemeUtil.TEXT_PRIMARY);
        add(titleLabel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        rightPanel.setOpaque(false);

        clockLabel = new JLabel();
        clockLabel.setFont(ThemeUtil.FONT_BODY);
        clockLabel.setForeground(ThemeUtil.TEXT_MUTED);

        // User badge
        JPanel userBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        userBadge.setBackground(ThemeUtil.PRIMARY_LIGHT);
        userBadge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(199, 210, 254), 1),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));

        JLabel avatarLbl = new JLabel("\uD83D\uDC64");
        avatarLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

        JLabel userLbl = new JLabel(user.getFullname());
        userLbl.setFont(ThemeUtil.FONT_BOLD);
        userLbl.setForeground(ThemeUtil.PRIMARY);

        JLabel roleLbl = new JLabel("| " + user.getRole());
        roleLbl.setFont(ThemeUtil.FONT_SMALL);
        roleLbl.setForeground(ThemeUtil.TEXT_MUTED);

        userBadge.add(avatarLbl);
        userBadge.add(userLbl);
        userBadge.add(roleLbl);

        rightPanel.add(clockLabel);
        rightPanel.add(userBadge);
        add(rightPanel, BorderLayout.EAST);

        Timer timer = new Timer(1000, e -> updateClock());
        timer.start();
        updateClock();
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    private void updateClock() {
        clockLabel.setText(new SimpleDateFormat("EEE, dd MMM yyyy  |  HH:mm:ss").format(new Date()));
    }
}
