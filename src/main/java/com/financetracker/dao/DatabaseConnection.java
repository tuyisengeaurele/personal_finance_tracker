package com.financetracker.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides a thread-safe SQLite connection using a simple singleton pattern.
 *
 * The database file is stored in the user's home directory under
 * {@code ~/.financetracker/finance.db}, ensuring the app works
 * without elevated permissions on any OS.
 */
public class DatabaseConnection {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnection.class);

    private static final String DB_DIR_NAME  = ".financetracker";
    private static final String DB_FILE_NAME = "finance.db";

    private static Connection instance;

    private DatabaseConnection() {}

    /**
     * Returns (and lazily creates) the singleton {@link Connection}.
     *
     * @return an open SQLite JDBC connection
     * @throws SQLException if the connection cannot be established
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (instance == null || instance.isClosed()) {
            instance = createConnection();
        }
        return instance;
    }

    private static Connection createConnection() throws SQLException {
        Path dbDir  = Paths.get(System.getProperty("user.home"), DB_DIR_NAME);
        Path dbFile = dbDir.resolve(DB_FILE_NAME);

        try {
            Files.createDirectories(dbDir);
        } catch (IOException e) {
            throw new SQLException("Cannot create database directory: " + dbDir, e);
        }

        String url = "jdbc:sqlite:" + dbFile.toAbsolutePath();
        log.info("Connecting to SQLite database: {}", dbFile.toAbsolutePath());

        Connection conn = DriverManager.getConnection(url);

        // Enable WAL for better concurrent read performance
        try (var stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL");
            stmt.execute("PRAGMA foreign_keys=ON");
        }

        return conn;
    }

    /** Closes the singleton connection (called on application shutdown). */
    public static synchronized void close() {
        if (instance != null) {
            try {
                instance.close();
                log.info("Database connection closed.");
            } catch (SQLException e) {
                log.warn("Error closing database connection", e);
            } finally {
                instance = null;
            }
        }
    }
}
