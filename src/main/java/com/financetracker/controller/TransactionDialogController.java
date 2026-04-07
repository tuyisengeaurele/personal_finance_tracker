package com.financetracker.controller;

import com.financetracker.model.Category;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import com.financetracker.service.CategoryService;
import com.financetracker.service.TransactionService;
import com.financetracker.util.AlertUtil;
import com.financetracker.util.ValidationUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

/**
 * Controls the Add / Edit Transaction modal dialog.
 */
public class TransactionDialogController {

    private static final Logger log = LoggerFactory.getLogger(TransactionDialogController.class);

    @FXML private ToggleGroup       typeGroup;
    @FXML private RadioButton       incomeRadio;
    @FXML private RadioButton       expenseRadio;
    @FXML private TextField         amountField;
    @FXML private ComboBox<Category> categoryCombo;
    @FXML private TextArea          descriptionArea;
    @FXML private DatePicker        datePicker;
    @FXML private Button            saveButton;
    @FXML private Label             errorLabel;

    private final TransactionService transactionService = new TransactionService();
    private final CategoryService    categoryService    = new CategoryService();

    private Transaction editingTransaction;
    private Runnable    onSaved;

    @FXML
    private void initialize() {
        loadCategories();
        datePicker.setValue(LocalDate.now());
        errorLabel.setText("");

        // Real-time validation feedback
        amountField.textProperty().addListener((obs, o, n) -> validateAmount(n));
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public void setTransaction(Transaction transaction) {
        this.editingTransaction = transaction;
        if (transaction != null) {
            populateForm(transaction);
        }
    }

    public void setOnSaved(Runnable callback) {
        this.onSaved = callback;
    }

    // -------------------------------------------------------------------------
    // Form setup
    // -------------------------------------------------------------------------

    private void loadCategories() {
        try {
            List<Category> cats = categoryService.getAllCategories();
            categoryCombo.setItems(FXCollections.observableArrayList(cats));
            if (!cats.isEmpty()) categoryCombo.setValue(cats.get(0));
        } catch (Exception e) {
            log.error("Failed to load categories", e);
            AlertUtil.showError("Error", "Could not load categories.");
        }
    }

    private void populateForm(Transaction t) {
        if (t.getType() == TransactionType.INCOME) {
            incomeRadio.setSelected(true);
        } else {
            expenseRadio.setSelected(true);
        }
        amountField.setText(String.format("%.2f", t.getAmount()));
        categoryCombo.setValue(t.getCategory());
        descriptionArea.setText(t.getDescription() != null ? t.getDescription() : "");
        datePicker.setValue(t.getDate());
    }

    // -------------------------------------------------------------------------
    // Actions
    // -------------------------------------------------------------------------

    @FXML
    private void onSave() {
        if (!validateForm()) return;

        try {
            TransactionType type   = incomeRadio.isSelected()
                    ? TransactionType.INCOME : TransactionType.EXPENSE;
            double          amount = Double.parseDouble(amountField.getText().strip());
            Category        cat    = categoryCombo.getValue();
            String          desc   = descriptionArea.getText().strip();
            LocalDate       date   = datePicker.getValue();

            if (editingTransaction == null) {
                transactionService.addTransaction(type, amount, cat, desc, date);
                log.info("Transaction added successfully.");
            } else {
                editingTransaction.setType(type);
                editingTransaction.setAmount(amount);
                editingTransaction.setCategory(cat);
                editingTransaction.setDescription(desc);
                editingTransaction.setDate(date);
                transactionService.updateTransaction(editingTransaction);
                log.info("Transaction updated id={}", editingTransaction.getId());
            }

            if (onSaved != null) onSaved.run();
            closeDialog();

        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            log.error("Save failed", e);
            AlertUtil.showError("Save Failed", e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        closeDialog();
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private boolean validateForm() {
        errorLabel.setText("");

        if (!ValidationUtil.isValidAmount(amountField.getText())) {
            errorLabel.setText("Please enter a valid positive amount.");
            amountField.requestFocus();
            return false;
        }
        if (categoryCombo.getValue() == null) {
            errorLabel.setText("Please select a category.");
            return false;
        }
        if (datePicker.getValue() == null) {
            errorLabel.setText("Please select a date.");
            return false;
        }
        if (datePicker.getValue().isAfter(LocalDate.now())) {
            errorLabel.setText("Transaction date cannot be in the future.");
            return false;
        }
        return true;
    }

    private void validateAmount(String text) {
        if (text.isBlank()) { amountField.setStyle(""); return; }
        boolean valid = ValidationUtil.isValidAmount(text);
        amountField.setStyle(valid ? "-fx-border-color: #22c55e;" : "-fx-border-color: #ef4444;");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void closeDialog() {
        ((Stage) saveButton.getScene().getWindow()).close();
    }
}
