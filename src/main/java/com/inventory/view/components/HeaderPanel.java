package com.inventory.view.components;

import com.inventory.model.User;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Top header bar showing page title, user info, and a live clock.
 */
public class HeaderPanel extends JPanel {

    private final JLabel titleLabel;
    private final JLabel clockLabel;
    private final JLabel userLabel;

    public HeaderPanel(User user) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeUtil.BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        setPreferredSize(new Dimension(0, 56));

        titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(ThemeUtil.FONT_HEADING);
        titleLabel.setForeground(ThemeUtil.TEXT_PRIMARY);
        add(titleLabel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        rightPanel.setOpaque(false);

        clockLabel = new JLabel();
        clockLabel.setFont(ThemeUtil.FONT_BODY);
        clockLabel.setForeground(ThemeUtil.TEXT_MUTED);

        userLabel = new JLabel("\uD83D\uDC64  " + user.getFullname() + "  |  " + user.getRole());
        userLabel.setFont(ThemeUtil.FONT_BODY);
        userLabel.setForeground(ThemeUtil.TEXT_PRIMARY);

        rightPanel.add(clockLabel);
        rightPanel.add(new JSeparator(SwingConstants.VERTICAL) {{ setPreferredSize(new Dimension(1, 20)); }});
        rightPanel.add(userLabel);
        add(rightPanel, BorderLayout.EAST);

        // Live clock timer
        Timer timer = new Timer(1000, e -> updateClock());
        timer.start();
        updateClock();
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    private void updateClock() {
        clockLabel.setText(new SimpleDateFormat("EEE, dd MMM yyyy  HH:mm:ss").format(new Date()));
    }
}
