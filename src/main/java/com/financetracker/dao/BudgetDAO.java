package com.financetracker.dao;

import com.financetracker.model.Budget;
import com.financetracker.model.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for {@link Budget} entities.
 */
public class BudgetDAO {

    private static final Logger log = LoggerFactory.getLogger(BudgetDAO.class);

    public Budget save(Budget budget) throws SQLException {
        if (budget.getId() == 0) {
            return insert(budget);
        }
        update(budget);
        return budget;
    }

    private Budget insert(Budget budget) throws SQLException {
        String sql = """
                INSERT INTO budgets (category_id, amount, month)
                VALUES (?, ?, ?)
                ON CONFLICT(category_id, month) DO UPDATE SET amount = excluded.amount
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, budget.getCategory().getId());
            ps.setDouble(2, budget.getAmount());
            ps.setString(3, budget.getMonth());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) budget.setId(keys.getInt(1));
            }
            log.debug("Saved budget for category={} month={}", budget.getCategory().getName(), budget.getMonth());
            return budget;
        }
    }

    private void update(Budget budget) throws SQLException {
        String sql = "UPDATE budgets SET amount = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, budget.getAmount());
            ps.setInt(2, budget.getId());
            ps.executeUpdate();
        }
    }

    public List<Budget> findByMonth(String month) throws SQLException {
        String sql = """
                SELECT b.id, b.amount, b.month,
                       c.id AS cat_id, c.name AS cat_name, c.color AS cat_color, c.icon AS cat_icon
                FROM budgets b
                JOIN categories c ON b.category_id = c.id
                WHERE b.month = ?
                ORDER BY c.name
                """;
        List<Budget> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Optional<Budget> findByCategoryAndMonth(int categoryId, String month) throws SQLException {
        String sql = """
                SELECT b.id, b.amount, b.month,
                       c.id AS cat_id, c.name AS cat_name, c.color AS cat_color, c.icon AS cat_icon
                FROM budgets b
                JOIN categories c ON b.category_id = c.id
                WHERE b.category_id = ? AND b.month = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            ps.setString(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM budgets WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Budget mapRow(ResultSet rs) throws SQLException {
        Category cat = new Category(
                rs.getInt("cat_id"),
                rs.getString("cat_name"),
                rs.getString("cat_color"),
                rs.getString("cat_icon")
        );
        return new Budget(
                rs.getInt("id"),
                cat,
                rs.getDouble("amount"),
                rs.getString("month")
        );
    }
}
