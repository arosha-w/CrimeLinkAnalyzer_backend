package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.entity.BackupMetadata;
import com.crimeLink.analyzer.repository.BackupMetadataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BackupService {

    private final BackupMetadataRepository backupRepo;

    // Whitelist pattern: only alphanumeric, underscores, hyphens, and a single .sql extension
    private static final Pattern SAFE_FILENAME = Pattern.compile("^[a-zA-Z0-9_\\-]+\\.sql$");

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${backup.directory:./backups}")
    private String backupDirectory;

    public BackupService(BackupMetadataRepository backupRepo) {
        this.backupRepo = backupRepo;
    }

    /**
     * Create a full database backup using pg_dump.
     */
    public BackupMetadata createBackup(String userEmail) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String filename = "backup_" + timestamp + ".sql";

        Path backupDir = Paths.get(backupDirectory);
        Path backupFile = backupDir.resolve(filename);

        BackupMetadata metadata = new BackupMetadata();
        metadata.setFilename(filename);
        metadata.setCreatedBy(userEmail);
        metadata.setCreatedAt(LocalDateTime.now());

        try {
            // Ensure backup directory exists
            Files.createDirectories(backupDir);

            // Parse JDBC URL to extract host, port, dbName
            DbConnectionInfo connInfo = parseJdbcUrl(dbUrl);

            ProcessBuilder pb = new ProcessBuilder(
                    "pg_dump",
                    "-h", connInfo.host,
                    "-p", String.valueOf(connInfo.port),
                    "-U", dbUsername,
                    "-F", "p",           // plain SQL format
                    "-f", backupFile.toAbsolutePath().toString(),
                    connInfo.database
            );

            // Pass password via environment variable (more secure than command line)
            pb.environment().put("PGPASSWORD", dbPassword);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // Read process output for logging
            String output;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                output = reader.lines().collect(Collectors.joining("\n"));
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                log.error("pg_dump failed with exit code {}: {}", exitCode, output);
                metadata.setStatus("FAILED");
                metadata.setSizeBytes(0L);
                backupRepo.save(metadata);
                throw new RuntimeException("pg_dump failed (exit code " + exitCode + "): " + output);
            }

            // Record file size
            File resultFile = backupFile.toFile();
            metadata.setSizeBytes(resultFile.length());
            metadata.setStatus("SUCCESS");
            backupRepo.save(metadata);

            log.info("Backup created successfully: {} ({} bytes) by {}",
                    filename, resultFile.length(), userEmail);

            return metadata;

        } catch (RuntimeException e) {
            throw e; // re-throw RuntimeExceptions as-is
        } catch (Exception e) {
            log.error("Backup failed: {}", e.getMessage(), e);
            metadata.setStatus("FAILED");
            metadata.setSizeBytes(0L);
            backupRepo.save(metadata);
            throw new RuntimeException("Backup failed: " + e.getMessage(), e);
        }
    }

    /**
     * Restore database from a backup file using psql.
     */
    public void restoreBackup(String filename, String userEmail) {
        // Sanitize filename to prevent path traversal
        if (!SAFE_FILENAME.matcher(filename).matches()) {
            throw new IllegalArgumentException(
                    "Invalid filename. Only alphanumeric characters, underscores, hyphens, and .sql extension are allowed.");
        }

        Path backupFile = Paths.get(backupDirectory).resolve(filename);

        if (!Files.exists(backupFile)) {
            throw new IllegalArgumentException("Backup file not found: " + filename);
        }

        try {
            DbConnectionInfo connInfo = parseJdbcUrl(dbUrl);

            ProcessBuilder pb = new ProcessBuilder(
                    "psql",
                    "-h", connInfo.host,
                    "-p", String.valueOf(connInfo.port),
                    "-U", dbUsername,
                    "-d", connInfo.database,
                    "-f", backupFile.toAbsolutePath().toString()
            );

            pb.environment().put("PGPASSWORD", dbPassword);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            String output;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                output = reader.lines().collect(Collectors.joining("\n"));
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                log.error("psql restore failed with exit code {}: {}", exitCode, output);
                throw new RuntimeException("Restore failed (exit code " + exitCode + "): " + output);
            }

            log.info("Database restored from {} by {}", filename, userEmail);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Restore failed: {}", e.getMessage(), e);
            throw new RuntimeException("Restore failed: " + e.getMessage(), e);
        }
    }

    /**
     * List all backup metadata, most recent first.
     */
    public List<BackupMetadata> listBackups() {
        return backupRepo.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Parse a JDBC PostgreSQL URL into host, port, and database components.
     * Supports: jdbc:postgresql://host:port/database and ?param=value query strings.
     */
    private DbConnectionInfo parseJdbcUrl(String jdbcUrl) {
        try {
            // Remove "jdbc:" prefix so URI can parse it
            String uriString = jdbcUrl.substring(5);
            URI uri = new URI(uriString);

            String host = uri.getHost() != null ? uri.getHost() : "localhost";
            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String path = uri.getPath();
            String database = (path != null && path.length() > 1) ? path.substring(1) : "postgres";

            return new DbConnectionInfo(host, port, database);
        } catch (Exception e) {
            log.warn("Could not parse JDBC URL '{}', using defaults. Error: {}", jdbcUrl, e.getMessage());
            return new DbConnectionInfo("localhost", 5432, "postgres");
        }
    }

    private record DbConnectionInfo(String host, int port, String database) {}
}
