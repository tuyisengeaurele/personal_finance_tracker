package com.financetracker.controller;

import com.financetracker.model.Budget;
import com.financetracker.model.MonthlySummary;
import com.financetracker.model.Transaction;
import com.financetracker.service.BudgetService;
import com.financetracker.service.TransactionService;
import com.financetracker.util.AlertUtil;
import com.financetracker.util.CurrencyUtil;
import com.financetracker.util.DateUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Controls the Dashboard view: summary cards, recent transactions,
 * pie chart (category breakdown), and line chart (monthly trend).
 */
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    // Summary cards
    @FXML private Label balanceLabel;
    @FXML private Label monthIncomeLabel;
    @FXML private Label monthExpenseLabel;
    @FXML private Label savingsLabel;

    // Charts
    @FXML private PieChart       pieChart;
    @FXML private LineChart<String, Number> lineChart;
    @FXML private CategoryAxis   lineChartXAxis;
    @FXML private NumberAxis     lineChartYAxis;

    // Recent transactions
    @FXML private TableView<Transaction>          recentTable;
    @FXML private TableColumn<Transaction, String> recentDateCol;
    @FXML private TableColumn<Transaction, String> recentTypeCol;
    @FXML private TableColumn<Transaction, String> recentCategoryCol;
    @FXML private TableColumn<Transaction, String> recentAmountCol;
    @FXML private TableColumn<Transaction, String> recentDescCol;

    // Budget alerts
    @FXML private VBox budgetAlertsBox;
    @FXML private Label budgetAlertsTitle;

    // Loading indicator
    @FXML private ProgressIndicator loadingIndicator;

    private final TransactionService transactionService = new TransactionService();
    private final BudgetService      budgetService      = new BudgetService();

    @FXML
    private void initialize() {
        setupTableColumns();
        loadDashboardData();
    }

    // -------------------------------------------------------------------------
    // Table setup
    // -------------------------------------------------------------------------

    private void setupTableColumns() {
        recentDateCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        DateUtil.formatDisplay(c.getValue().getDate())));

        recentTypeCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getType().getDisplayName()));
        recentTypeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("INCOME".equals(getTableView().getItems()
                            .get(getIndex()).getType().name())
                            ? "-fx-text-fill: #22c55e; -fx-font-weight: bold;"
                            : "-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                }
            }
        });

        recentCategoryCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getCategoryName()));

        recentAmountCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        CurrencyUtil.format(c.getValue().getAmount())));

        recentDescCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getDescription()));
    }

    // -------------------------------------------------------------------------
    // Data loading
    // -------------------------------------------------------------------------

    public void loadDashboardData() {
        loadingIndicator.setVisible(true);

        Task<Void> task = new Task<>() {
            double balance, income, expenses;
            List<Transaction> recent;
            List<Object[]>    categoryData;
            List<MonthlySummary> trend;
            List<Budget>      alerts;

            @Override
            protected Void call() throws Exception {
                balance      = transactionService.getTotalBalance();
                income       = transactionService.getCurrentMonthIncome();
                expenses     = transactionService.getCurrentMonthExpenses();
                recent       = transactionService.getAllTransactions().stream().limit(10).toList();
                categoryData = transactionService.getExpensesByCategory(DateUtil.currentMonthKey());
                trend        = transactionService.getMonthlySummaries(6);
                alerts       = budgetService.getOverBudgetAlerts();
                return null;
            }

            @Override
            protected void succeeded() {
                updateSummaryCards(balance, income, expenses);
                updateRecentTable(recent);
                updatePieChart(categoryData);
                updateLineChart(trend);
                updateBudgetAlerts(alerts);
                loadingIndicator.setVisible(false);
            }

            @Override
            protected void failed() {
                log.error("Dashboard load failed", getException());
                loadingIndicator.setVisible(false);
                AlertUtil.showError("Load Error", "Failed to load dashboard data.");
            }
        };

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void updateSummaryCards(double balance, double income, double expenses) {
        balanceLabel.setText(CurrencyUtil.format(balance));
        monthIncomeLabel.setText(CurrencyUtil.format(income));
        monthExpenseLabel.setText(CurrencyUtil.format(expenses));
        double savings = income > 0 ? ((income - expenses) / income) * 100 : 0;
        savingsLabel.setText(String.format("%.1f%%", Math.max(savings, 0)));

        // Colour the balance red if negative
        balanceLabel.setStyle(balance < 0 ? "-fx-text-fill: #ef4444;" : "");
    }

    private void updateRecentTable(List<Transaction> transactions) {
        recentTable.setItems(FXCollections.observableArrayList(transactions));
    }

    private void updatePieChart(List<Object[]> data) {
        pieChart.getData().clear();
        if (data.isEmpty()) {
            pieChart.setTitle("No expenses this month");
            return;
        }
        pieChart.setTitle("Expenses by Category — " + DateUtil.currentMonthKey());
        for (Object[] row : data) {
            String name  = (String) row[0];
            double total = (double) row[2];
            PieChart.Data slice = new PieChart.Data(
                    name + " (" + CurrencyUtil.format(total) + ")", total);
            pieChart.getData().add(slice);
        }
    }

    private void updateLineChart(List<MonthlySummary> summaries) {
        lineChart.getData().clear();

        XYChart.Series<String, Number> incomeSeries  = new XYChart.Series<>();
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        expenseSeries.setName("Expenses");

        for (MonthlySummary s : summaries) {
            incomeSeries.getData().add(new XYChart.Data<>(s.getMonth(), s.getIncome()));
            expenseSeries.getData().add(new XYChart.Data<>(s.getMonth(), s.getExpenses()));
        }

        lineChart.getData().addAll(incomeSeries, expenseSeries);
    }

    private void updateBudgetAlerts(List<Budget> alerts) {
        budgetAlertsBox.getChildren().clear();
        if (alerts.isEmpty()) {
            budgetAlertsTitle.setText("All budgets on track");
            budgetAlertsTitle.setStyle("-fx-text-fill: #22c55e;");
            return;
        }
        budgetAlertsTitle.setText("Over-Budget Alerts (" + alerts.size() + ")");
        budgetAlertsTitle.setStyle("-fx-text-fill: #ef4444;");

        for (Budget b : alerts) {
            HBox row = new HBox(10);
            row.getStyleClass().add("alert-row");

            Label catLabel = new Label(b.getCategory().getIcon() + " " + b.getCategory().getName());
            catLabel.getStyleClass().add("alert-category");

            Label amtLabel = new Label(
                    CurrencyUtil.format(b.getSpent()) + " / " + CurrencyUtil.format(b.getAmount()));
            amtLabel.getStyleClass().add("alert-amount");

            row.getChildren().addAll(catLabel, amtLabel);
            budgetAlertsBox.getChildren().add(row);
        }
    }
}
