package com.financetracker.controller;

import com.financetracker.model.Budget;
import com.financetracker.model.Category;
import com.financetracker.service.BudgetService;
import com.financetracker.service.CategoryService;
import com.financetracker.util.AlertUtil;
import com.financetracker.util.CurrencyUtil;
import com.financetracker.util.DateUtil;
import com.financetracker.util.ThemeManager;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * Controls the Settings view: theme toggle, currency, budget management, categories.
 */
public class SettingsController {

    private static final Logger log = LoggerFactory.getLogger(SettingsController.class);

    // Appearance
    @FXML private ToggleButton darkModeToggle;
    @FXML private ComboBox<String> currencyCombo;

    // Budget management
    @FXML private ComboBox<Category> budgetCategoryCombo;
    @FXML private TextField          budgetAmountField;
    @FXML private TableView<Budget>  budgetTable;
    @FXML private TableColumn<Budget, String> budgetCatCol;
    @FXML private TableColumn<Budget, String> budgetAmtCol;
    @FXML private TableColumn<Budget, String> budgetSpentCol;
    @FXML private TableColumn<Budget, String> budgetStatusCol;

    // Category management
    @FXML private TextField  newCategoryName;
    @FXML private ColorPicker newCategoryColor;
    @FXML private TableView<Category> categoryTable;
    @FXML private TableColumn<Category, String> catNameCol;
    @FXML private TableColumn<Category, String> catColorCol;

    private final CategoryService categoryService = new CategoryService();
    private final BudgetService   budgetService   = new BudgetService();

    @FXML
    private void initialize() {
        setupAppearance();
        setupCurrencyCombo();
        setupBudgetSection();
        setupCategorySection();
    }

    // -------------------------------------------------------------------------
    // Appearance
    // -------------------------------------------------------------------------

    private void setupAppearance() {
        boolean isDark = ThemeManager.getInstance().getCurrentTheme() == ThemeManager.Theme.DARK;
        darkModeToggle.setSelected(isDark);
        darkModeToggle.setText(isDark ? "Dark Mode" : "Light Mode");

        darkModeToggle.selectedProperty().addListener((obs, o, selected) -> {
            ThemeManager.getInstance().setTheme(
                    selected ? ThemeManager.Theme.DARK : ThemeManager.Theme.LIGHT);
            darkModeToggle.setText(selected ? "Dark Mode" : "Light Mode");
        });
    }

    private void setupCurrencyCombo() {
        currencyCombo.setItems(FXCollections.observableArrayList(
                "USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "INR", "RWF"));
        currencyCombo.setValue(CurrencyUtil.getCurrencyCode());
        currencyCombo.valueProperty().addListener((obs, o, n) -> {
            if (n != null) CurrencyUtil.setCurrencyCode(n);
        });
    }

    // -------------------------------------------------------------------------
    // Budget
    // -------------------------------------------------------------------------

    private void setupBudgetSection() {
        budgetCatCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getCategory().getIcon() + " " +
                        c.getValue().getCategory().getName()));

        budgetAmtCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        CurrencyUtil.format(c.getValue().getAmount())));

        budgetSpentCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        CurrencyUtil.format(c.getValue().getSpent())));

        budgetStatusCol.setCellValueFactory(c -> {
            Budget b = c.getValue();
            String pct = String.format("%.0f%%", b.getPercentageUsed());
            return new javafx.beans.property.SimpleStringProperty(
                    b.isOverBudget() ? "OVER (" + pct + ")" : "OK (" + pct + ")");
        });
        budgetStatusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle(item.startsWith("OVER")
                        ? "-fx-text-fill: #ef4444; -fx-font-weight: bold;"
                        : "-fx-text-fill: #22c55e;");
            }
        });

        loadBudgets();
        loadCategoriesForBudget();
    }

    private void loadBudgets() {
        Task<List<Budget>> task = new Task<>() {
            @Override
            protected List<Budget> call() throws Exception {
                return budgetService.getCurrentMonthBudgets();
            }
            @Override
            protected void succeeded() {
                budgetTable.setItems(FXCollections.observableArrayList(getValue()));
            }
            @Override
            protected void failed() {
                log.error("Load budgets failed", getException());
            }
        };
        new Thread(task).start();
    }

    private void loadCategoriesForBudget() {
        try {
            budgetCategoryCombo.setItems(FXCollections.observableArrayList(
                    categoryService.getAllCategories()));
        } catch (SQLException e) {
            log.error("Load categories failed", e);
        }
    }

    @FXML
    private void onSaveBudget() {
        Category cat = budgetCategoryCombo.getValue();
        if (cat == null) {
            AlertUtil.showWarning("Validation", "Please select a category.");
            return;
        }
        String amtText = budgetAmountField.getText().strip();
        if (!isValidAmount(amtText)) {
            AlertUtil.showWarning("Validation", "Please enter a valid positive amount.");
            return;
        }
        double amount = Double.parseDouble(amtText);
        try {
            budgetService.setBudget(cat, amount, DateUtil.currentMonthKey());
            budgetAmountField.clear();
            loadBudgets();
            AlertUtil.showInfo("Budget Saved",
                    "Budget of " + CurrencyUtil.format(amount) +
                    " set for " + cat.getName() + " this month.");
        } catch (Exception e) {
            log.error("Save budget failed", e);
            AlertUtil.showError("Save Failed", e.getMessage());
        }
    }

    @FXML
    private void onDeleteBudget() {
        Budget selected = budgetTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (!AlertUtil.confirm("Delete Budget", "Remove this budget?")) return;
        try {
            budgetService.deleteBudget(selected.getId());
            loadBudgets();
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Categories
    // -------------------------------------------------------------------------

    private void setupCategorySection() {
        catNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        catColorCol.setCellValueFactory(new PropertyValueFactory<>("color"));
        loadCategoryTable();
    }

    private void loadCategoryTable() {
        try {
            categoryTable.setItems(FXCollections.observableArrayList(
                    categoryService.getAllCategories()));
        } catch (SQLException e) {
            log.error("Load categories failed", e);
        }
    }

    @FXML
    private void onAddCategory() {
        String name = newCategoryName.getText().strip();
        if (name.isBlank()) {
            AlertUtil.showWarning("Validation", "Category name is required.");
            return;
        }
        String color = toHex(newCategoryColor.getValue());
        try {
            categoryService.createCategory(name, color, "•");
            newCategoryName.clear();
            loadCategoryTable();
            loadCategoriesForBudget();
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void onDeleteCategory() {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (!AlertUtil.confirm("Delete Category",
                "Delete '" + selected.getName() + "'? Categories with transactions cannot be deleted.")) return;
        try {
            categoryService.deleteCategory(selected.getId());
            loadCategoryTable();
        } catch (IllegalStateException e) {
            AlertUtil.showWarning("Cannot Delete", e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private boolean isValidAmount(String text) {
        try { return Double.parseDouble(text) > 0; }
        catch (NumberFormatException e) { return false; }
    }

    private String toHex(javafx.scene.paint.Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed()   * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue()  * 255));
    }
}
