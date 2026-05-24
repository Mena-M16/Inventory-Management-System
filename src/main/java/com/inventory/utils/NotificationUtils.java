package com.inventory.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Utility for showing toast-style notifications and standard dialogs.
 */
public class NotificationUtils {

    private NotificationUtils() {}

    public static void showSuccess(Component parent, String message) {
        showToast(parent, message, new Color(76, 175, 80));
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message, "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    /** Displays a brief toast notification that auto-dismisses after 2.5 seconds. */
    private static void showToast(Component parent, String message, Color bgColor) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        if (window == null && parent instanceof Window) window = (Window) parent;
        if (window == null) {
            JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JWindow toast = new JWindow(window);
        JLabel label = new JLabel("  \u2714  " + message + "  ", SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        label.setOpaque(true);
        label.setBackground(bgColor);
        label.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        toast.add(label);
        toast.pack();

        // Position at bottom-centre of parent window
        Point loc = window.getLocationOnScreen();
        int x = loc.x + (window.getWidth() - toast.getWidth()) / 2;
        int y = loc.y + window.getHeight() - toast.getHeight() - 60;
        toast.setLocation(x, y);
        toast.setVisible(true);

        Timer timer = new Timer(2500, (ActionEvent e) -> toast.dispose());
        timer.setRepeats(false);
        timer.start();
    }
}
