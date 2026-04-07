package com.financetracker.service;

import com.financetracker.dao.BudgetDAO;
import com.financetracker.model.Budget;
import com.financetracker.model.Category;
import com.financetracker.util.ValidationUtil;

import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Business logic for budget management and over-budget detection.
 */
public class BudgetService {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final BudgetDAO            budgetDAO;
    private final TransactionService   transactionService;

    public BudgetService() {
        this.budgetDAO          = new BudgetDAO();
        this.transactionService = new TransactionService();
    }

    /** Visible for testing. */
    BudgetService(BudgetDAO budgetDAO, TransactionService transactionService) {
        this.budgetDAO          = budgetDAO;
        this.transactionService = transactionService;
    }

    public Budget setBudget(Category category, double amount, String month) throws SQLException {
        ValidationUtil.requireNonNull(category, "Category");
        ValidationUtil.requirePositive(amount, "Budget amount");
        ValidationUtil.requireNonBlank(month, "Month");

        Budget budget = budgetDAO
                .findByCategoryAndMonth(category.getId(), month)
                .orElseGet(Budget::new);

        budget.setCategory(category);
        budget.setAmount(amount);
        budget.setMonth(month);
        return budgetDAO.save(budget);
    }

    public void deleteBudget(int id) throws SQLException {
        budgetDAO.delete(id);
    }

    /**
     * Returns budgets for the given month with actual spending populated.
     */
    public List<Budget> getBudgetsForMonth(String month) throws SQLException {
        List<Budget> budgets = budgetDAO.findByMonth(month);
        for (Budget b : budgets) {
            double spent = transactionService
                    .getExpensesForCategoryAndMonth(b.getCategory().getId(), month);
            b.setSpent(spent);
        }
        return budgets;
    }

    public List<Budget> getCurrentMonthBudgets() throws SQLException {
        return getBudgetsForMonth(YearMonth.now().format(MONTH_FMT));
    }

    /** Returns budgets that are over the limit for the current month. */
    public List<Budget> getOverBudgetAlerts() throws SQLException {
        return getCurrentMonthBudgets().stream()
                .filter(Budget::isOverBudget)
                .toList();
    }
}
