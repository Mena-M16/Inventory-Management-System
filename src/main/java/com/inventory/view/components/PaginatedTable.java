package com.inventory.view.components;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A reusable paginated table component with horizontal scroll and page navigation.
 */
public class PaginatedTable extends JPanel {

    private final JTable table;
    private final DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    private final List<Object[]> allRows = new ArrayList<>();
    private int currentPage = 0;
    private int pageSize = 15;

    // Pagination controls
    private final JLabel pageInfoLabel;
    private final JButton firstBtn;
    private final JButton prevBtn;
    private final JButton nextBtn;
    private final JButton lastBtn;
    private final JComboBox<Integer> pageSizeCombo;

    public PaginatedTable(String[] columns) {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);

        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        ThemeUtil.styleTable(table);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Horizontal + vertical scroll
        JScrollPane scrollPane = new JScrollPane(table,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeUtil.BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // Pagination bar
        JPanel paginationBar = new JPanel(new BorderLayout(8, 0));
        paginationBar.setBackground(Color.WHITE);
        paginationBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeUtil.BORDER_COLOR),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        // Left: page size selector
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leftPanel.setOpaque(false);
        JLabel rowsLbl = new JLabel("Rows per page:");
        rowsLbl.setFont(ThemeUtil.FONT_SMALL);
        rowsLbl.setForeground(ThemeUtil.TEXT_MUTED);
        pageSizeCombo = new JComboBox<>(new Integer[]{10, 15, 25, 50, 100});
        pageSizeCombo.setSelectedItem(pageSize);
        pageSizeCombo.setFont(ThemeUtil.FONT_SMALL);
        pageSizeCombo.setPreferredSize(new Dimension(65, 28));
        pageSizeCombo.addActionListener(e -> {
            pageSize = (Integer) pageSizeCombo.getSelectedItem();
            currentPage = 0;
            renderPage();
        });
        leftPanel.add(rowsLbl);
        leftPanel.add(pageSizeCombo);
        paginationBar.add(leftPanel, BorderLayout.WEST);

        // Centre: page info
        pageInfoLabel = new JLabel("Page 1 of 1  (0 records)");
        pageInfoLabel.setFont(ThemeUtil.FONT_SMALL);
        pageInfoLabel.setForeground(ThemeUtil.TEXT_MUTED);
        pageInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        paginationBar.add(pageInfoLabel, BorderLayout.CENTER);

        // Right: navigation buttons
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        navPanel.setOpaque(false);

        firstBtn = navBtn("\u00AB");
        prevBtn  = navBtn("\u2039");
        nextBtn  = navBtn("\u203A");
        lastBtn  = navBtn("\u00BB");

        firstBtn.addActionListener(e -> { currentPage = 0; renderPage(); });
        prevBtn.addActionListener(e -> { if (currentPage > 0) { currentPage--; renderPage(); } });
        nextBtn.addActionListener(e -> { if (currentPage < getTotalPages() - 1) { currentPage++; renderPage(); } });
        lastBtn.addActionListener(e -> { currentPage = Math.max(0, getTotalPages() - 1); renderPage(); });

        navPanel.add(firstBtn);
        navPanel.add(prevBtn);
        navPanel.add(nextBtn);
        navPanel.add(lastBtn);
        paginationBar.add(navPanel, BorderLayout.EAST);

        add(paginationBar, BorderLayout.SOUTH);
    }

    private JButton navBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(32, 28));
        btn.setBackground(ThemeUtil.BG_LIGHT);
        btn.setForeground(ThemeUtil.TEXT_PRIMARY);
        btn.setBorder(BorderFactory.createLineBorder(ThemeUtil.BORDER_COLOR));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(ThemeUtil.PRIMARY);
                btn.setForeground(Color.WHITE);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(ThemeUtil.BG_LIGHT);
                btn.setForeground(ThemeUtil.TEXT_PRIMARY);
            }
        });
        return btn;
    }

    /** Sets all data rows and re-renders the first page. */
    public void setData(List<Object[]> rows) {
        allRows.clear();
        allRows.addAll(rows);
        currentPage = 0;
        renderPage();
    }

    /** Clears all data. */
    public void clearData() {
        allRows.clear();
        currentPage = 0;
        renderPage();
    }

    /** Adds a single row. Call renderPage() after adding all rows. */
    public void addRow(Object[] row) {
        allRows.add(row);
    }

    /** Re-renders the current page from allRows. */
    public void renderPage() {
        model.setRowCount(0);
        int total = allRows.size();
        int totalPages = getTotalPages();
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, total);

        for (int i = start; i < end; i++) {
            model.addRow(allRows.get(i));
        }

        pageInfoLabel.setText(String.format("Page %d of %d  (%d records)",
            totalPages == 0 ? 0 : currentPage + 1, totalPages, total));

        firstBtn.setEnabled(currentPage > 0);
        prevBtn.setEnabled(currentPage > 0);
        nextBtn.setEnabled(currentPage < totalPages - 1);
        lastBtn.setEnabled(currentPage < totalPages - 1);
    }

    private int getTotalPages() {
        if (allRows.isEmpty()) return 1;
        return (int) Math.ceil((double) allRows.size() / pageSize);
    }

    public JTable getTable() { return table; }
    public DefaultTableModel getModel() { return model; }

    /** Applies a text filter across specified columns. */
    public void setFilter(String text, int... cols) {
        if (text == null || text.isBlank()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, cols));
        }
    }

    /** Sets a custom cell renderer on a column. */
    public void setColumnRenderer(int col, javax.swing.table.TableCellRenderer renderer) {
        table.getColumnModel().getColumn(col).setCellRenderer(renderer);
    }

    /** Sets max width on a column. */
    public void setColumnMaxWidth(int col, int width) {
        table.getColumnModel().getColumn(col).setMaxWidth(width);
    }

    /** Sets preferred width on a column. */
    public void setColumnPreferredWidth(int col, int width) {
        table.getColumnModel().getColumn(col).setPreferredWidth(width);
    }
}
