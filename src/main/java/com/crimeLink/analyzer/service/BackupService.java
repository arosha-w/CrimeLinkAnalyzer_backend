package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.entity.BackupMetadata;
import com.crimeLink.analyzer.repository.BackupMetadataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
public class BackupService {

    private final BackupMetadataRepository backupRepo;
    private final JdbcTemplate jdbcTemplate;

    // Whitelist pattern: only alphanumeric, underscores, hyphens, and a single .sql extension
    private static final Pattern SAFE_FILENAME = Pattern.compile("^[a-zA-Z0-9_\\-]+\\.sql$");

    @Value("${backup.directory:./backups}")
    private String backupDirectory;

    public BackupService(BackupMetadataRepository backupRepo, JdbcTemplate jdbcTemplate) {
        this.backupRepo = backupRepo;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Create a full database backup using pure JDBC.
     * Exports all user tables as SQL INSERT statements.
     */
    public BackupMetadata createBackup(String userEmail) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String filename = "backup_" + timestamp + ".sql";

        Path backupFile = resolveSafeBackupPath(filename);
        Path backupDir = backupFile.getParent();

        BackupMetadata metadata = new BackupMetadata();
        metadata.setFilename(filename);
        metadata.setCreatedBy(userEmail);
        metadata.setCreatedAt(LocalDateTime.now());

        try {
            // Ensure backup directory exists
            Files.createDirectories(backupDir);

            // Get all user table names (exclude system tables)
            List<String> tables = getTableNames();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(backupFile.toFile()))) {
                writer.write("-- CrimeLink Analyzer Database Backup\n");
                writer.write("-- Created: " + LocalDateTime.now() + "\n");
                writer.write("-- Created by: " + userEmail + "\n");
                writer.write("-- Tables: " + tables.size() + "\n\n");

                for (String table : tables) {
                    writer.write("\n-- ========================================\n");
                    writer.write("-- Table: " + table + "\n");
                    writer.write("-- ========================================\n\n");

                    // Export table data as INSERT statements
                    exportTableData(writer, table);
                }

                writer.write("\n-- Backup complete\n");
            }

            // Record file size
            File resultFile = backupFile.toFile();
            metadata.setSizeBytes(resultFile.length());
            metadata.setStatus("SUCCESS");
            backupRepo.save(metadata);

            log.info("Backup created successfully: {} ({} bytes) by {}",
                    sanitizeForLog(filename), resultFile.length(), sanitizeForLog(userEmail));

            return metadata;

        } catch (Exception e) {
            log.error("Backup failed: {}", sanitizeForLog(e.getMessage()), e);
            metadata.setStatus("FAILED");
            metadata.setSizeBytes(0L);
            backupRepo.save(metadata);
            throw new RuntimeException("Backup failed: " + e.getMessage(), e);
        }
    }

    /**
     * Restore database from a backup file by executing its SQL statements.
     */
    public void restoreBackup(String filename, String userEmail) {
        Path backupFile = resolveSafeBackupPath(filename);

        if (!Files.exists(backupFile)) {
            throw new IllegalArgumentException("Backup file not found: " + filename);
        }

        try {
            String sql = Files.readString(backupFile);

            // Split by semicolons and execute each statement
            String[] statements = sql.split(";");
            int executed = 0;

            for (String stmt : statements) {
                String trimmed = stmt.trim();
                // Skip empty lines and comments
                if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                    continue;
                }
                try {
                    jdbcTemplate.execute(trimmed);
                    executed++;
                } catch (Exception stmtEx) {
                    // Log and continue — some statements may fail on duplicates etc.
                    log.warn("Skipping failed statement: {}... Error: {}",
                            sanitizeForLog(trimmed.substring(0, Math.min(80, trimmed.length()))),
                            sanitizeForLog(stmtEx.getMessage()));
                }
            }

            log.info("Database restored from {} by {} ({} statements executed)",
                    sanitizeForLog(filename), sanitizeForLog(userEmail), executed);

        } catch (IOException e) {
            log.error("Restore failed: {}", sanitizeForLog(e.getMessage()), e);
            throw new RuntimeException("Restore failed: " + e.getMessage(), e);
        }
    }

    /**
     * List all backup metadata, most recent first.
     */
    public List<BackupMetadata> listBackups() {
        return backupRepo.findAllByOrderByCreatedAtDesc();
    }

    // ════════════════════════════════════════════════════════════════
    //  Private helpers
    // ════════════════════════════════════════════════════════════════

    /**
     * Get all user table names from the public schema.
     */
    private List<String> getTableNames() {
        return jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables " +
                        "WHERE table_schema = 'public' AND table_type = 'BASE TABLE' " +
                        "ORDER BY table_name",
                String.class
        );
    }

    /**
     * Export all rows of a table as INSERT statements.
     */
    private void exportTableData(BufferedWriter writer, String tableName) throws IOException {
        // Validate table name to prevent SQL injection (should only contain safe chars)
        if (!tableName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            log.warn("Skipping suspicious table name: {}", sanitizeForLog(tableName));
            return;
        }

        try {
            jdbcTemplate.query("SELECT * FROM \"" + tableName + "\"", (ResultSet rs) -> {
                try {
                    ResultSetMetaData meta = rs.getMetaData();
                    int columnCount = meta.getColumnCount();

                    // Build column name list
                    List<String> columns = new ArrayList<>();
                    for (int i = 1; i <= columnCount; i++) {
                        columns.add("\"" + meta.getColumnName(i) + "\"");
                    }
                    String columnList = String.join(", ", columns);

                    while (rs.next()) {
                        List<String> values = new ArrayList<>();
                        for (int i = 1; i <= columnCount; i++) {
                            Object val = rs.getObject(i);
                            if (val == null) {
                                values.add("NULL");
                            } else if (val instanceof Number) {
                                values.add(val.toString());
                            } else if (val instanceof Boolean) {
                                values.add(((Boolean) val) ? "TRUE" : "FALSE");
                            } else {
                                // Escape single quotes for SQL strings
                                String escaped = val.toString().replace("'", "''");
                                values.add("'" + escaped + "'");
                            }
                        }

                        writer.write("INSERT INTO \"" + tableName + "\" (" + columnList + ") VALUES ("
                                + String.join(", ", values) + ");\n");
                    }
                } catch (Exception e) {
                    log.error("Error exporting table {}: {}", sanitizeForLog(tableName), sanitizeForLog(e.getMessage()));
                }
            });
        } catch (Exception e) {
            writer.write("-- ERROR exporting table " + tableName + ": " + e.getMessage() + "\n");
            log.error("Failed to export table {}: {}", sanitizeForLog(tableName), sanitizeForLog(e.getMessage()));
        }
    }

    /**
     * Confirms the requested filename is safe, resolves and normalizes the path,
     * and strictly checks that it falls inside the base backup directory.
     */
    private Path resolveSafeBackupPath(String filename) {
        if (filename == null || !SAFE_FILENAME.matcher(filename).matches()) {
            throw new IllegalArgumentException(
                    "Invalid filename. Only alphanumeric characters, underscores, hyphens, and .sql extension are allowed.");
        }

        Path baseDir = Paths.get(backupDirectory).toAbsolutePath().normalize();
        Path resolvedPath = baseDir.resolve(filename).normalize();

        if (!resolvedPath.startsWith(baseDir)) {
            throw new IllegalArgumentException("Path traversal attempt detected.");
        }

        return resolvedPath;
    }

    /**
     * Sanitizes strings for safe logging to prevent log injection.
     */
    private String sanitizeForLog(String input) {
        if (input == null) return "null";
        return input.replaceAll("[\\r\\n\\t]", "_");
    }
}
