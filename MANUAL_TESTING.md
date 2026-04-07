# Manual UI Testing Checklist

Use this checklist to verify the application works correctly after a build.

---

## 🚀 Startup

- [ ] App launches without errors after `run.bat`
- [ ] Database file created at `~/.financetracker/finance.db`
- [ ] Default categories appear in dropdowns (Salary, Food, Transport…)
- [ ] Dashboard loads with zero values on fresh install
- [ ] Dark theme applied by default

---

## 📊 Dashboard

- [ ] Total Balance card shows `$0.00` on empty DB
- [ ] Monthly Income card shows correct sum for current month
- [ ] Monthly Expenses card shows correct sum for current month
- [ ] Savings Rate card displays as percentage
- [ ] Pie chart shows "No expenses this month" label when empty
- [ ] Line chart loads with zero series on empty DB
- [ ] Recent Transactions table is empty with placeholder text
- [ ] Budget Alerts section shows "All budgets on track" when no budgets set
- [ ] Refresh button reloads all widgets

---

## 💳 Transactions

### Filter Bar
- [ ] Typing in Search field filters table in real time
- [ ] Type combo (Income/Expense/All) filters table
- [ ] Category combo filters by selected category
- [ ] From/To date pickers narrow results correctly
- [ ] Clear button resets all filters

### Table
- [ ] All columns sortable by clicking header
- [ ] Income rows styled green, Expense rows styled red
- [ ] Amounts formatted with currency symbol
- [ ] Edit and Delete buttons disabled when no row selected
- [ ] Buttons enable when row is selected

### Add Transaction
- [ ] Clicking "Add Transaction" opens dialog
- [ ] All required fields validated (empty amount shows error)
- [ ] Negative or zero amount shows validation error
- [ ] Future date shows error
- [ ] Saving valid Income transaction adds it to table
- [ ] Saving valid Expense transaction adds it to table
- [ ] Dashboard balance updates after adding transaction

### Edit Transaction
- [ ] Dialog pre-fills with existing values
- [ ] Changes saved correctly
- [ ] Cancel closes dialog without changes

### Delete Transaction
- [ ] Confirmation dialog shown before delete
- [ ] Cancelling confirmation leaves record intact
- [ ] Confirming removes row from table

### Export
- [ ] Export CSV opens file chooser
- [ ] CSV file opens correctly in Excel/Notepad
- [ ] Export Excel opens file chooser
- [ ] Excel file contains Transactions and Monthly Summary sheets

---

## 📈 Reports

- [ ] Report view loads without errors
- [ ] Bar chart displays income vs expenses bars
- [ ] Pie chart shows current month category breakdown
- [ ] Monthly summary table shows rows
- [ ] Balance column shows green for positive, red for negative
- [ ] Changing trend period combo updates bar chart
- [ ] Refresh button reloads data
- [ ] Export Excel from Reports generates valid file

---

## ⚙️ Settings

### Theme
- [ ] Dark Mode toggle switches entire UI to dark theme
- [ ] Light Mode toggle switches to light theme
- [ ] Theme preference persisted after restarting app

### Currency
- [ ] Changing currency combo updates amount display across all views

### Budgets
- [ ] Selecting category and entering amount and clicking Set Budget creates budget
- [ ] Budget appears in budget table with Spent and Status columns
- [ ] Adding a transaction in the budgeted category updates Spent value
- [ ] Exceeding budget amount shows OVER status in red
- [ ] Dashboard shows budget alert when over-budget
- [ ] Deleting a budget removes it from table

### Categories
- [ ] Entering name and picking colour adds new category
- [ ] New category appears in all dropdowns
- [ ] Attempting to delete a category with transactions shows warning
- [ ] Deleting a category with no transactions removes it

---

## 🌗 Theme Consistency

- [ ] All pages look correct in Dark mode
- [ ] All pages look correct in Light mode
- [ ] Dialog windows inherit current theme
- [ ] TableView rows have correct alternating row colours in both themes

---

## 📐 Responsive Behaviour

- [ ] App is usable at 1024×680 (minimum size)
- [ ] Content scrolls at smaller window sizes
- [ ] Charts resize proportionally when window is resized

---

## 🔒 Input Validation

- [ ] Amount field rejects letters
- [ ] Amount field rejects zero
- [ ] Amount field rejects negative values
- [ ] Category name rejects blank input
- [ ] Budget amount rejects non-numeric and zero values
- [ ] Transaction date blocks future dates

---

*Last updated: April 2024*
