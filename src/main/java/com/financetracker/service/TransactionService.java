package com.financetracker.service;

import com.financetracker.dao.TransactionDAO;
import com.financetracker.model.Category;
import com.financetracker.model.MonthlySummary;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import com.financetracker.util.ValidationUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for managing financial transactions.
 */
public class TransactionService {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final TransactionDAO dao;

    public TransactionService() {
        this.dao = new TransactionDAO();
    }

    /** Visible for testing. */
    TransactionService(TransactionDAO dao) {
        this.dao = dao;
    }

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    public Transaction addTransaction(TransactionType type,
                                      double amount,
                                      Category category,
                                      String description,
                                      LocalDate date) throws SQLException {
        ValidationUtil.requireNonNull(type, "Transaction type");
        ValidationUtil.requirePositive(amount, "Amount");
        ValidationUtil.requireNonNull(category, "Category");
        ValidationUtil.requireNonNull(date, "Date");

        if (date.isAfter(LocalDate.now().plusDays(1))) {
            throw new IllegalArgumentException("Transaction date cannot be in the future.");
        }

        Transaction t = new Transaction();
        t.setType(type);
        t.setAmount(amount);
        t.setCategory(category);
        t.setDescription(description != null ? description.strip() : "");
        t.setDate(date);
        return dao.create(t);
    }

    public void updateTransaction(Transaction transaction) throws SQLException {
        ValidationUtil.requireNonNull(transaction, "Transaction");
        ValidationUtil.requirePositive(transaction.getAmount(), "Amount");
        ValidationUtil.requireNonNull(transaction.getCategory(), "Category");
        dao.update(transaction);
    }

    public void deleteTransaction(int id) throws SQLException {
        dao.delete(id);
    }

    public Optional<Transaction> getById(int id) throws SQLException {
        return dao.findById(id);
    }

    public List<Transaction> getAllTransactions() throws SQLException {
        return dao.findAll();
    }

    // -------------------------------------------------------------------------
    // Filtering
    // -------------------------------------------------------------------------

    public List<Transaction> getFiltered(TransactionType type,
                                         Integer categoryId,
                                         LocalDate from,
                                         LocalDate to,
                                         String keyword) throws SQLException {
        return dao.findFiltered(type, categoryId, from, to, keyword);
    }

    // -------------------------------------------------------------------------
    // Analytics
    // -------------------------------------------------------------------------

    /** Returns total income for the current calendar month. */
    public double getCurrentMonthIncome() throws SQLException {
        return dao.sumIncomeForMonth(currentMonth());
    }

    /** Returns total expenses for the current calendar month. */
    public double getCurrentMonthExpenses() throws SQLException {
        return dao.sumExpensesForMonth(currentMonth());
    }

    /** Returns net balance (all-time income minus all-time expenses). */
    public double getTotalBalance() throws SQLException {
        List<MonthlySummary> all = dao.getMonthlySummaries(120); // 10 years
        double income   = all.stream().mapToDouble(MonthlySummary::getIncome).sum();
        double expenses = all.stream().mapToDouble(MonthlySummary::getExpenses).sum();
        return income - expenses;
    }

    /** Returns per-category expense breakdown for a given month. */
    public List<Object[]> getExpensesByCategory(String month) throws SQLException {
        return dao.sumExpensesByCategory(month);
    }

    /** Returns monthly summaries for the last N months (for the line chart). */
    public List<MonthlySummary> getMonthlySummaries(int months) throws SQLException {
        return dao.getMonthlySummaries(months);
    }

    /** Expenses for a specific category and month (used for budget tracking). */
    public double getExpensesForCategoryAndMonth(int categoryId, String month) throws SQLException {
        return dao.sumExpensesForMonthAndCategory(month, categoryId);
    }

    private String currentMonth() {
        return YearMonth.now().format(MONTH_FMT);
    }
}
