package com.financetracker.dao;

import com.financetracker.model.Category;
import com.financetracker.model.MonthlySummary;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for {@link Transaction} entities.
 * Supports full CRUD plus filtered queries and monthly aggregation.
 */
public class TransactionDAO {

    private static final Logger log = LoggerFactory.getLogger(TransactionDAO.class);

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    public Transaction create(Transaction t) throws SQLException {
        String sql = """
                INSERT INTO transactions (type, amount, category_id, description, date, created_at)
                VALUES (?, ?, ?, ?, ?, datetime('now'))
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bindTransaction(ps, t);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    t.setId(keys.getInt(1));
                }
            }
            log.debug("Created transaction id={}", t.getId());
            return t;
        }
    }

    public Optional<Transaction> findById(int id) throws SQLException {
        String sql = buildSelectBase() + " WHERE t.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public List<Transaction> findAll() throws SQLException {
        String sql = buildSelectBase() + " ORDER BY t.date DESC, t.id DESC";
        return executeQuery(sql);
    }

    public void update(Transaction t) throws SQLException {
        String sql = """
                UPDATE transactions
                SET type = ?, amount = ?, category_id = ?, description = ?, date = ?
                WHERE id = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindTransaction(ps, t);
            ps.setInt(6, t.getId());
            ps.executeUpdate();
            log.debug("Updated transaction id={}", t.getId());
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            log.debug("Deleted transaction id={}", id);
        }
    }

    // -------------------------------------------------------------------------
    // Filtered queries
    // -------------------------------------------------------------------------

    /**
     * Flexible search with all parameters optional (pass null to skip).
     */
    public List<Transaction> findFiltered(TransactionType type,
                                          Integer categoryId,
                                          LocalDate from,
                                          LocalDate to,
                                          String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder(buildSelectBase()).append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (type != null) {
            sql.append(" AND t.type = ?");
            params.add(type.name());
        }
        if (categoryId != null) {
            sql.append(" AND t.category_id = ?");
            params.add(categoryId);
        }
        if (from != null) {
            sql.append(" AND t.date >= ?");
            params.add(from.toString());
        }
        if (to != null) {
            sql.append(" AND t.date <= ?");
            params.add(to.toString());
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (LOWER(t.description) LIKE ? OR LOWER(c.name) LIKE ?)");
            String kw = "%" + keyword.toLowerCase().strip() + "%";
            params.add(kw);
            params.add(kw);
        }

        sql.append(" ORDER BY t.date DESC, t.id DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                return collectRows(rs);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Aggregation
    // -------------------------------------------------------------------------

    /** Total income for a given year-month string (e.g. "2024-03"). */
    public double sumIncomeForMonth(String month) throws SQLException {
        return sumForMonthAndType(month, "INCOME");
    }

    /** Total expenses for a given year-month string. */
    public double sumExpensesForMonth(String month) throws SQLException {
        return sumForMonthAndType(month, "EXPENSE");
    }

    private double sumForMonthAndType(String month, String type) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(amount), 0)
                FROM transactions
                WHERE type = ? AND strftime('%Y-%m', date) = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, type);
            ps.setString(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        }
    }

    /** Returns per-category totals for expenses in a given month. */
    public List<Object[]> sumExpensesByCategory(String month) throws SQLException {
        String sql = """
                SELECT c.name, c.color, COALESCE(SUM(t.amount), 0) AS total
                FROM transactions t
                JOIN categories c ON t.category_id = c.id
                WHERE t.type = 'EXPENSE'
                  AND strftime('%Y-%m', t.date) = ?
                GROUP BY c.id
                ORDER BY total DESC
                """;
        List<Object[]> results = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new Object[]{
                            rs.getString("name"),
                            rs.getString("color"),
                            rs.getDouble("total")
                    });
                }
            }
        }
        return results;
    }

    /** Returns monthly summaries for the last N months. */
    public List<MonthlySummary> getMonthlySummaries(int months) throws SQLException {
        String sql = """
                SELECT strftime('%Y-%m', date) AS month,
                       COALESCE(SUM(CASE WHEN type='INCOME'  THEN amount ELSE 0 END), 0) AS income,
                       COALESCE(SUM(CASE WHEN type='EXPENSE' THEN amount ELSE 0 END), 0) AS expenses
                FROM transactions
                WHERE date >= date('now', ? || ' months')
                GROUP BY month
                ORDER BY month ASC
                """;
        List<MonthlySummary> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "-" + months);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new MonthlySummary(
                            rs.getString("month"),
                            rs.getDouble("income"),
                            rs.getDouble("expenses")
                    ));
                }
            }
        }
        return list;
    }

    /** Total expenses for a given month and category. */
    public double sumExpensesForMonthAndCategory(String month, int categoryId) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(amount), 0)
                FROM transactions
                WHERE type = 'EXPENSE'
                  AND category_id = ?
                  AND strftime('%Y-%m', date) = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            ps.setString(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String buildSelectBase() {
        return """
                SELECT t.id, t.type, t.amount, t.description, t.date, t.created_at,
                       c.id AS cat_id, c.name AS cat_name, c.color AS cat_color, c.icon AS cat_icon
                FROM transactions t
                JOIN categories c ON t.category_id = c.id
                """;
    }

    private void bindTransaction(PreparedStatement ps, Transaction t) throws SQLException {
        ps.setString(1, t.getType().name());
        ps.setDouble(2, t.getAmount());
        ps.setInt(3, t.getCategory().getId());
        ps.setString(4, t.getDescription() != null ? t.getDescription().strip() : "");
        ps.setString(5, t.getDate().toString());
    }

    private List<Transaction> executeQuery(String sql) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return collectRows(rs);
        }
    }

    private List<Transaction> collectRows(ResultSet rs) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return list;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Category cat = new Category(
                rs.getInt("cat_id"),
                rs.getString("cat_name"),
                rs.getString("cat_color"),
                rs.getString("cat_icon")
        );
        Transaction t = new Transaction(
                rs.getInt("id"),
                TransactionType.fromString(rs.getString("type")),
                rs.getDouble("amount"),
                cat,
                rs.getString("description"),
                LocalDate.parse(rs.getString("date"))
        );
        t.setCreatedAt(rs.getString("created_at"));
        return t;
    }
}
