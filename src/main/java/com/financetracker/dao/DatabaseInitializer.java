package com.financetracker.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates the database schema on first run and seeds default categories.
 * All statements use IF NOT EXISTS / ON CONFLICT IGNORE so subsequent
 * application starts are safely idempotent.
 *
 * <p>Each logical step gets its own connection so no shared state leaks
 * between schema creation and data seeding.
 */
public final class DatabaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private DatabaseInitializer() {}

    public static void initialize() throws SQLException {
        createSchema();
        seedDefaultCategories();
        log.info("Database initialised successfully.");
    }

    // -------------------------------------------------------------------------

    private static void createSchema() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement  stmt = conn.createStatement()) {

            conn.setAutoCommit(false);
            try {
                stmt.execute("""
                        CREATE TABLE IF NOT EXISTS categories (
                            id    INTEGER PRIMARY KEY AUTOINCREMENT,
                            name  TEXT    NOT NULL UNIQUE,
                            color TEXT    NOT NULL DEFAULT '#6366f1',
                            icon  TEXT    NOT NULL DEFAULT '•'
                        )
                        """);

                stmt.execute("""
                        CREATE TABLE IF NOT EXISTS transactions (
                            id          INTEGER PRIMARY KEY AUTOINCREMENT,
                            type        TEXT    NOT NULL CHECK(type IN ('INCOME','EXPENSE')),
                            amount      REAL    NOT NULL CHECK(amount > 0),
                            category_id INTEGER NOT NULL,
                            description TEXT    DEFAULT '',
                            date        TEXT    NOT NULL,
                            created_at  TEXT    NOT NULL DEFAULT (datetime('now')),
                            FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
                        )
                        """);

                stmt.execute("""
                        CREATE TABLE IF NOT EXISTS budgets (
                            id          INTEGER PRIMARY KEY AUTOINCREMENT,
                            category_id INTEGER NOT NULL,
                            amount      REAL    NOT NULL CHECK(amount > 0),
                            month       TEXT    NOT NULL,
                            UNIQUE(category_id, month),
                            FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
                        )
                        """);

                stmt.execute("""
                        CREATE INDEX IF NOT EXISTS idx_transactions_date
                        ON transactions(date)
                        """);

                stmt.execute("""
                        CREATE INDEX IF NOT EXISTS idx_transactions_category
                        ON transactions(category_id)
                        """);

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static void seedDefaultCategories() throws SQLException {
        // Check first with its own connection
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (var rs = conn.createStatement()
                    .executeQuery("SELECT COUNT(*) FROM categories")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return; // already seeded
                }
            }
        }

        // Seed with a separate connection
        String sql = "INSERT OR IGNORE INTO categories (name, color, icon) VALUES (?, ?, ?)";
        Object[][] defaults = {
                {"Salary",        "#22c55e", "💼"},
                {"Freelance",     "#10b981", "💻"},
                {"Investment",    "#3b82f6", "📈"},
                {"Food",          "#f97316", "🍔"},
                {"Transport",     "#8b5cf6", "🚗"},
                {"Housing",       "#ef4444", "🏠"},
                {"Healthcare",    "#ec4899", "💊"},
                {"Entertainment", "#f59e0b", "🎬"},
                {"Shopping",      "#06b6d4", "🛍️"},
                {"Education",     "#6366f1", "📚"},
                {"Bills",         "#64748b", "📄"},
                {"Other",         "#94a3b8", "•"},
        };

        try (Connection conn = DatabaseConnection.getConnection();
             var ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            try {
                for (Object[] row : defaults) {
                    ps.setString(1, (String) row[0]);
                    ps.setString(2, (String) row[1]);
                    ps.setString(3, (String) row[2]);
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
                log.info("Default categories seeded.");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
