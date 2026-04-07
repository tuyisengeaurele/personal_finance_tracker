package com.financetracker.model;

/**
 * Aggregated financial data for a single calendar month.
 * Used for report generation and the dashboard line chart.
 */
public class MonthlySummary {

    private final String month;   // "YYYY-MM"
    private final double income;
    private final double expenses;

    public MonthlySummary(String month, double income, double expenses) {
        this.month    = month;
        this.income   = income;
        this.expenses = expenses;
    }

    public String getMonth()    { return month; }
    public double getIncome()   { return income; }
    public double getExpenses() { return expenses; }
    public double getBalance()  { return income - expenses; }

    @Override
    public String toString() {
        return String.format("MonthlySummary{month=%s, income=%.2f, expenses=%.2f}",
                month, income, expenses);
    }
}
