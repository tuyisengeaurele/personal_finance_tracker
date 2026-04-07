package com.financetracker.dao;

import com.financetracker.model.Category;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link TransactionDAO}.
 * Uses an in-memory SQLite database so no files are created on disk.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransactionDAOTest {

    private static TransactionDAO dao;
    private static Category       testCategory;

    @BeforeAll
    static void setup() throws SQLException {
        // Point DatabaseConnection to an in-memory DB for testing
        System.setProperty("finance.test.mode", "true");
        DatabaseInitializer.initialize();

        // Ensure we have a usable category
        CategoryDAO catDao = new CategoryDAO();
        List<Category> cats = catDao.findAll();
        assertFalse(cats.isEmpty(), "Default categories should be seeded");
        testCategory = cats.get(0);

        dao = new TransactionDAO();
    }

    @Test
    @Order(1)
    void create_shouldPersistAndReturnGeneratedId() throws SQLException {
        Transaction t = buildTransaction(TransactionType.INCOME, 1500.00);
        Transaction saved = dao.create(t);

        assertTrue(saved.getId() > 0, "ID should be generated after insert");
        assertEquals(1500.00, saved.getAmount(), 0.001);
        assertEquals(TransactionType.INCOME, saved.getType());
    }

    @Test
    @Order(2)
    void findAll_shouldReturnAtLeastOneRow() throws SQLException {
        List<Transaction> all = dao.findAll();
        assertFalse(all.isEmpty());
    }

    @Test
    @Order(3)
    void findById_shouldReturnCorrectTransaction() throws SQLException {
        Transaction created = dao.create(buildTransaction(TransactionType.EXPENSE, 99.99));
        var found = dao.findById(created.getId());

        assertTrue(found.isPresent());
        assertEquals(99.99, found.get().getAmount(), 0.001);
        assertEquals(TransactionType.EXPENSE, found.get().getType());
    }

    @Test
    @Order(4)
    void update_shouldPersistChanges() throws SQLException {
        Transaction t = dao.create(buildTransaction(TransactionType.EXPENSE, 50.00));
        t.setAmount(75.00);
        t.setDescription("Updated description");
        dao.update(t);

        Transaction fetched = dao.findById(t.getId()).orElseThrow();
        assertEquals(75.00, fetched.getAmount(), 0.001);
        assertEquals("Updated description", fetched.getDescription());
    }

    @Test
    @Order(5)
    void delete_shouldRemoveTransaction() throws SQLException {
        Transaction t = dao.create(buildTransaction(TransactionType.INCOME, 200.00));
        int id = t.getId();
        dao.delete(id);

        assertTrue(dao.findById(id).isEmpty(), "Transaction should be gone after delete");
    }

    @Test
    @Order(6)
    void findFiltered_byType_shouldReturnOnlyMatchingType() throws SQLException {
        dao.create(buildTransaction(TransactionType.INCOME,  100.00));
        dao.create(buildTransaction(TransactionType.EXPENSE, 200.00));

        List<Transaction> incomes  = dao.findFiltered(TransactionType.INCOME,  null, null, null, null);
        List<Transaction> expenses = dao.findFiltered(TransactionType.EXPENSE, null, null, null, null);

        incomes.forEach(t  -> assertEquals(TransactionType.INCOME,  t.getType()));
        expenses.forEach(t -> assertEquals(TransactionType.EXPENSE, t.getType()));
    }

    @Test
    @Order(7)
    void findFiltered_byKeyword_shouldMatchDescription() throws SQLException {
        Transaction t = buildTransaction(TransactionType.EXPENSE, 42.00);
        t.setDescription("Netflix subscription");
        dao.create(t);

        List<Transaction> results = dao.findFiltered(null, null, null, null, "netflix");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(r -> r.getDescription().toLowerCase().contains("netflix")));
    }

    @Test
    @Order(8)
    void sumIncomeForMonth_shouldReturnCorrectTotal() throws SQLException {
        String month = LocalDate.now().getYear() + "-" +
                       String.format("%02d", LocalDate.now().getMonthValue());

        double before = dao.sumIncomeForMonth(month);

        Transaction t = buildTransaction(TransactionType.INCOME, 300.00);
        dao.create(t);

        double after = dao.sumIncomeForMonth(month);
        assertEquals(300.00, after - before, 0.01);
    }

    @Test
    @Order(9)
    void getMonthlySummaries_shouldNotThrow() throws SQLException {
        assertDoesNotThrow(() -> dao.getMonthlySummaries(6));
    }

    // -------------------------------------------------------------------------

    private Transaction buildTransaction(TransactionType type, double amount) {
        Transaction t = new Transaction();
        t.setType(type);
        t.setAmount(amount);
        t.setCategory(testCategory);
        t.setDescription("Test transaction");
        t.setDate(LocalDate.now());
        return t;
    }
}
