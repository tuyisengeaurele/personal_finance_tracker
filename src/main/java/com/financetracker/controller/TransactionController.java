package com.financetracker.controller;

import com.financetracker.model.Category;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import com.financetracker.service.CategoryService;
import com.financetracker.service.ExportService;
import com.financetracker.service.TransactionService;
import com.financetracker.util.AlertUtil;
import com.financetracker.util.CurrencyUtil;
import com.financetracker.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

/**
 * Controls the Transactions view: filterable/sortable table with Add/Edit/Delete/Export actions.
 */
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    // Filter controls
    @FXML private TextField         searchField;
    @FXML private ComboBox<String>  typeFilter;
    @FXML private ComboBox<Category> categoryFilter;
    @FXML private DatePicker        fromDatePicker;
    @FXML private DatePicker        toDatePicker;

    // Table
    @FXML private TableView<Transaction>          transactionTable;
    @FXML private TableColumn<Transaction, String> idCol;
    @FXML private TableColumn<Transaction, String> dateCol;
    @FXML private TableColumn<Transaction, String> typeCol;
    @FXML private TableColumn<Transaction, String> categoryCol;
    @FXML private TableColumn<Transaction, String> amountCol;
    @FXML private TableColumn<Transaction, String> descCol;

    // Action buttons
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    // Status
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingSpinner;

    private final TransactionService transactionService = new TransactionService();
    private final CategoryService    categoryService    = new CategoryService();
    private final ExportService      exportService      = new ExportService();

    private final ObservableList<Transaction> tableData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setupTableColumns();
        setupFilters();
        setupSelectionListener();
        loadData();
    }

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    private void setupTableColumns() {
        idCol.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getId())));

        dateCol.setCellValueFactory(c ->
                new SimpleStringProperty(DateUtil.formatDisplay(c.getValue().getDate())));
        dateCol.setComparator((a, b) -> {
            LocalDate da = DateUtil.parseOrNull(a);
            LocalDate db = DateUtil.parseOrNull(b);
            if (da == null || db == null) return 0;
            return da.compareTo(db);
        });

        typeCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getType().getDisplayName()));
        typeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("Income".equals(item)
                        ? "-fx-text-fill: #22c55e; -fx-font-weight: bold;"
                        : "-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            }
        });

        categoryCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getCategoryName()));

        amountCol.setCellValueFactory(c ->
                new SimpleStringProperty(CurrencyUtil.format(c.getValue().getAmount())));
        amountCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        descCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDescription() != null
                        ? c.getValue().getDescription() : ""));

        transactionTable.setItems(tableData);
        transactionTable.getSortOrder().add(dateCol);
    }

    private void setupFilters() {
        typeFilter.setItems(FXCollections.observableArrayList("All", "Income", "Expense"));
        typeFilter.setValue("All");

        try {
            List<Category> categories = categoryService.getAllCategories();
            Category all = new Category(0, "All Categories", "", "");
            ObservableList<Category> catList = FXCollections.observableArrayList();
            catList.add(all);
            catList.addAll(categories);
            categoryFilter.setItems(catList);
            categoryFilter.setValue(all);
        } catch (Exception e) {
            log.error("Failed to load categories", e);
        }

        // Auto-apply filters as user types or changes combos
        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        typeFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        categoryFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        fromDatePicker.valueProperty().addListener((obs, o, n) -> applyFilters());
        toDatePicker.valueProperty().addListener((obs, o, n) -> applyFilters());
    }

    private void setupSelectionListener() {
        transactionTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> {
                    boolean hasSelection = selected != null;
                    editButton.setDisable(!hasSelection);
                    deleteButton.setDisable(!hasSelection);
                });
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    // -------------------------------------------------------------------------
    // Data loading
    // -------------------------------------------------------------------------

    public void loadData() {
        applyFilters();
    }

    private void applyFilters() {
        loadingSpinner.setVisible(true);

        String       keyword    = searchField.getText();
        String       typeStr    = typeFilter.getValue();
        Category     cat        = categoryFilter.getValue();
        LocalDate    from       = fromDatePicker.getValue();
        LocalDate    to         = toDatePicker.getValue();

        TransactionType type = null;
        if ("Income".equals(typeStr))  type = TransactionType.INCOME;
        if ("Expense".equals(typeStr)) type = TransactionType.EXPENSE;

        Integer catId = (cat != null && cat.getId() != 0) ? cat.getId() : null;

        final TransactionType finalType = type;

        Task<List<Transaction>> task = new Task<>() {
            @Override
            protected List<Transaction> call() throws Exception {
                return transactionService.getFiltered(finalType, catId, from, to,
                        keyword.isBlank() ? null : keyword);
            }

            @Override
            protected void succeeded() {
                tableData.setAll(getValue());
                statusLabel.setText(getValue().size() + " transaction(s) found");
                loadingSpinner.setVisible(false);
            }

            @Override
            protected void failed() {
                log.error("Filter failed", getException());
                loadingSpinner.setVisible(false);
                statusLabel.setText("Error loading data");
            }
        };
        new Thread(task).start();
    }

    // -------------------------------------------------------------------------
    // Actions
    // -------------------------------------------------------------------------

    @FXML
    private void onAdd() {
        openDialog(null);
    }

    @FXML
    private void onEdit() {
        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected != null) openDialog(selected);
    }

    @FXML
    private void onDelete() {
        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        boolean confirmed = AlertUtil.confirm("Delete Transaction",
                "Delete this transaction? This action cannot be undone.");
        if (!confirmed) return;

        try {
            transactionService.deleteTransaction(selected.getId());
            loadData();
            statusLabel.setText("Transaction deleted.");
        } catch (Exception e) {
            log.error("Delete failed", e);
            AlertUtil.showError("Delete Failed", e.getMessage());
        }
    }

    @FXML
    private void onClearFilters() {
        searchField.clear();
        typeFilter.setValue("All");
        if (!categoryFilter.getItems().isEmpty()) categoryFilter.setValue(categoryFilter.getItems().get(0));
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
    }

    @FXML
    private void onExportCSV() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export to CSV");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        chooser.setInitialFileName("transactions_export.csv");

        File file = chooser.showSaveDialog(transactionTable.getScene().getWindow());
        if (file == null) return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                exportService.exportToCSV(tableData, file.toPath());
                return null;
            }
            @Override
            protected void succeeded() {
                AlertUtil.showInfo("Export Complete",
                        "Transactions exported to:\n" + file.getAbsolutePath());
            }
            @Override
            protected void failed() {
                log.error("CSV export failed", getException());
                AlertUtil.showError("Export Failed", getException().getMessage());
            }
        };
        new Thread(task).start();
    }

    @FXML
    private void onExportExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export to Excel");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        chooser.setInitialFileName("finance_report.xlsx");

        File file = chooser.showSaveDialog(transactionTable.getScene().getWindow());
        if (file == null) return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                var summaries = transactionService.getMonthlySummaries(12);
                exportService.exportToExcel(tableData, summaries, file.toPath());
                return null;
            }
            @Override
            protected void succeeded() {
                AlertUtil.showInfo("Export Complete",
                        "Report exported to:\n" + file.getAbsolutePath());
            }
            @Override
            protected void failed() {
                log.error("Excel export failed", getException());
                AlertUtil.showError("Export Failed", getException().getMessage());
            }
        };
        new Thread(task).start();
    }

    // -------------------------------------------------------------------------
    // Dialog
    // -------------------------------------------------------------------------

    private void openDialog(Transaction existing) {
        try {
            URL url = getClass().getResource("/fxml/TransactionDialog.fxml");
            if (url == null) throw new IOException("TransactionDialog.fxml not found");

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            TransactionDialogController ctrl = loader.getController();
            ctrl.setTransaction(existing);
            ctrl.setOnSaved(this::loadData);

            Stage dialog = new Stage();
            dialog.setTitle(existing == null ? "Add Transaction" : "Edit Transaction");
            dialog.setScene(new Scene(root));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (IOException e) {
            log.error("Failed to open transaction dialog", e);
            AlertUtil.showError("Error", "Could not open transaction form.");
        }
    }
}
