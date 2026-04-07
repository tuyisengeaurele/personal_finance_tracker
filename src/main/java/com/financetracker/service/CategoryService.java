package com.financetracker.service;

import com.financetracker.dao.CategoryDAO;
import com.financetracker.model.Category;
import com.financetracker.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for category management.
 */
public class CategoryService {

    private final CategoryDAO dao;

    public CategoryService() {
        this.dao = new CategoryDAO();
    }

    /** Visible for testing. */
    CategoryService(CategoryDAO dao) {
        this.dao = dao;
    }

    public Category createCategory(String name, String color, String icon) throws SQLException {
        ValidationUtil.requireNonBlank(name, "Category name");
        ValidationUtil.requireMaxLength(name, 50, "Category name");

        String sanitizedName = name.strip();
        Category category = new Category(sanitizedName,
                color  != null ? color : "#6366f1",
                icon   != null ? icon  : "•");
        return dao.create(category);
    }

    public void updateCategory(Category category) throws SQLException {
        ValidationUtil.requireNonNull(category, "Category");
        ValidationUtil.requireNonBlank(category.getName(), "Category name");
        dao.update(category);
    }

    public void deleteCategory(int id) throws SQLException {
        if (dao.hasTransactions(id)) {
            throw new IllegalStateException(
                    "Cannot delete a category that has existing transactions.");
        }
        dao.delete(id);
    }

    public List<Category> getAllCategories() throws SQLException {
        return dao.findAll();
    }

    public Optional<Category> getCategoryById(int id) throws SQLException {
        return dao.findById(id);
    }
}
