package com.financetracker.service;

import com.financetracker.dao.DatabaseInitializer;
import com.financetracker.model.Category;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CategoryService} business rules.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CategoryServiceTest {

    private static CategoryService service;
    private static int             createdCategoryId;

    @BeforeAll
    static void setup() throws SQLException {
        DatabaseInitializer.initialize();
        service = new CategoryService();
    }

    @Test
    @Order(1)
    void getAllCategories_shouldReturnSeededDefaults() throws SQLException {
        List<Category> cats = service.getAllCategories();
        assertFalse(cats.isEmpty(), "Default categories must be seeded");
        assertTrue(cats.stream().anyMatch(c -> c.getName().equals("Food")));
    }

    @Test
    @Order(2)
    void createCategory_validData_shouldReturnCategoryWithId() throws SQLException {
        Category c = service.createCategory("Test Category", "#ff0000", "★");
        createdCategoryId = c.getId();
        assertTrue(c.getId() > 0);
        assertEquals("Test Category", c.getName());
    }

    @Test
    @Order(3)
    void createCategory_blankName_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createCategory("", "#ff0000", "•"));
    }

    @Test
    @Order(4)
    void createCategory_nullName_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createCategory(null, "#ff0000", "•"));
    }

    @Test
    @Order(5)
    void getCategoryById_existingId_shouldReturnCategory() throws SQLException {
        var found = service.getCategoryById(createdCategoryId);
        assertTrue(found.isPresent());
        assertEquals("Test Category", found.get().getName());
    }

    @Test
    @Order(6)
    void getCategoryById_nonExistentId_shouldReturnEmpty() throws SQLException {
        var found = service.getCategoryById(Integer.MAX_VALUE);
        assertTrue(found.isEmpty());
    }

    @Test
    @Order(7)
    void updateCategory_shouldPersistChange() throws SQLException {
        Category c = service.getCategoryById(createdCategoryId).orElseThrow();
        c.setName("Updated Category");
        service.updateCategory(c);

        Category updated = service.getCategoryById(createdCategoryId).orElseThrow();
        assertEquals("Updated Category", updated.getName());
    }

    @Test
    @Order(8)
    void deleteCategory_withoutTransactions_shouldSucceed() throws SQLException {
        // deleteCategory is tested here on the newly-created category
        // (which has no transactions linked to it)
        assertDoesNotThrow(() -> service.deleteCategory(createdCategoryId));
        assertTrue(service.getCategoryById(createdCategoryId).isEmpty());
    }
}
