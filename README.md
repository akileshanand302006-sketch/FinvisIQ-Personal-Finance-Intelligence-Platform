# 💎 FinvisIQ — Personal-Finance-Intelligence-Platform

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-blue.svg)](https://openjfx.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-blue.svg)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Maven-3.9%2B-red.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**Finora** is a state-of-the-art, premium desktop application built with JavaFX 21 and MySQL. It empowers individuals and financial professionals with real-time expense tracking, intelligent SIP wealth forecasting, automated AI insights, debt payoff planning, and professional PDF/Word report generation.

---

## ✨ Key Features

- **🎨 Modern Glassmorphic UI & Dual Theme System**
  - Ultra-sleek dark mode and high-contrast light mode with vibrant gradient lighting.
  - Interactive micro-animations, floating background particle graphics, and smooth tab transitions.

- **📊 Comprehensive Financial Analytics & Executive Dashboard**
  - Real-time Income vs. Expense visualization and net savings calculator.
  - Category-wise expense distribution charts and overall Financial Health Score metrics.

- **🤖 AI Intelligence Engine & Automated Insights**
  - Anomaly detection for unusual high-value expenses.
  - Automated spending advice, emergency fund readiness calculations, and budget alert badges.

- **📈 SIP & Wealth Planner**
  - Interactive Systematic Investment Plan (SIP) forecasting calculator.
  - Visual compound interest projection graphs, expected return breakdowns, and wealth goal timelines.

- **📑 Executive Report Generation (PDF & Word)**
  - **Native PDF 1.4 Generator:** Built-in pure Java PDF binary graphics engine creating vector visual bar charts, metrics boxes, and pixel-perfect transaction tables.
  - **Native OpenXML Word (.docx) Generator:** Produces 100% genuine Word documents compatible with Microsoft Word, LibreOffice, and Google Docs with custom cell shading, progress callouts, and detailed ledgers.

- **🔒 Role-Based Security & Admin Panel**
  - BCrypt password hashing and session management.
  - Role-based authorization (Admin / Common User) with admin user management and system audit logging.

- **🗄️ Full MySQL Relational Storage**
  - Automatic database initialization, table creation, and schema migration.

---

## 🛠️ Technology Stack

| Component | Technology / Library |
| :--- | :--- |
| **Language** | Java 21 (JDK 21+) |
| **UI Framework** | JavaFX 21.0.2 (Controls & FXML) |
| **Database** | MySQL Server 8.0+ |
| **JDBC Driver** | MySQL Connector/J `8.3.0` |
| **Build System** | Apache Maven `3.9+` |
| **Logging** | SLF4J Simple `1.7.36` |

---

## 📁 Project Structure

```text
Finora-Personal Finance Intelligence Platform/
├── pom.xml                               # Maven Project Dependencies & Plugin Config
├── run.bat                               # Automated Build & Launch Script
├── README.md                             # Documentation
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── smartfinance/
        │           ├── App.java          # JavaFX Application Entry Point
        │           ├── Launcher.java     # Classpath Launcher Shim
        │           ├── controller/       # UI Controllers (Login, Main, Budget, Analytics, SIP, Reports, Admin, etc.)
        │           ├── dao/              # Data Access Objects (UserDAO, TransactionDAO, BudgetDAO, etc.)
        │           ├── model/            # Data Models (User, Transaction, Budget, Subscription, Goal)
        │           ├── service/          # Business Logic & Report Generators (PdfReportGenerator, WordReportGenerator)
        │           └── util/             # Security, Animation & Theme Utilities (PasswordUtil, ThemeManager, etc.)
        └── resources/
            ├── css/                      # Theme Styling (dark-theme.css, light-theme.css)
            ├── db.properties             # Database Connection Configuration
            └── images/                   # UI Assets & Backgrounds
🚀 Getting Started
Prerequisites
Ensure you have the following installed on your machine:

Java Development Kit (JDK 21 or higher)
Apache Maven 3.9+
MySQL Server 8.0+ running locally
Database Configuration
Start your local MySQL Server.
Verify or update your database credentials in src/main/resources/db.properties or DatabaseManager.java:
properties
db.url=jdbc:mysql://localhost:3306/finora_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
db.username=root
db.password=1234
The application automatically creates the finora_db database and all required tables upon first launch!
🏃 Running the Application
Option 1: Using the Automated Script (Recommended)
Simply double-click or execute the included Windows script:

powershell
.\run.bat
Option 2: Using Maven Command Line
powershell
mvn compile javafx:run
🧪 Admin Credentials
To log in as an administrator to inspect user management and system audit logs:

Username / Email: admin or admin@finora.com
Password: 1234
📄 Exported Reports
Exported financial reports are generated in the application's root directory:

PDF Report: Finora_Financial_Report.pdf
Word Document: Finora_Financial_Report.docx
🤝 Contributing
Contributions, issues, and feature requests are welcome! Feel free to check the 

issues page
.

📜 License
Distributed under the MIT License. See LICENSE for more information.
