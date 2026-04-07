package com.financetracker.service;

import com.financetracker.model.MonthlySummary;
import com.financetracker.model.Transaction;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exports transaction data to CSV or Excel (.xlsx) format.
 */
public class ExportService {

    private static final Logger log = LoggerFactory.getLogger(ExportService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final TransactionService transactionService;

    public ExportService() {
        this.transactionService = new TransactionService();
    }

    // -------------------------------------------------------------------------
    // CSV export
    // -------------------------------------------------------------------------

    /**
     * Exports all transactions visible under the current filter to a CSV file.
     */
    public void exportToCSV(List<Transaction> transactions, Path outputPath) throws IOException {
        log.info("Exporting {} transactions to CSV: {}", transactions.size(), outputPath);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputPath.toFile()),
                        StandardCharsets.UTF_8))) {

            // BOM for Excel compatibility
            writer.write('\uFEFF');
            writer.write("ID,Date,Type,Category,Amount,Description");
            writer.newLine();

            for (Transaction t : transactions) {
                writer.write(String.join(",",
                        String.valueOf(t.getId()),
                        t.getDate().format(DATE_FMT),
                        t.getType().getDisplayName(),
                        escapeCsv(t.getCategoryName()),
                        String.format("%.2f", t.getAmount()),
                        escapeCsv(t.getDescription() != null ? t.getDescription() : "")
                ));
                writer.newLine();
            }
        }
        log.info("CSV export complete.");
    }

    // -------------------------------------------------------------------------
    // Excel export
    // -------------------------------------------------------------------------

    /**
     * Exports transactions and a monthly summary sheet to an .xlsx workbook.
     */
    public void exportToExcel(List<Transaction> transactions,
                               List<MonthlySummary> summaries,
                               Path outputPath) throws IOException, SQLException {
        log.info("Exporting {} transactions to Excel: {}", transactions.size(), outputPath);

        try (Workbook wb = new XSSFWorkbook()) {
            buildTransactionsSheet(wb, transactions);
            buildSummarySheet(wb, summaries);

            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                wb.write(fos);
            }
        }
        log.info("Excel export complete.");
    }

    private void buildTransactionsSheet(Workbook wb, List<Transaction> transactions) {
        Sheet sheet = wb.createSheet("Transactions");

        // Header style
        CellStyle headerStyle = createHeaderStyle(wb);

        // Column widths (characters * 256)
        int[] colWidths = {8, 14, 12, 20, 14, 40};
        for (int i = 0; i < colWidths.length; i++) {
            sheet.setColumnWidth(i, colWidths[i] * 256);
        }

        // Header row
        String[] headers = {"ID", "Date", "Type", "Category", "Amount", "Description"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        CellStyle incomeStyle  = createColoredStyle(wb, IndexedColors.LIGHT_GREEN.getIndex());
        CellStyle expenseStyle = createColoredStyle(wb, IndexedColors.ROSE.getIndex());
        CellStyle amountStyle  = createAmountStyle(wb);

        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            Row row = sheet.createRow(i + 1);

            CellStyle rowStyle = "INCOME".equals(t.getType().name()) ? incomeStyle : expenseStyle;

            createStyledCell(row, 0, t.getId(), rowStyle);
            createStyledCell(row, 1, t.getDate().format(DATE_FMT), rowStyle);
            createStyledCell(row, 2, t.getType().getDisplayName(), rowStyle);
            createStyledCell(row, 3, t.getCategoryName(), rowStyle);

            Cell amtCell = row.createCell(4);
            amtCell.setCellValue(t.getAmount());
            amtCell.setCellStyle(amountStyle);

            createStyledCell(row, 5,
                    t.getDescription() != null ? t.getDescription() : "", rowStyle);
        }

        // AutoFilter
        if (!transactions.isEmpty()) {
            sheet.setAutoFilter(new CellRangeAddress(0, transactions.size(), 0, 5));
        }
    }

    private void buildSummarySheet(Workbook wb, List<MonthlySummary> summaries) {
        Sheet sheet = wb.createSheet("Monthly Summary");
        CellStyle headerStyle = createHeaderStyle(wb);
        CellStyle amountStyle = createAmountStyle(wb);

        int[] colWidths = {14, 16, 16, 16};
        for (int i = 0; i < colWidths.length; i++) {
            sheet.setColumnWidth(i, colWidths[i] * 256);
        }

        String[] headers = {"Month", "Income", "Expenses", "Balance"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (int i = 0; i < summaries.size(); i++) {
            MonthlySummary s = summaries.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(s.getMonth());

            Cell incCell = row.createCell(1);
            incCell.setCellValue(s.getIncome());
            incCell.setCellStyle(amountStyle);

            Cell expCell = row.createCell(2);
            expCell.setCellValue(s.getExpenses());
            expCell.setCellStyle(amountStyle);

            Cell balCell = row.createCell(3);
            balCell.setCellValue(s.getBalance());
            balCell.setCellStyle(amountStyle);
        }
    }

    // -------------------------------------------------------------------------
    // Style helpers
    // -------------------------------------------------------------------------

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private CellStyle createColoredStyle(Workbook wb, short colorIndex) {
        CellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(colorIndex);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createAmountStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        DataFormat format = wb.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private void createStyledCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value instanceof Integer i) {
            cell.setCellValue(i);
        } else {
            cell.setCellValue(String.valueOf(value));
        }
        cell.setCellStyle(style);
    }

    // -------------------------------------------------------------------------
    // CSV helpers
    // -------------------------------------------------------------------------

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
