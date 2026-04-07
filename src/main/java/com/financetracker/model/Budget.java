package com.financetracker.model;

import javafx.beans.property.*;

/**
 * Represents a monthly spending budget for a specific category.
 */
public class Budget {

    private final IntegerProperty          id         = new SimpleIntegerProperty();
    private final ObjectProperty<Category> category   = new SimpleObjectProperty<>();
    private final DoubleProperty           amount     = new SimpleDoubleProperty();
    private final StringProperty           month      = new SimpleStringProperty(); // "YYYY-MM"
    private       double                   spent      = 0.0;  // populated by service layer

    public Budget() {}

    public Budget(int id, Category category, double amount, String month) {
        setId(id);
        setCategory(category);
        setAmount(amount);
        setMonth(month);
    }

    // --- id ---
    public IntegerProperty idProperty() { return id; }
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    // --- category ---
    public ObjectProperty<Category> categoryProperty() { return category; }
    public Category getCategory() { return category.get(); }
    public void setCategory(Category category) { this.category.set(category); }

    // --- amount ---
    public DoubleProperty amountProperty() { return amount; }
    public double getAmount() { return amount.get(); }
    public void setAmount(double amount) { this.amount.set(amount); }

    // --- month ---
    public StringProperty monthProperty() { return month; }
    public String getMonth() { return month.get(); }
    public void setMonth(String month) { this.month.set(month); }

    // --- spent (runtime only, not persisted) ---
    public double getSpent() { return spent; }
    public void setSpent(double spent) { this.spent = spent; }

    /** Returns how much of the budget remains. Negative means over-budget. */
    public double getRemaining() { return getAmount() - spent; }

    /** Returns spent percentage (0–100+). */
    public double getPercentageUsed() {
        return getAmount() > 0 ? (spent / getAmount()) * 100.0 : 0;
    }

    public boolean isOverBudget() {
        return spent > getAmount();
    }
}
