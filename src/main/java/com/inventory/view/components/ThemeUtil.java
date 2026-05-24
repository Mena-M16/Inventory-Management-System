package com.inventory.view.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Centralised theme constants and helper methods for consistent UI styling.
 */
public class ThemeUtil {

    // Modern 4-color palette
    public static final Color PRIMARY       = new Color(37, 99, 235);   // Blue
    public static final Color PRIMARY_DARK  = new Color(29, 78, 216);
    public static final Color PRIMARY_LIGHT = new Color(219, 234, 254);
    public static final Color SUCCESS       = new Color(22, 163, 74);   // Green
    public static final Color SUCCESS_DARK  = new Color(15, 118, 54);
    public static final Color WARNING       = new Color(217, 119, 6);   // Amber
    public static final Color WARNING_DARK  = new Color(180, 83, 9);
    public static final Color DANGER        = new Color(220, 38, 38);   // Red
    public static final Color DANGER_DARK   = new Color(185, 28, 28);
    public static final Color SIDEBAR_BG    = new Color(15, 23, 42);
    public static final Color SIDEBAR_FG    = new Color(148, 163, 184);
    public static final Color SIDEBAR_SEL   = new Color(30, 41, 59);
    public static final Color SIDEBAR_HOVER = new Color(30, 41, 59);
    public static final Color BG_LIGHT      = new Color(248, 250, 252);
    public static final Color CARD_BG       = Color.WHITE;
    public static final Color TEXT_PRIMARY  = new Color(15, 23, 42);
    public static final Color TEXT_MUTED    = new Color(100, 116, 139);
    public static final Color BORDER_COLOR  = new Color(226, 232, 240);
    public static final Color TABLE_HEADER  = new Color(37, 99, 235);
    public static final Color TABLE_ALT     = new Color(248, 250, 252);
    public static final Color ACCENT        = new Color(37, 99, 235);

    // Consistent font sizes - increased
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 14);

    private ThemeUtil() {}

    /** Applies a flat look-and-feel to the application. */
    public static void applyTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        UIManager.put("Button.font", FONT_BODY);
        UIManager.put("Label.font", FONT_BODY);
        UIManager.put("TextField.font", FONT_BODY);
        UIManager.put("ComboBox.font", FONT_BODY);
        UIManager.put("Table.font", FONT_BODY);
        UIManager.put("TableHeader.font", FONT_BOLD);
        UIManager.put("TextArea.font", FONT_BODY);
        UIManager.put("Panel.background", BG_LIGHT);
    }

    /** Creates a styled primary button with hover effect. */
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 32));
        btn.setOpaque(true);
        addHoverEffect(btn, PRIMARY, PRIMARY_DARK);
        return btn;
    }

    public static JButton dangerButton(String text) {
        JButton btn = primaryButton(text);
        btn.setBackground(DANGER);
        addHoverEffect(btn, DANGER, DANGER_DARK);
        return btn;
    }

    public static JButton successButton(String text) {
        JButton btn = primaryButton(text);
        btn.setBackground(SUCCESS);
        addHoverEffect(btn, SUCCESS, SUCCESS_DARK);
        return btn;
    }

    public static JButton warningButton(String text) {
        JButton btn = primaryButton(text);
        btn.setBackground(WARNING);
        addHoverEffect(btn, WARNING, WARNING_DARK);
        return btn;
    }

    public static JButton secondaryButton(String text) {
        JButton btn = primaryButton(text);
        Color base  = new Color(100, 116, 139);
        Color hover = new Color(71, 85, 105);
        btn.setBackground(base);
        addHoverEffect(btn, base, hover);
        return btn;
    }

    private static void addHoverEffect(JButton btn, Color normal, Color hovered) {
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(hovered);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(normal);
            }
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(hovered.darker());
            }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(hovered);
            }
        });
    }

    /** Applies standard header styling to a JTable. */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(36);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(TABLE_HEADER);
        header.setForeground(Color.WHITE);
        header.setFont(FONT_BOLD);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setReorderingAllowed(false);
        header.setOpaque(true);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = new JLabel(value == null ? "" : value.toString());
                lbl.setFont(FONT_BOLD);
                lbl.setForeground(Color.WHITE);
                lbl.setBackground(TABLE_HEADER);
                lbl.setOpaque(true);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 1, PRIMARY_DARK),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10)
                ));
                return lbl;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : TABLE_ALT);
                    setForeground(TEXT_PRIMARY);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        });
    }

    /** Creates a card-style panel. */
    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        return card;
    }

    /** Creates a card with a colored top accent bar. */
    public static JPanel createAccentCard(Color accent) {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(3, 0, 0, 0, accent),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
            )
        ));
        return card;
    }

    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    public static JLabel sectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_HEADING);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    public static Color stockStatusColor(String status) {
        if (status == null) return TEXT_MUTED;
        switch (status) {
            case "In Stock":     return SUCCESS;
            case "Low Stock":    return WARNING;
            case "Out of Stock": return DANGER;
            default:             return TEXT_MUTED;
        }
    }

    /** Returns a background color for stock status badges. */
    public static Color stockStatusBg(String status) {
        if (status == null) return new Color(241, 245, 249);
        switch (status) {
            case "In Stock":     return new Color(220, 252, 231);
            case "Low Stock":    return new Color(255, 237, 213);
            case "Out of Stock": return new Color(254, 226, 226);
            default:             return new Color(241, 245, 249);
        }
    }

    /**
     * Shows a semi-transparent blur overlay behind a dialog.
     * Returns the overlay JDialog — call overlay.dispose() when the main dialog closes.
     */
    public static JDialog showBlurOverlay(Window parentWindow) {
        JDialog overlay = new JDialog((Frame) null, false);
        overlay.setUndecorated(true);
        overlay.setBackground(new Color(0, 0, 0, 0));
        if (parentWindow != null) {
            overlay.setSize(parentWindow.getSize());
            overlay.setLocation(parentWindow.getLocationOnScreen());
        }
        JPanel glass = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 140));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        glass.setOpaque(false);
        overlay.setContentPane(glass);
        overlay.setVisible(true);
        return overlay;
    }
}