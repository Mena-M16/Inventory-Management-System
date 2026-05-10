package com.inventory.utils;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for exporting JTable data to CSV.
 */
public class ExportUtils {

    private static final Logger LOGGER = Logger.getLogger(ExportUtils.class.getName());

    private ExportUtils() {}

    /**
     * Exports the given table model to a CSV file chosen by the user.
     *
     * @param parent parent component for the file chooser
     * @param model  the table model to export
     * @param defaultName suggested file name (without extension)
     */
    public static void exportToCSV(Component parent, TableModel model, String defaultName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(defaultName + "_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv"));
        int result = chooser.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            // Header row
            StringBuilder sb = new StringBuilder();
            for (int col = 0; col < model.getColumnCount(); col++) {
                if (col > 0) sb.append(",");
                sb.append(escapeCsv(model.getColumnName(col)));
            }
            pw.println(sb);

            // Data rows
            for (int row = 0; row < model.getRowCount(); row++) {
                sb = new StringBuilder();
                for (int col = 0; col < model.getColumnCount(); col++) {
                    if (col > 0) sb.append(",");
                    Object val = model.getValueAt(row, col);
                    sb.append(escapeCsv(val == null ? "" : val.toString()));
                }
                pw.println(sb);
            }
            NotificationUtils.showSuccess(parent, "Exported to " + file.getName());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "CSV export failed", e);
            NotificationUtils.showError(parent, "Export failed: " + e.getMessage());
        }
    }

    private static String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
