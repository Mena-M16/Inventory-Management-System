package com.inventory.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for generating PDF reports from JTable data using iText.
 */
public class PDFGenerator {

    private static final Logger LOGGER = Logger.getLogger(PDFGenerator.class.getName());
    private static final BaseColor HEADER_COLOR = new BaseColor(33, 150, 243);
    private static final BaseColor ALT_ROW_COLOR = new BaseColor(240, 248, 255);

    private PDFGenerator() {}

    /**
     * Exports the given table model to a PDF file chosen by the user.
     *
     * @param parent      parent component for the file chooser
     * @param model       the table model to export
     * @param title       report title
     * @param defaultName suggested file name (without extension)
     */
    public static void exportToPDF(Component parent, TableModel model, String title, String defaultName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(defaultName + "_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try {
            Document doc = new Document(PageSize.A4.rotate(), 20, 20, 40, 30);
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.DARK_GRAY);
            Paragraph titlePara = new Paragraph(title, titleFont);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            titlePara.setSpacingAfter(6);
            doc.add(titlePara);

            // Timestamp
            Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.GRAY);
            Paragraph datePara = new Paragraph("Generated: " + new SimpleDateFormat("dd MMM yyyy HH:mm").format(new Date()), subFont);
            datePara.setAlignment(Element.ALIGN_CENTER);
            datePara.setSpacingAfter(12);
            doc.add(datePara);

            // Table
            PdfPTable table = new PdfPTable(model.getColumnCount());
            table.setWidthPercentage(100);

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
            for (int col = 0; col < model.getColumnCount(); col++) {
                PdfPCell cell = new PdfPCell(new Phrase(model.getColumnName(col), headerFont));
                cell.setBackgroundColor(HEADER_COLOR);
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < model.getColumnCount(); col++) {
                    Object val = model.getValueAt(row, col);
                    PdfPCell cell = new PdfPCell(new Phrase(val == null ? "" : val.toString(), dataFont));
                    cell.setPadding(5);
                    if (row % 2 == 1) cell.setBackgroundColor(ALT_ROW_COLOR);
                    table.addCell(cell);
                }
            }
            doc.add(table);
            doc.close();
            NotificationUtils.showSuccess(parent, "PDF saved: " + file.getName());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "PDF export failed", e);
            NotificationUtils.showError(parent, "PDF export failed: " + e.getMessage());
        }
    }
}
