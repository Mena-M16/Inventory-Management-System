package com.inventory.view.components;

import javax.swing.*;
import java.awt.*;

/**
 * Bottom status bar for real-time status messages.
 */
public class StatusBar extends JPanel {

    private final JLabel messageLabel;
    private final JLabel dbStatusLabel;
    private Timer clearTimer;

    public StatusBar() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeUtil.BORDER_COLOR),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        setPreferredSize(new Dimension(0, 28));

        messageLabel = new JLabel("Ready");
        messageLabel.setFont(ThemeUtil.FONT_SMALL);
        messageLabel.setForeground(ThemeUtil.TEXT_MUTED);

        dbStatusLabel = new JLabel("\u25CF  Connected");
        dbStatusLabel.setFont(ThemeUtil.FONT_SMALL);
        dbStatusLabel.setForeground(ThemeUtil.SUCCESS);

        add(messageLabel, BorderLayout.WEST);
        add(dbStatusLabel, BorderLayout.EAST);
    }

    /** Shows a temporary status message that clears after 4 seconds. */
    public void setMessage(String message) {
        setMessage(message, ThemeUtil.TEXT_MUTED);
    }

    public void setSuccess(String message) {
        setMessage(message, ThemeUtil.SUCCESS);
    }

    public void setError(String message) {
        setMessage(message, ThemeUtil.DANGER);
    }

    private void setMessage(String message, Color color) {
        messageLabel.setText(message);
        messageLabel.setForeground(color);
        if (clearTimer != null) clearTimer.stop();
        clearTimer = new Timer(4000, e -> {
            messageLabel.setText("Ready");
            messageLabel.setForeground(ThemeUtil.TEXT_MUTED);
        });
        clearTimer.setRepeats(false);
        clearTimer.start();
    }

    public void setDbStatus(boolean connected) {
        if (connected) {
            dbStatusLabel.setText("\u25CF  Connected");
            dbStatusLabel.setForeground(ThemeUtil.SUCCESS);
        } else {
            dbStatusLabel.setText("\u25CF  Disconnected");
            dbStatusLabel.setForeground(ThemeUtil.DANGER);
        }
    }
}
