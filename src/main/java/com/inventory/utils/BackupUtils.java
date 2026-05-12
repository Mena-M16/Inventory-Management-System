package com.inventory.utils;

import javax.swing.*;
import java.awt.Component;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for MySQL database backup and restore using mysqldump / mysql CLI tools.
 */
public class BackupUtils {

    private static final Logger LOGGER = Logger.getLogger(BackupUtils.class.getName());
    private static final String BACKUP_DIR = "backups";

    private BackupUtils() {}

    /**
     * Creates a mysqldump backup of the inventory_db database.
     *
     * @param parent   parent component for dialogs
     * @param host     DB host
     * @param port     DB port
     * @param dbName   database name
     * @param user     DB username
     * @param password DB password
     */
    public static void backup(Component parent, String host, String port,
                              String dbName, String user, String password) {
        File dir = new File(BACKUP_DIR);
        if (!dir.exists()) dir.mkdirs();

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File backupFile = new File(dir, dbName + "_" + timestamp + ".sql");

        try {
            ProcessBuilder pb = new ProcessBuilder(
                "mysqldump",
                "-h", host, "-P", port,
                "-u", user, "--password=" + password,
                dbName
            );
            pb.redirectOutput(backupFile);
            pb.redirectErrorStream(false);
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                pruneOldBackups(dir, 7);
                NotificationUtils.showSuccess(parent, "Backup saved: " + backupFile.getName());
            } else {
                NotificationUtils.showError(parent, "Backup failed (exit code " + exitCode + "). Ensure mysqldump is on PATH.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Backup failed", e);
            NotificationUtils.showError(parent, "Backup failed: " + e.getMessage());
        }
    }

    /**
     * Restores a database from a chosen SQL dump file.
     */
    public static void restore(Component parent, String host, String port,
                               String dbName, String user, String password) {
        JFileChooser chooser = new JFileChooser(BACKUP_DIR);
        chooser.setDialogTitle("Select backup file to restore");
        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!NotificationUtils.showConfirm(parent, "Restore from " + file.getName() + "?\nThis will overwrite current data!")) return;

        try {
            ProcessBuilder pb = new ProcessBuilder(
                "mysql",
                "-h", host, "-P", port,
                "-u", user, "--password=" + password,
                dbName
            );
            pb.redirectInput(file);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                NotificationUtils.showSuccess(parent, "Database restored from " + file.getName());
            } else {
                NotificationUtils.showError(parent, "Restore failed (exit code " + exitCode + ").");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Restore failed", e);
            NotificationUtils.showError(parent, "Restore failed: " + e.getMessage());
        }
    }

    /** Keeps only the most recent {@code keep} backup files. */
    private static void pruneOldBackups(File dir, int keep) {
        File[] files = dir.listFiles((d, name) -> name.endsWith(".sql"));
        if (files == null || files.length <= keep) return;
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        for (int i = 0; i < files.length - keep; i++) {
            files[i].delete();
        }
    }
}
