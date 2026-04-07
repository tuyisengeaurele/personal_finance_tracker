package com.financetracker.dao;

import com.financetracker.model.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for {@link Category} entities.
 * All SQL uses PreparedStatements to prevent injection.
 */
public class CategoryDAO {

    private static final Logger log = LoggerFactory.getLogger(CategoryDAO.class);

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    public Category create(Category category) throws SQLException {
        String sql = "INSERT INTO categories (name, color, icon) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, sanitize(category.getName()));
            ps.setString(2, sanitize(category.getColor()));
            ps.setString(3, sanitize(category.getIcon()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    category.setId(keys.getInt(1));
                }
            }
            log.debug("Created category: {}", category.getName());
            return category;
        }
    }

    public Optional<Category> findById(int id) throws SQLException {
        String sql = "SELECT id, name, color, icon FROM categories WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Category> findAll() throws SQLException {
        String sql = "SELECT id, name, color, icon FROM categories ORDER BY name";
        List<Category> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void update(Category category) throws SQLException {
        String sql = "UPDATE categories SET name = ?, color = ?, icon = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sanitize(category.getName()));
            ps.setString(2, sanitize(category.getColor()));
            ps.setString(3, sanitize(category.getIcon()));
            ps.setInt(4, category.getId());
            ps.executeUpdate();
            log.debug("Updated category id={}", category.getId());
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            log.debug("Deleted category id={}", id);
        }
    }

    /** Returns true if any transaction references this category. */
    public boolean hasTransactions(int categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM transactions WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Category mapRow(ResultSet rs) throws SQLException {
        return new Category(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("color"),
                rs.getString("icon")
        );
    }

    /** Basic sanitisation — strip leading/trailing whitespace. */
    private String sanitize(String value) {
        return value == null ? "" : value.strip();
    }
}
