package com.financetracker.service;

import com.financetracker.dao.DatabaseInitializer;
import com.financetracker.dao.CategoryDAO;
import com.financetracker.model.Category;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit / integration tests for {@link TransactionService}.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransactionServiceTest {

    private static TransactionService service;
    private static Category           category;

    @BeforeAll
    static void setup() throws SQLException {
        DatabaseInitializer.initialize();
        service  = new TransactionService();
        category = new CategoryDAO().findAll().get(0);
    }

    @Test
    @Order(1)
    void addTransaction_validData_shouldSucceed() throws SQLException {
        Transaction t = service.addTransaction(
                TransactionType.INCOME, 2000.00, category, "Salary", LocalDate.now());

        assertNotNull(t);
        assertTrue(t.getId() > 0);
        assertEquals(TransactionType.INCOME, t.getType());
        assertEquals(2000.00, t.getAmount(), 0.001);
    }

    @Test
    @Order(2)
    void addTransaction_negativeAmount_shouldThrowIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                service.addTransaction(TransactionType.EXPENSE, -50.00, category,
                        "Invalid", LocalDate.now()));
    }

    @Test
    @Order(3)
    void addTransaction_zeroAmount_shouldThrowIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                service.addTransaction(TransactionType.EXPENSE, 0, category,
                        "Zero", LocalDate.now()));
    }

    @Test
    @Order(4)
    void addTransaction_nullCategory_shouldThrowIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                service.addTransaction(TransactionType.EXPENSE, 10.00, null,
                        "No category", LocalDate.now()));
    }

    @Test
    @Order(5)
    void addTransaction_futureDate_shouldThrowIllegalArgument() {
        LocalDate future = LocalDate.now().plusDays(30);
        assertThrows(IllegalArgumentException.class, () ->
                service.addTransaction(TransactionType.INCOME, 100.00, category,
                        "Future", future));
    }

    @Test
    @Order(6)
    void getAllTransactions_shouldReturnNonEmptyList() throws SQLException {
        List<Transaction> all = service.getAllTransactions();
        assertFalse(all.isEmpty());
    }

    @Test
    @Order(7)
    void updateTransaction_shouldPersistChanges() throws SQLException {
        Transaction t = service.addTransaction(
                TransactionType.EXPENSE, 100.00, category, "Original", LocalDate.now());
        t.setAmount(150.00);
        t.setDescription("Updated");
        service.updateTransaction(t);

        Transaction updated = service.getById(t.getId()).orElseThrow();
        assertEquals(150.00, updated.getAmount(), 0.001);
        assertEquals("Updated", updated.getDescription());
    }

    @Test
    @Order(8)
    void deleteTransaction_shouldRemoveRecord() throws SQLException {
        Transaction t = service.addTransaction(
                TransactionType.EXPENSE, 25.00, category, "To be deleted", LocalDate.now());
        int id = t.getId();
        service.deleteTransaction(id);

        assertTrue(service.getById(id).isEmpty());
    }

    @Test
    @Order(9)
    void getCurrentMonthIncome_shouldReturnPositiveOrZero() throws SQLException {
        double income = service.getCurrentMonthIncome();
        assertTrue(income >= 0);
    }

    @Test
    @Order(10)
    void getCurrentMonthExpenses_shouldReturnPositiveOrZero() throws SQLException {
        double expenses = service.getCurrentMonthExpenses();
        assertTrue(expenses >= 0);
    }

    @Test
    @Order(11)
    void getMonthlySummaries_shouldReturnList() throws SQLException {
        var summaries = service.getMonthlySummaries(3);
        assertNotNull(summaries);
    }
}
