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
 * Provides new SQLite JDBC connections on every call.
 *
 * <h3>Why not a singleton connection?</h3>
 * A single shared {@link Connection} object is not thread-safe: if a
 * background {@link javafx.concurrent.Task} and the JavaFX Application
 * Thread both call {@code getConnection()} at the same time they receive
 * the <em>same</em> object. When one thread closes its
 * {@code PreparedStatement} or {@code ResultSet}, the other thread's
 * cursor can become invalid ("stmt pointer is closed").
 *
 * <p>SQLite in WAL mode ({@code PRAGMA journal_mode=WAL}) allows multiple
 * concurrent readers, so opening a fresh connection per DAO call is both
 * safe and efficient for a desktop application.
 *
 * <p>All callers use try-with-resources, so every connection is closed
 * immediately after the query completes — no connection leak.
 *
 * <h3>Database location</h3>
 * {@code ~/.financetracker/finance.db}
 * Created automatically if it does not exist.
 */
public final class DatabaseConnection {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnection.class);

    private static final String DB_DIR_NAME  = ".financetracker";
    private static final String DB_FILE_NAME = "finance.db";

    /** Cached URL — the path never changes after the first call. */
    private static volatile String jdbcUrl;

    private DatabaseConnection() {}

    /**
     * Opens and returns a brand-new SQLite connection with WAL mode and
     * foreign-key support enabled.
     *
     * <p><strong>Always call this inside a try-with-resources block</strong>
     * so the connection is properly closed.
     *
     * @return an open, configured {@link Connection}
     * @throws SQLException if the DB file cannot be created or opened
     */
    public static Connection getConnection() throws SQLException {
        String url = resolveUrl();
        Connection conn = DriverManager.getConnection(url);
        try (var stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL");
            stmt.execute("PRAGMA foreign_keys=ON");
        } catch (SQLException e) {
            conn.close();
            throw e;
        }
        return conn;
    }

    // -------------------------------------------------------------------------

    private static String resolveUrl() throws SQLException {
        if (jdbcUrl != null) return jdbcUrl;

        synchronized (DatabaseConnection.class) {
            if (jdbcUrl != null) return jdbcUrl;

            Path dbDir  = Paths.get(System.getProperty("user.home"), DB_DIR_NAME);
            Path dbFile = dbDir.resolve(DB_FILE_NAME);

            try {
                Files.createDirectories(dbDir);
            } catch (IOException e) {
                throw new SQLException("Cannot create database directory: " + dbDir, e);
            }

            jdbcUrl = "jdbc:sqlite:" + dbFile.toAbsolutePath();
            log.info("SQLite database path: {}", dbFile.toAbsolutePath());
        }
        return jdbcUrl;
    }
}
