 
# 💎 FinvisIQ — Personal Finance Intelligence Platform

<p align="center">

A cloud-enabled personal finance intelligence platform for tracking, analyzing, and improving personal financial health.

</p>

<p align="center">

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-blue.svg)](https://openjfx.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-blue.svg)](https://www.mysql.com/)
[![Aiven](https://img.shields.io/badge/Aiven-Cloud%20MySQL-purple.svg)](https://aiven.io/)
[![Railway](https://img.shields.io/badge/Railway-Backend-purple.svg)](https://railway.app/)
[![Netlify](https://img.shields.io/badge/Netlify-Frontend-00C7B7.svg)](https://www.netlify.com/)
[![Maven](https://img.shields.io/badge/Maven-3.9%2B-red.svg)](https://maven.apache.org/)

</p>

<p align="center">

## 🌐 Live Web Application

### [🚀 Launch FinvisIQ — finvis.netlify.app](https://finvis.netlify.app)

</p>

---

## 📌 Overview

**FinvisIQ** is a full-stack Personal Finance Intelligence Platform designed to help users manage their income, expenses, budgets, savings, investments, financial goals, and overall financial health from a unified system.

The platform combines a modern web application, a native JavaFX desktop client, a Spring Boot REST API, and a cloud-hosted MySQL database.

### 🌐 Production Application

**Live Website:**  
https://finvis.netlify.app

**Frontend:** Netlify  
**Backend:** Railway  
**Database:** Aiven Cloud MySQL

### Core Architecture

```text
                         ┌─────────────────────────┐
                         │     Aiven Cloud MySQL    │
                         │       Production DB      │
                         └────────────┬────────────┘
                                      │
                                JDBC + TLS
                                      │
                         ┌────────────▼────────────┐
                         │     Railway Backend      │
                         │   Spring Boot REST API   │
                         │                          │
                         │  Authentication          │
                         │  Business Logic          │
                         │  Financial Services      │
                         │  Database Access         │
                         └────────────┬────────────┘
                                      │
                         HTTPS / REST API
                       ┌──────────────┴──────────────┐
                       │                             │
              ┌────────▼────────┐          ┌────────▼────────┐
              │ Netlify Web App │          │ JavaFX Desktop  │
              │                 │          │     Client      │
              │ HTML/CSS/JS     │          │ Java 21 + JavaFX│
              └─────────────────┘          └─────────────────┘

Both the web application and desktop application communicate with the same Spring Boot backend.

The client applications never connect directly to the production database.


---

✨ Key Features

🎨 Modern Financial Dashboard

Premium Liquid Glass inspired interface

Responsive web interface

Light and dark themes

Smooth transitions and micro-interactions

Financial overview dashboard

Income and expense summaries

Net savings calculation

Financial health indicators



---

💰 Income & Expense Management

Record income transactions

Record expense transactions

Categorize transactions

View transaction history

Track spending patterns

Calculate income versus expenses

Monitor monthly financial activity



---

📊 Financial Analytics

Income versus expense analysis

Expense category distribution

Savings analysis

Spending pattern visualization

Financial health metrics

Dashboard-level financial summaries



---

🤖 Financial Intelligence

FinvisIQ provides automated financial insights based on user data.

Features include:

Unusual expense detection

Spending alerts

Budget warnings

Emergency fund readiness analysis

Automated financial recommendations

Financial health calculations



---

📈 SIP & Wealth Planner

The wealth planning module provides tools for estimating long-term investment growth.

Features include:

SIP calculation

Investment amount estimation

Expected return calculation

Compound interest projection

Wealth goal planning

Investment timeline visualization



---

📑 Financial Reports

FinvisIQ supports financial report generation.

PDF Reports

The application can generate financial reports containing:

Financial summaries

Transaction information

Metrics

Tables

Charts

Financial analysis


Word Reports

The application also supports generation of genuine .docx documents compatible with:

Microsoft Word

LibreOffice

Google Docs



---

🔐 Authentication & Security

FinvisIQ uses a centralized backend security architecture.

Security features include:

User authentication

Password hashing

JWT-based API authentication

Role-based authorization

Admin and common-user roles

Session/token management

Backend-side validation

Protected REST endpoints


Security Architecture

Web Client ────────┐
                   │
Desktop Client ────┤
                   │ HTTPS
                   ▼
          Spring Boot REST API
                   │
                   │ JDBC + TLS
                   ▼
            Aiven MySQL

Database credentials are stored only on the backend server.

They are never distributed to users through the desktop application or frontend application.


---

🏗️ Technology Stack

Layer	Technology	Purpose

Backend	Spring Boot 3.2.5	REST API and business logic
Programming Language	Java 21	Application development
Web Frontend	HTML5 / CSS3 / JavaScript	Browser-based application
Desktop UI	JavaFX 21.0.2	Native desktop client
API Client	Java HttpClient	Desktop-to-backend communication
Database	MySQL 8.x	Relational data storage
Cloud Database	Aiven Cloud MySQL	Production database hosting
Database Driver	MySQL Connector/J	Java database connectivity
Authentication	JWT	Stateless API authentication
Connection Pool	HikariCP	Database connection management
Build System	Apache Maven	Build and dependency management
Backend Hosting	Railway	Spring Boot deployment
Frontend Hosting	Netlify	Web application deployment



---

📁 Project Structure

FinvisIQ/
│
├── pom.xml
├── README.md
├── DEPLOYMENT.md
├── API.md
├── .env.example
├── .gitignore
├── run.bat
├── run-local.bat
├── SETUP_WINDOWS.bat
├── FINVISIQ_LOCAL_REQUIREMENTS.txt
│
├── frontend/
│   ├── index.html
│   ├── assets/
│   ├── css/
│   └── js/
│
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── smartfinance/
        │           ├── App.java
        │           ├── Launcher.java
        │           │
        │           ├── backend/
        │           │   └── FinvisiqBackendApplication.java
        │           │
        │           ├── controller/
        │           │   ├── AuthController.java
        │           │   ├── TransactionController.java
        │           │   ├── BudgetController.java
        │           │   ├── GoalController.java
        │           │   └── ...
        │           │
        │           ├── dao/
        │           │   ├── UserDAO.java
        │           │   ├── TransactionDAO.java
        │           │   ├── BudgetDAO.java
        │           │   └── ...
        │           │
        │           ├── model/
        │           │   ├── User.java
        │           │   ├── Transaction.java
        │           │   ├── Budget.java
        │           │   ├── Goal.java
        │           │   └── ...
        │           │
        │           ├── service/
        │           │   ├── AuthenticationService.java
        │           │   ├── FinanceService.java
        │           │   ├── ReportService.java
        │           │   └── ...
        │           │
        │           └── util/
        │               ├── PasswordUtil.java
        │               ├── ThemeManager.java
        │               └── ...
        │
        └── resources/
            ├── css/
            ├── images/
            ├── application.properties
            └── ...

> The exact package/file structure may evolve as the application is developed.




---

🚀 Getting Started

1. Requirements

For the desktop application

You need:

Java JDK 21+

Maven 3.9+ (optional if using the provided launcher)

Windows for run.bat


The desktop application does not require:

MySQL Server

XAMPP

phpMyAdmin

Aiven credentials

Local database configuration


The desktop client communicates with the deployed backend through HTTPS.


---

🖥️ Running the Desktop Application

The easiest method is:

run.bat

The desktop application uses the configured backend API endpoint.

JavaFX Desktop
      │
      │ HTTPS REST API
      ▼
Railway Spring Boot Backend
      │
      │ JDBC + TLS
      ▼
Aiven MySQL

No database password should be required on the user's computer.


---

💻 Local Development

For backend development, configure the required environment variables.

Example:

DB_URL=jdbc:mysql://<AIVEN_HOST>:<PORT>/smart_finance_db?sslMode=REQUIRED
DB_USERNAME=avnadmin
DB_PASSWORD=<YOUR_DATABASE_PASSWORD>

FRONTEND_URL=http://localhost:3000

JWT_SECRET=<YOUR_SECRET>

Do not commit actual credentials to GitHub.


---

Start the Backend

Using Maven:

mvnw.cmd spring-boot:run

Or package the application:

mvnw.cmd clean package

Then run the generated Spring Boot application.

Example:

java -jar target\smart-finance-1.0.0.jar

The backend uses:

PORT=8080

by default when running locally.


---

🌐 Running the Web Frontend

Navigate to the frontend directory:

cd frontend

Install dependencies if required:

npm install

Start the development server:

npm run dev

The production web application is available at:

https://finvis.netlify.app

The production frontend communicates with the deployed Railway backend through HTTPS.


---

☁️ Production Deployment

FinvisIQ uses the following deployment architecture:

┌───────────────────────┐
│       Netlify         │
│   Web Frontend        │
│                       │
│ finvis.netlify.app    │
└───────────┬───────────┘
            │
            │ HTTPS
            ▼
┌───────────────────────┐
│       Railway         │
│  Spring Boot Backend  │
└───────────┬───────────┘
            │
            │ JDBC + TLS
            ▼
┌───────────────────────┐
│   Aiven Cloud MySQL   │
│    Production DB      │
└───────────────────────┘


---

🚄 Railway Backend

The Spring Boot backend is deployed on Railway.

Railway provides:

Backend hosting

HTTPS endpoint

Environment variables

Automatic deployments from GitHub

Runtime configuration

Application logs


The backend should listen on the Railway-provided PORT.

Example:

server.port=${PORT:8080}
server.address=0.0.0.0


---

🌐 Netlify Frontend

The production web frontend is deployed at:

https://finvis.netlify.app

The frontend uses an environment variable to determine the backend API URL.

Example:

VITE_API_URL=https://<your-railway-domain>

The actual production value must point to the deployed Railway backend.


---

☁️ Aiven MySQL

The production database is hosted on Aiven Cloud.

The backend connects using JDBC with TLS:

jdbc:mysql://<AIVEN_HOST>:<PORT>/smart_finance_db?sslMode=REQUIRED

Database credentials must remain inside the Railway environment.


---

🔑 Environment Variables

Example configuration:

DB_URL=jdbc:mysql://<AIVEN_HOST>:<PORT>/smart_finance_db?sslMode=REQUIRED
DB_USERNAME=avnadmin
DB_PASSWORD=<SECRET>

FRONTEND_URL=https://finvis.netlify.app

JWT_SECRET=<SECRET>

PORT=8080

Important

Never commit:

DB_PASSWORD
JWT_SECRET
Aiven credentials
Production API secrets
Private keys

to GitHub.

Use Railway environment variables for backend secrets.

Use Netlify environment variables for frontend configuration.


---

🔒 Production Security Model

FinvisIQ follows a client-server security model.

❌ Incorrect architecture

Desktop ───────────────► Aiven MySQL
                         ▲
                         │
                    DB Password

This would expose production database credentials to end users.

✅ FinvisIQ architecture

Desktop ──HTTPS──► Railway API ──JDBC/TLS──► Aiven MySQL

Web ──────HTTPS──► Railway API ──JDBC/TLS──► Aiven MySQL

Only the Railway backend has access to the production database credentials.


---

🔄 Unified Data Architecture

The web and desktop applications use the same backend and database.

For example:

Desktop Client
      │
      │ Create Transaction
      ▼
Railway API
      │
      ▼
Aiven MySQL
      ▲
      │
      │ Retrieve Transaction
      │
Web Application

Therefore, data created through one client can be retrieved by another client through the centralized backend.


---

❤️ Financial Health

FinvisIQ provides a financial health evaluation based on available financial information.

The dashboard can incorporate factors such as:

Income

Expenses

Savings

Budget adherence

Emergency fund readiness

Investment planning

Spending patterns


The health score is presented as an application-generated financial metric.


---

🛡️ API Health Check

The backend provides a health endpoint for deployment verification.

Example:

GET /api/health

Expected response:

{
  "status": "UP"
}

A database health endpoint may also be provided:

GET /api/health/db

depending on the backend implementation.


---

🧪 Testing Checklist

Before production deployment, verify:

Backend

[ ] Spring Boot starts successfully

[ ] Railway deployment succeeds

[ ] /api/health responds successfully

[ ] Database connection succeeds

[ ] Aiven MySQL is reachable

[ ] Authentication works

[ ] JWT authentication works

[ ] CRUD operations work

[ ] CORS is configured correctly


Web Application

[ ] Netlify deployment succeeds

[ ] Live website opens at https://finvis.netlify.app

[ ] Login works

[ ] Dashboard loads

[ ] Transactions work

[ ] Budgets work

[ ] Goals work

[ ] Analytics load

[ ] SIP calculator works

[ ] Reports work

[ ] Light theme works

[ ] Dark theme works

[ ] Responsive layout works


Desktop Application

[ ] run.bat starts successfully

[ ] No local MySQL installation is required

[ ] No database password is required

[ ] Desktop connects to Railway

[ ] Login works

[ ] Financial data loads

[ ] CRUD operations work

[ ] Reports work


Cross-Client Verification

[ ] Create data from desktop

[ ] Verify it appears on web

[ ] Create data from web

[ ] Verify it appears on desktop

[ ] Confirm both clients use the same production data



---

📚 Documentation

Additional documentation:

Deployment Guide

REST API Documentation

Environment Configuration

Local Development Requirements

Windows Automatic Setup



---

🔧 Development Workflow

Recommended development workflow:

1. Develop locally
       ↓
2. Test Spring Boot backend
       ↓
3. Test Aiven database connection
       ↓
4. Test web frontend
       ↓
5. Test JavaFX desktop client
       ↓
6. Commit changes
       ↓
7. Push to GitHub
       ↓
8. Railway deploys backend
       ↓
9. Netlify deploys frontend
       ↓
10. Test production system


---

🌟 Project Goals

FinvisIQ aims to provide a unified platform for:

Personal financial management

Expense tracking

Budget planning

Savings analysis

Investment planning

Financial intelligence

Automated financial insights

Financial reporting

Cross-platform access



---

📌 Project Status

Project: FinvisIQ — Personal Finance Intelligence Platform

Architecture: Full-Stack Cloud Application

Backend: Spring Boot REST API

Web Client: HTML / CSS / JavaScript

Desktop Client: JavaFX

Database: Aiven Cloud MySQL

Backend Hosting: Railway

Frontend Hosting: Netlify

Live Website: https://finvis.netlify.app

Language: Java 21

Status: Active Development


---

👨‍💻 Author

Akilesh A

Integrated M.Sc. Software Systems
Coimbatore Institute of Technology


---

<p align="center">💎 FinvisIQ — Intelligent Finance. Smarter Decisions.

</p>