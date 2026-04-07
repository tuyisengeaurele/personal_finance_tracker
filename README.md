# 💰 Personal Finance Tracker

A modern, cross-platform desktop application for managing personal finances — built with **Java 17**, **JavaFX 21**, and **SQLite**.

![Java](https://img.shields.io/badge/Java-17-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue)
![SQLite](https://img.shields.io/badge/SQLite-3.45-green)
![Maven](https://img.shields.io/badge/Maven-3.8+-red)
![License](https://img.shields.io/badge/License-MIT-yellow)

---

## ✨ Features

### Core
- **Add / Edit / Delete** income and expense transactions
- **Category management** with custom colours
- **Real-time balance** tracking (all-time & monthly)
- **Search & filter** by date range, category, type, or keyword

### Analytics & Reports
- **Dashboard cards** — Total Balance, Monthly Income, Monthly Expenses, Savings Rate
- **Pie chart** — spending by category (current month)
- **Line chart** — income vs. expenses trend (last N months)
- **Bar chart** — monthly income vs. expenses comparison
- **Monthly summary table** with net balance per month

### Budget Tracking
- Set monthly budgets per category
- Visual over-budget alerts on the dashboard
- Budget progress indicators in Settings

### Export
- **Export to CSV** — filtered transactions with UTF-8 BOM (Excel-friendly)
- **Export to Excel (.xlsx)** — transactions + monthly summary, styled with Apache POI

### UI/UX
- **Dark + Light theme** — toggleable, persisted across sessions
- **Sidebar navigation** — Dashboard, Transactions, Reports, Settings
- Responsive layouts with smooth data loading (background threads)
- All currency display configurable (USD, EUR, GBP, JPY, RWF, …)

---

## 🛠 Tech Stack

| Layer        | Technology               |
|--------------|--------------------------|
| Language     | Java 17                  |
| UI Framework | JavaFX 21                |
| Database     | SQLite via JDBC          |
| Build        | Apache Maven 3.8+        |
| Logging      | SLF4J + Logback          |
| Excel Export | Apache POI 5.2           |
| Testing      | JUnit 5 + Mockito        |
| Packaging    | Maven Shade Plugin (fat JAR) |

### Architecture
- **MVC** — FXML views, controller classes, service + DAO layers
- **DAO pattern** — `CategoryDAO`, `TransactionDAO`, `BudgetDAO`
- **Service layer** — business logic independent of persistence
- **PreparedStatements** everywhere — SQL injection prevention
- Background threads (`Task<>`) — UI never blocks on DB operations

---

## 🚀 Quick Start

### Prerequisites
- **Java 17+** (e.g., [Eclipse Temurin](https://adoptium.net))
- **Apache Maven 3.8+** (for first build)

### Build & Run (Windows)

```bash
# Build the fat JAR
build.bat

# Launch the application
run.bat
```

### Manual Maven Commands

```bash
# Build
mvn clean package

# Run during development
mvn javafx:run

# Run tests
mvn test

# Launch packaged JAR
java -jar target/personal-finance-tracker-1.0.0.jar
```

---

## 📁 Project Structure

```
personal-finance-tracker/
├── src/
│   ├── main/
│   │   ├── java/com/financetracker/
│   │   │   ├── app/          # Entry points (MainApp, Launcher)
│   │   │   ├── model/        # Domain models (Transaction, Category, Budget)
│   │   │   ├── dao/          # Database access objects + initializer
│   │   │   ├── service/      # Business logic layer
│   │   │   ├── controller/   # JavaFX FXML controllers
│   │   │   └── util/         # Utilities (theme, currency, validation)
│   │   └── resources/
│   │       ├── fxml/         # JavaFX layout files
│   │       ├── css/          # Dark + light theme stylesheets
│   │       └── logback.xml   # Logging configuration
│   └── test/
│       └── java/com/financetracker/
│           ├── dao/          # TransactionDAOTest
│           └── service/      # TransactionServiceTest
├── build.bat                 # Windows build script
├── run.bat                   # Windows launch script
├── pom.xml                   # Maven project descriptor
└── README.md
```

---

## 🗃 Database

The SQLite database is created automatically on first launch at:
```
~/.financetracker/finance.db
```

### Schema

```sql
CREATE TABLE categories (
    id    INTEGER PRIMARY KEY AUTOINCREMENT,
    name  TEXT    NOT NULL UNIQUE,
    color TEXT    NOT NULL,
    icon  TEXT    NOT NULL
);

CREATE TABLE transactions (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    type        TEXT    NOT NULL CHECK(type IN ('INCOME','EXPENSE')),
    amount      REAL    NOT NULL CHECK(amount > 0),
    category_id INTEGER NOT NULL REFERENCES categories(id),
    description TEXT    DEFAULT '',
    date        TEXT    NOT NULL,
    created_at  TEXT    NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE budgets (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    category_id INTEGER NOT NULL REFERENCES categories(id),
    amount      REAL    NOT NULL CHECK(amount > 0),
    month       TEXT    NOT NULL,
    UNIQUE(category_id, month)
);
```

Default categories are seeded on first run: Salary, Food, Transport, Housing, Bills, and more.

---

## 🧪 Running Tests

```bash
mvn test
```

Tests use the same SQLite database infrastructure — no mocking of the database layer ensures realistic integration coverage.

---

## 📦 Distribution

The `mvn clean package` command produces a **self-contained fat JAR** at:
```
target/personal-finance-tracker-1.0.0.jar
```

This JAR includes all dependencies (JavaFX native libraries for Windows, Linux, and macOS, SQLite driver, POI, Logback) so it runs with just:
```bash
java -jar target/personal-finance-tracker-1.0.0.jar
```

No separate JavaFX installation required.

---

## 🔒 Security

- All SQL queries use **PreparedStatements** — no string concatenation
- Input validation via `ValidationUtil` before any DB write
- Passwords / secrets are never stored (single-user app)
- Database stored in user home directory — no elevated permissions needed

---

## 👤 Author

**Ange Aurele TUYISENGE**
GitHub: [@tuyisengeaurele](https://github.com/tuyisengeaurele)

---

## 📄 License

MIT — see [LICENSE](LICENSE) for details.
