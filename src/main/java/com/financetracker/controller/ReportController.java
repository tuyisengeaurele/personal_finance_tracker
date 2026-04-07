package com.financetracker.controller;

import com.financetracker.model.MonthlySummary;
import com.financetracker.service.ReportService;
import com.financetracker.service.TransactionService;
import com.financetracker.service.ExportService;
import com.financetracker.util.AlertUtil;
import com.financetracker.util.CurrencyUtil;
import com.financetracker.util.DateUtil;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controls the Reports view: trend charts, category breakdown, and export.
 */
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    // Filter
    @FXML private DatePicker  fromDatePicker;
    @FXML private DatePicker  toDatePicker;
    @FXML private ComboBox<String> trendPeriodCombo;

    // Summary labels
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpenseLabel;
    @FXML private Label netBalanceLabel;
    @FXML private Label savingsRateLabel;

    // Charts
    @FXML private BarChart<String, Number> barChart;
    @FXML private CategoryAxis            barXAxis;
    @FXML private NumberAxis              barYAxis;
    @FXML private PieChart                pieChart;

    // Table
    @FXML private TableView<MonthlySummary>          summaryTable;
    @FXML private TableColumn<MonthlySummary, String> monthCol;
    @FXML private TableColumn<MonthlySummary, String> incomeCol;
    @FXML private TableColumn<MonthlySummary, String> expenseCol;
    @FXML private TableColumn<MonthlySummary, String> balanceCol;

    @FXML private ProgressIndicator spinner;

    private final ReportService      reportService      = new ReportService();
    private final TransactionService transactionService = new TransactionService();
    private final ExportService      exportService      = new ExportService();

    @FXML
    private void initialize() {
        setupTable();
        setupControls();
        loadReportData();
    }

    private void setupTable() {
        monthCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getMonth()));
        incomeCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        CurrencyUtil.format(c.getValue().getIncome())));
        expenseCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        CurrencyUtil.format(c.getValue().getExpenses())));
        balanceCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        CurrencyUtil.format(c.getValue().getBalance())));
        balanceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                double val = getTableView().getItems().get(getIndex()).getBalance();
                setStyle(val >= 0 ? "-fx-text-fill: #22c55e;" : "-fx-text-fill: #ef4444;");
            }
        });
    }

    private void setupControls() {
        trendPeriodCombo.setItems(FXCollections.observableArrayList(
                "3 Months", "6 Months", "12 Months", "24 Months"));
        trendPeriodCombo.setValue("12 Months");

        // Default date range: current year
        fromDatePicker.setValue(LocalDate.of(LocalDate.now().getYear(), 1, 1));
        toDatePicker.setValue(LocalDate.now());
    }

    private void loadReportData() {
        spinner.setVisible(true);
        int months = parseTrendPeriod();
        LocalDate from = fromDatePicker.getValue();
        LocalDate to   = toDatePicker.getValue();

        Task<Void> task = new Task<>() {
            double income, expenses, balance, savingsRate;
            List<MonthlySummary> trend;
            List<Object[]> categoryBreakdown;

            @Override
            protected Void call() throws Exception {
                trend             = reportService.getTrend(months);
                categoryBreakdown = reportService.getCategoryBreakdown(
                        YearMonth.now().format(MONTH_FMT));

                var txs   = transactionService.getFiltered(null, null, from, to, null);
                income    = txs.stream().filter(t -> t.getType().name().equals("INCOME"))
                               .mapToDouble(t -> t.getAmount()).sum();
                expenses  = txs.stream().filter(t -> t.getType().name().equals("EXPENSE"))
                               .mapToDouble(t -> t.getAmount()).sum();
                balance   = income - expenses;
                savingsRate = income > 0 ? ((income - expenses) / income) * 100 : 0;
                return null;
            }

            @Override
            protected void succeeded() {
                updateSummary(income, expenses, balance, savingsRate);
                updateBarChart(trend);
                updatePieChart(categoryBreakdown);
                summaryTable.setItems(FXCollections.observableArrayList(trend));
                spinner.setVisible(false);
            }

            @Override
            protected void failed() {
                log.error("Report load failed", getException());
                spinner.setVisible(false);
                AlertUtil.showError("Error", "Failed to load report data.");
            }
        };
        new Thread(task).start();
    }

    private void updateSummary(double income, double expenses,
                                double balance, double savingsRate) {
        totalIncomeLabel.setText(CurrencyUtil.format(income));
        totalExpenseLabel.setText(CurrencyUtil.format(expenses));
        netBalanceLabel.setText(CurrencyUtil.format(balance));
        netBalanceLabel.setStyle(balance >= 0 ? "-fx-text-fill: #22c55e;" : "-fx-text-fill: #ef4444;");
        savingsRateLabel.setText(String.format("%.1f%%", Math.max(savingsRate, 0)));
    }

    private void updateBarChart(List<MonthlySummary> summaries) {
        barChart.getData().clear();
        XYChart.Series<String, Number> incomeSeries  = new XYChart.Series<>();
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        expenseSeries.setName("Expenses");

        for (MonthlySummary s : summaries) {
            incomeSeries.getData().add(new XYChart.Data<>(s.getMonth(), s.getIncome()));
            expenseSeries.getData().add(new XYChart.Data<>(s.getMonth(), s.getExpenses()));
        }
        barChart.getData().addAll(incomeSeries, expenseSeries);
    }

    private void updatePieChart(List<Object[]> data) {
        pieChart.getData().clear();
        for (Object[] row : data) {
            String name  = (String) row[0];
            double total = (double) row[2];
            pieChart.getData().add(new PieChart.Data(
                    name + " (" + CurrencyUtil.format(total) + ")", total));
        }
    }

    @FXML
    private void onRefresh() {
        loadReportData();
    }

    @FXML
    private void onExportExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Report");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        chooser.setInitialFileName("financial_report.xlsx");

        File file = chooser.showSaveDialog(barChart.getScene().getWindow());
        if (file == null) return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                int months = parseTrendPeriod();
                var summaries = reportService.getTrend(months);
                var txs = transactionService.getFiltered(null, null,
                        fromDatePicker.getValue(), toDatePicker.getValue(), null);
                exportService.exportToExcel(txs, summaries, file.toPath());
                return null;
            }
            @Override
            protected void succeeded() {
                AlertUtil.showInfo("Export Complete",
                        "Report exported to:\n" + file.getAbsolutePath());
            }
            @Override
            protected void failed() {
                AlertUtil.showError("Export Failed", getException().getMessage());
            }
        };
        new Thread(task).start();
    }

    private int parseTrendPeriod() {
        String val = trendPeriodCombo.getValue();
        if (val == null) return 12;
        return switch (val) {
            case "3 Months"  ->  3;
            case "6 Months"  ->  6;
            case "24 Months" -> 24;
            default          -> 12;
        };
    }
}
