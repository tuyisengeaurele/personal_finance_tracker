package com.financetracker.model;

import javafx.beans.property.*;

import java.time.LocalDate;

/**
 * Represents a single financial transaction (income or expense).
 * Uses JavaFX properties so it can be bound directly to TableView columns.
 */
public class Transaction {

    private final IntegerProperty         id          = new SimpleIntegerProperty();
    private final ObjectProperty<TransactionType> type = new SimpleObjectProperty<>();
    private final DoubleProperty          amount      = new SimpleDoubleProperty();
    private final ObjectProperty<Category> category   = new SimpleObjectProperty<>();
    private final StringProperty          description = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> date      = new SimpleObjectProperty<>();
    private final StringProperty          createdAt   = new SimpleStringProperty();

    public Transaction() {}

    public Transaction(int id, TransactionType type, double amount,
                       Category category, String description, LocalDate date) {
        setId(id);
        setType(type);
        setAmount(amount);
        setCategory(category);
        setDescription(description);
        setDate(date);
    }

    // --- id ---
    public IntegerProperty idProperty() { return id; }
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    // --- type ---
    public ObjectProperty<TransactionType> typeProperty() { return type; }
    public TransactionType getType() { return type.get(); }
    public void setType(TransactionType type) { this.type.set(type); }

    // --- amount ---
    public DoubleProperty amountProperty() { return amount; }
    public double getAmount() { return amount.get(); }
    public void setAmount(double amount) { this.amount.set(amount); }

    // --- category ---
    public ObjectProperty<Category> categoryProperty() { return category; }
    public Category getCategory() { return category.get(); }
    public void setCategory(Category category) { this.category.set(category); }

    // --- description ---
    public StringProperty descriptionProperty() { return description; }
    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }

    // --- date ---
    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public LocalDate getDate() { return date.get(); }
    public void setDate(LocalDate date) { this.date.set(date); }

    // --- createdAt ---
    public StringProperty createdAtProperty() { return createdAt; }
    public String getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(String createdAt) { this.createdAt.set(createdAt); }

    /** Convenience: category name or empty string. */
    public String getCategoryName() {
        return category.get() != null ? category.get().getName() : "";
    }

    @Override
    public String toString() {
        return String.format("Transaction{id=%d, type=%s, amount=%.2f, category=%s, date=%s}",
                getId(), getType(), getAmount(), getCategoryName(), getDate());
    }
}
