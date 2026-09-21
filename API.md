# 📡 FinvisIQ REST API Documentation

Base URL: `https://<your-railway-app>.up.railway.app` or `http://localhost:8085`

All authenticated endpoints require the header:
```http
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

---

## 1. System & Health

### `GET /api/health`
Checks backend service availability.
- **Auth:** None
- **Response `200 OK`:**
```json
{
  "success": true,
  "message": "Service is healthy",
  "data": {
    "service": "FinvisIQ Backend",
    "version": "2.0.0",
    "status": "UP",
    "timestamp": "2026-09-21T04:11:25.772700100Z"
  },
  "status": 200,
  "timestamp": 1789963885772
}
```

### `GET /api/health/db`
Validates connectivity to Aiven Cloud MySQL.
- **Auth:** None
- **Response `200 OK`:**
```json
{
  "success": true,
  "message": "Database connection successful",
  "data": {
    "database": "smart_finance_db",
    "serverVersion": "8.4.8",
    "sslActive": true,
    "status": "UP"
  },
  "status": 200,
  "timestamp": 1789963886862
}
```

---

## 2. Authentication

### `POST /api/auth/register`
Create a new user account.
- **Body:**
```json
{
  "name": "Alex Mercer",
  "email": "alex@finvisiq.com",
  "password": "Password@123"
}
```
- **Response `200 OK`:**
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "userId": 5,
    "name": "Alex Mercer",
    "email": "alex@finvisiq.com",
    "role": "USER",
    "currency": "INR"
  }
}
```

### `POST /api/auth/login`
Authenticate user and obtain JWT token.
- **Body:**
```json
{
  "username": "alex@finvisiq.com",
  "password": "Password@123"
}
```

### `GET /api/auth/me`
Retrieve currently authenticated user profile.
- **Auth:** Bearer Token

---

## 3. Financial Dashboard

### `GET /api/dashboard`
Returns comprehensive dashboard overview for the authenticated user.
- **Auth:** Bearer Token
- **Response `200 OK`:**
```json
{
  "success": true,
  "data": {
    "totalIncome": 50000.0,
    "totalExpenses": 12000.0,
    "netBalance": 38000.0,
    "savingsRate": 76.0,
    "financialHealthScore": 75,
    "netWorth": 75000.0,
    "totalBudgets": 1,
    "totalGoals": 1,
    "predictedNextMonthExpenses": 12000.0,
    "categoryExpenses": {
      "Groceries": 12000.0
    },
    "recentTransactions": [...]
  }
}
```

---

## 4. Transactions

- `GET /api/transactions`: Fetch user's transactions ledger.
- `POST /api/transactions`: Record new transaction.
```json
{
  "amount": 2500.0,
  "type": "EXPENSE",
  "category": "Food & Dining",
  "date": "2026-09-21",
  "description": "Team lunch",
  "paymentMethod": "UPI"
}
```
- `DELETE /api/transactions/{id}`: Delete a transaction.

---

## 5. Budgets

- `GET /api/budgets`: Fetch category spending limits and threshold usage.
- `POST /api/budgets`: Set a budget constraint.
```json
{
  "category": "Food & Dining",
  "budgetAmount": 10000.0,
  "period": "MONTHLY",
  "warningThreshold": 80.0
}
```
- `DELETE /api/budgets/{id}`: Remove a budget.

---

## 6. Savings Goals

- `GET /api/goals`: Retrieve all goals and progress.
- `POST /api/goals`: Create a savings target.
- `POST /api/goals/{id}/contribute`: Add funds to a goal.
```json
{
  "amount": 5000.0
}
```

---

## 7. Investments & SIP

- `GET /api/investments`: Retrieve investment portfolio.
- `POST /api/investments`: Add an asset holding.
- `POST /api/investments/sip-calculator`: Calculate compound growth.
```json
{
  "monthlyInvestment": 5000.0,
  "expectedReturnRate": 12.0,
  "timePeriodYears": 10
}
```

---

## 8. Net Worth & Liabilities

- `GET /api/networth`: Total Assets, Total Liabilities, Net Worth, and Debt-to-Asset ratio.
- `GET /api/assets`: List user assets.
- `POST /api/assets`: Record an asset.
- `GET /api/liabilities`: List user loans and liabilities.
- `POST /api/liabilities`: Record a loan/liability.

---

## 9. Subscriptions

- `GET /api/subscriptions`: Active recurring subscriptions.
- `POST /api/subscriptions`: Record a subscription.
- `DELETE /api/subscriptions/{id}`: Remove/cancel subscription.

---

## 10. AI Financial Intelligence

### `GET /api/ai/insights`
Analyzes spending anomalies, budget thresholds, savings rate, emergency fund status, and generates smart recommendations.
- **Auth:** Bearer Token
- **Response `200 OK`:**
```json
{
  "success": true,
  "data": {
    "insights": [
      {
        "priority": "SUCCESS",
        "category": "Savings",
        "title": "Excellent Savings Rate",
        "message": "You are saving 76.0% of your total income...",
        "timestamp": "2026-09-21T09:44:14.1239409"
      }
    ],
    "suggestions": [
      "Diversify your portfolio! Having only one type of investment increases risk.",
      "Maintain at least 6 months of expenses in an emergency fund."
    ]
  }
}
```
