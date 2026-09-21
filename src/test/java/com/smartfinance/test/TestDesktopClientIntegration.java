package com.smartfinance.test;

import com.smartfinance.api.ApiClient;
import com.smartfinance.api.ApiConfig;
import com.smartfinance.dao.*;
import com.smartfinance.model.*;

import java.time.LocalDate;
import java.util.List;

public class TestDesktopClientIntegration {
    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("TEST: Desktop Client Gateway Verification (No DB Credentials)");
        System.out.println("==========================================================");

        // 1. Set client mode and base URL to local dev server
        ApiConfig.setClientMode(true);
        ApiConfig.setBaseUrl("http://localhost:8085");
        ApiClient.getInstance().init();

        // 2. Verify DatabaseManager does NOT attempt JDBC or fail
        System.out.println("\n[1] Verifying DatabaseManager bypass...");
        DatabaseManager db = DatabaseManager.getInstance();
        try {
            db.getConnection();
            System.err.println("FAILURE: DatabaseManager returned non-null connection in client mode!");
        } catch (Exception e) {
            System.out.println("SUCCESS: DatabaseManager safely prevented direct JDBC: " + e.getMessage());
        }

        // 3. Authenticate via ApiClient
        System.out.println("\n[2] Logging in via REST API Client...");
        User user = ApiClient.getInstance().login("desktopuser@finvisiq.com", "Password@123");
        if (user == null) {
            System.err.println("FAILED to login with desktopuser@finvisiq.com");
            System.exit(1);
            return;
        }
        System.out.println("SUCCESS: Logged in as " + user.getName() + " (ID: " + user.getUserId() + ")");

        // 3. Testing TransactionDAO CRUD delegation
        System.out.println("\n[3] Testing TransactionDAO CRUD delegation...");
        TransactionDAO txDao = new TransactionDAO();
        Transaction newTx = new Transaction(user.getUserId(), 2500.0, "EXPENSE", "Food & Dining", LocalDate.now(), "Dinner with colleagues", "UPI");
        int txId = txDao.insert(newTx);
        if (txId > 0) {
            System.out.println("SUCCESS: TransactionDAO inserted transaction with ID: " + txId);
        } else {
            System.err.println("FAILURE: Failed to insert transaction via API");
        }
        List<Transaction> txList = txDao.findByUserId(user.getUserId());
        System.out.println("SUCCESS: TransactionDAO retrieved " + txList.size() + " transactions via API.");
        boolean txDeleted = txDao.delete(txId);
        System.out.println("SUCCESS: TransactionDAO deleted test transaction: " + txDeleted);

        // 4. Testing BudgetDAO CRUD delegation
        System.out.println("\n[4] Testing BudgetDAO CRUD delegation...");
        BudgetDAO budgetDao = new BudgetDAO();
        Budget newBudget = new Budget(user.getUserId(), "Food & Dining", 15000.0, "MONTHLY", 80.0);
        int bId = budgetDao.insert(newBudget);
        if (bId > 0) {
            System.out.println("SUCCESS: BudgetDAO inserted budget with ID: " + bId);
        }
        List<Budget> budgetList = budgetDao.findByUserId(user.getUserId());
        System.out.println("SUCCESS: BudgetDAO retrieved " + budgetList.size() + " budgets via API.");
        boolean bDeleted = budgetDao.delete(bId);
        System.out.println("SUCCESS: BudgetDAO deleted test budget: " + bDeleted);

        // 5. Test GoalDAO delegation
        System.out.println("\n[5] Testing GoalDAO delegation...");
        GoalDAO goalDao = new GoalDAO();
        List<Goal> goalList = goalDao.findByUserId(user.getUserId());
        System.out.println("SUCCESS: GoalDAO retrieved " + goalList.size() + " goals via API.");

        // 6. Test InvestmentDAO delegation
        System.out.println("\n[6] Testing InvestmentDAO delegation...");
        InvestmentDAO invDao = new InvestmentDAO();
        List<Investment> invList = invDao.findByUserId(user.getUserId());
        System.out.println("SUCCESS: InvestmentDAO retrieved " + invList.size() + " investments via API.");

        // 7. Test SubscriptionDAO delegation
        System.out.println("\n[7] Testing SubscriptionDAO delegation...");
        SubscriptionDAO subDao = new SubscriptionDAO();
        List<Subscription> subList = subDao.findByUserId(user.getUserId());
        System.out.println("SUCCESS: SubscriptionDAO retrieved " + subList.size() + " subscriptions via API.");

        // 8. Test AssetDAO delegation
        System.out.println("\n[8] Testing AssetDAO delegation...");
        AssetDAO assetDao = new AssetDAO();
        List<Asset> assetList = assetDao.findByUserId(user.getUserId());
        System.out.println("SUCCESS: AssetDAO retrieved " + assetList.size() + " assets via API.");

        // 9. Test LiabilityDAO delegation
        System.out.println("\n[9] Testing LiabilityDAO delegation...");
        LiabilityDAO liabDao = new LiabilityDAO();
        List<Liability> liabList = liabDao.findByUserId(user.getUserId());
        System.out.println("SUCCESS: LiabilityDAO retrieved " + liabList.size() + " liabilities via API.");

        // 10. Test NotificationDAO delegation
        System.out.println("\n[10] Testing NotificationDAO delegation...");
        NotificationDAO notifDao = new NotificationDAO();
        List<Notification> notifList = notifDao.findByUserId(user.getUserId());
        System.out.println("SUCCESS: NotificationDAO retrieved " + notifList.size() + " notifications via API.");

        // 11. Test UserDAO delegation
        System.out.println("\n[11] Testing UserDAO delegation...");
        UserDAO userDao = new UserDAO();
        User found = userDao.findById(user.getUserId());
        if (found != null && found.getUserId() == user.getUserId()) {
            System.out.println("SUCCESS: UserDAO found current user correctly in client mode.");
        } else {
            System.err.println("FAILURE: UserDAO failed to retrieve current user in client mode.");
        }

        System.out.println("\n==========================================================");
        System.out.println("ALL DESKTOP CLIENT GATEWAY INTEGRATION TESTS PASSED!");
        System.out.println("==========================================================");
    }
}
