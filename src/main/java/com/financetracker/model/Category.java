package com.financetracker.model;

import javafx.beans.property.*;

/**
 * Represents a transaction category (e.g. Food, Transport, Salary).
 */
public class Category {

    private final IntegerProperty id    = new SimpleIntegerProperty();
    private final StringProperty  name  = new SimpleStringProperty();
    private final StringProperty  color = new SimpleStringProperty();
    private final StringProperty  icon  = new SimpleStringProperty();

    public Category() {}

    public Category(int id, String name, String color, String icon) {
        setId(id);
        setName(name);
        setColor(color);
        setIcon(icon);
    }

    public Category(String name, String color, String icon) {
        setName(name);
        setColor(color);
        setIcon(icon);
    }

    // --- id ---
    public IntegerProperty idProperty() { return id; }
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    // --- name ---
    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }

    // --- color ---
    public StringProperty colorProperty() { return color; }
    public String getColor() { return color.get(); }
    public void setColor(String color) { this.color.set(color); }

    // --- icon ---
    public StringProperty iconProperty() { return icon; }
    public String getIcon() { return icon.get(); }
    public void setIcon(String icon) { this.icon.set(icon); }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category other)) return false;
        return getId() == other.getId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getId());
    }
}
