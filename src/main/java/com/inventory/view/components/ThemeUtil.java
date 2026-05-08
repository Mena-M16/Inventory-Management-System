package com.inventory.view.components;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Centralised theme constants and helper methods for consistent UI styling.
 */
public class ThemeUtil {

    // Colour palette
    public static final Color PRIMARY      = new Color(33, 150, 243);
    public static final Color PRIMARY_DARK = new Color(21, 101, 192);
    public static final Color SUCCESS      = new Color(76, 175, 80);
    public static final Color WARNING      = new Color(255, 152, 0);
    public static final Color DANGER       = new Color(244, 67, 54);
    public static final Color SIDEBAR_BG   = new Color(33, 33, 33);
    public static final Color SIDEBAR_FG   = new Color(200, 200, 200);
    public static final Color SIDEBAR_SEL  = new Color(55, 55, 55);
    public static final Color BG_LIGHT     = new Color(248, 249, 250);
    public static final Color CARD_BG      = Color.WHITE;
    public static final Color TEXT_PRIMARY = new Color(33, 33, 33);
    public static final Color TEXT_MUTED   = new Color(117, 117, 117);
    public static final Color BORDER_COLOR = new Color(224, 224, 224);
    public static final Color TABLE_HEADER = new Color(33, 150, 243);
    public static final Color TABLE_ALT    = new Color(245, 250, 255);

    // Fonts
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 13);

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
    }

    /** Creates a styled primary button. */
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 36));
        return btn;
    }

    /** Creates a styled danger (red) button. */
    public static JButton dangerButton(String text) {
        JButton btn = primaryButton(text);
        btn.setBackground(DANGER);
        return btn;
    }

    /** Creates a styled success (green) button. */
    public static JButton successButton(String text) {
        JButton btn = primaryButton(text);
        btn.setBackground(SUCCESS);
        return btn;
    }

    /** Creates a styled warning (orange) button. */
    public static JButton warningButton(String text) {
        JButton btn = primaryButton(text);
        btn.setBackground(WARNING);
        return btn;
    }

    /** Creates a styled secondary (grey) button. */
    public static JButton secondaryButton(String text) {
        JButton btn = primaryButton(text);
        btn.setBackground(new Color(158, 158, 158));
        return btn;
    }

    /** Applies standard header styling to a JTable. */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(30);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(new Color(187, 222, 251));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFillsViewportHeight(true);

        // Always show column headers — fix header visibility
        JTableHeader header = table.getTableHeader();
        header.setBackground(TABLE_HEADER);
        header.setForeground(Color.WHITE);
        header.setFont(FONT_BOLD);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setReorderingAllowed(false);
        header.setOpaque(true);
        header.setVisible(true);

        // Custom header renderer so column names are always shown with white text
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
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)
                ));
                return lbl;
            }
        });

        // Alternating row colours
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : TABLE_ALT);
                    setForeground(TEXT_PRIMARY);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
    }

    /** Creates a card-style panel with a white background and subtle shadow border. */
    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        return card;
    }

    /** Creates a labelled text field pair. */
    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        return tf;
    }

    /** Creates a section title label. */
    public static JLabel sectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_HEADING);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    /** Returns a colour for a stock status string. */
    public static Color stockStatusColor(String status) {
        if (status == null) return TEXT_MUTED;
        switch (status) {
            case "In Stock":    return SUCCESS;
            case "Low Stock":   return WARNING;
            case "Out of Stock": return DANGER;
            default:            return TEXT_MUTED;
        }
    }
}
