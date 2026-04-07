package com.financetracker.service;

import com.financetracker.model.MonthlySummary;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generates report data consumed by the Reports controller.
 * Delegates database queries to {@link TransactionService}.
 */
public class ReportService {

    private final TransactionService transactionService;

    public ReportService() {
        this.transactionService = new TransactionService();
    }

    ReportService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /** Per-category breakdown for expenses in the given month (YYYY-MM). */
    public List<Object[]> getCategoryBreakdown(String month) throws SQLException {
        return transactionService.getExpensesByCategory(month);
    }

    /** Monthly summaries for the last {@code months} months. */
    public List<MonthlySummary> getTrend(int months) throws SQLException {
        return transactionService.getMonthlySummaries(months);
    }

    /**
     * Detailed transactions in a date range, grouped by month key.
     */
    public Map<String, List<Transaction>> getTransactionsByMonth(LocalDate from,
                                                                   LocalDate to) throws SQLException {
        List<Transaction> all = transactionService.getFiltered(null, null, from, to, null);
        return all.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getDate().getYear() + "-" +
                             String.format("%02d", t.getDate().getMonthValue())
                ));
    }

    /** Net balance (income − expenses) over all time. */
    public double getNetBalance() throws SQLException {
        return transactionService.getTotalBalance();
    }

    /** Current month totals. */
    public double[] getCurrentMonthTotals() throws SQLException {
        return new double[]{
                transactionService.getCurrentMonthIncome(),
                transactionService.getCurrentMonthExpenses()
        };
    }

    /** Income vs expense ratio for a given period. */
    public double getSavingsRate(LocalDate from, LocalDate to) throws SQLException {
        List<Transaction> txs = transactionService.getFiltered(null, null, from, to, null);
        double income   = txs.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount).sum();
        double expenses = txs.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount).sum();
        return income > 0 ? ((income - expenses) / income) * 100.0 : 0;
    }
}
