package com.smartfinance.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartfinance.model.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-Performance REST API Client for FinvisIQ Desktop.
 * Communicates securely with the Railway Spring Boot Backend over HTTPS.
 * Stores JWT authentication tokens in-memory and deserializes responses into typed domain models.
 * Features fast in-memory caching to eliminate redundant network roundtrips on the JavaFX UI thread.
 */
public class ApiClient {

    private static final ApiClient INSTANCE = new ApiClient();

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String authToken = null;
    private User authenticatedUser = null;

    // Cache structure with TTL
    private static class CacheEntry<T> {
        final T data;
        final long expiresAt;

        CacheEntry(T data, long ttlMillis) {
            this.data = data;
            this.expiresAt = System.currentTimeMillis() + ttlMillis;
        }

        boolean isValid() {
            return System.currentTimeMillis() < expiresAt;
        }
    }

    private final Map<Integer, CacheEntry<ArrayList<Transaction>>> transactionsCache = new ConcurrentHashMap<>();
    private final Map<Integer, CacheEntry<ArrayList<Budget>>> budgetsCache = new ConcurrentHashMap<>();
    private final Map<Integer, CacheEntry<ArrayList<Goal>>> goalsCache = new ConcurrentHashMap<>();
    private final Map<Integer, CacheEntry<ArrayList<Investment>>> investmentsCache = new ConcurrentHashMap<>();
    private final Map<Integer, CacheEntry<ArrayList<Subscription>>> subscriptionsCache = new ConcurrentHashMap<>();
    private final Map<Integer, CacheEntry<ArrayList<Notification>>> notificationsCache = new ConcurrentHashMap<>();
    private static final long DEFAULT_TTL_MS = 10_000L; // 10 seconds TTL

    private ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(8))
                .build();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ApiClient getInstance() {
        return INSTANCE;
    }

    public void init() {
        // Warm up and test connectivity
        System.out.println("[FinvisIQ Desktop] Initialized REST API client pointing to: " + ApiConfig.getBaseUrl());
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String token) {
        this.authToken = token;
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    public void invalidateAllCaches() {
        transactionsCache.clear();
        budgetsCache.clear();
        goalsCache.clear();
        investmentsCache.clear();
        subscriptionsCache.clear();
        notificationsCache.clear();
    }

    public void invalidateTransactionsCache() {
        transactionsCache.clear();
    }

    public void logout() {
        this.authToken = null;
        this.authenticatedUser = null;
        invalidateAllCaches();
    }

    public boolean isLoggedIn() {
        return authToken != null && !authToken.isBlank();
    }

    // =========================================================================
    // AUTHENTICATION
    // =========================================================================

    public User login(String username, String password) {
        try {
            Map<String, String> payload = Map.of(
                    "username", username,
                    "password", password
            );

            HttpResponse<String> response = sendRequest("POST", "/api/auth/login", payload, false);
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                if (root.path("success").asBoolean(false)) {
                    JsonNode data = root.path("data");
                    this.authToken = data.path("token").asText();

                    User user = new User();
                    user.setUserId(data.path("userId").asInt());
                    user.setName(data.path("name").asText());
                    user.setEmail(data.path("email").asText());
                    user.setRole(data.path("role").asText("USER"));
                    user.setCurrency(data.path("currency").asText("INR"));
                    this.authenticatedUser = user;
                    return user;
                }
            }
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Login request failed: " + e.getMessage());
        }
        return null;
    }

    public User register(String name, String email, String password, String role, int age) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("name", name);
            payload.put("email", email);
            payload.put("password", password);
            payload.put("role", role);
            payload.put("age", age);

            HttpResponse<String> response = sendRequest("POST", "/api/auth/register", payload, false);
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                JsonNode root = objectMapper.readTree(response.body());
                if (root.path("success").asBoolean(false)) {
                    JsonNode data = root.path("data");
                    this.authToken = data.path("token").asText();

                    User user = new User();
                    user.setUserId(data.path("userId").asInt());
                    user.setName(data.path("name").asText());
                    user.setEmail(data.path("email").asText());
                    user.setRole(data.path("role").asText(role));
                    user.setAge(age);
                    this.authenticatedUser = user;
                    return user;
                }
            }
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Registration request failed: " + e.getMessage());
        }
        return null;
    }

    // =========================================================================
    // TRANSACTIONS
    // =========================================================================

    public ArrayList<Transaction> getTransactions(int userId) {
        CacheEntry<ArrayList<Transaction>> cached = transactionsCache.get(userId);
        if (cached != null && cached.isValid()) {
            return new ArrayList<>(cached.data);
        }
        try {
            HttpResponse<String> response = sendRequest("GET", "/api/transactions", null, true);
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode data = root.path("data");
                if (data.isArray()) {
                    List<Transaction> list = objectMapper.readerFor(new TypeReference<List<Transaction>>() {}).readValue(data);
                    ArrayList<Transaction> result = new ArrayList<>(list);
                    transactionsCache.put(userId, new CacheEntry<>(result, DEFAULT_TTL_MS));
                    return new ArrayList<>(result);
                }
            }
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Get transactions failed: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public int createTransaction(Transaction txn) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("amount", txn.getAmount());
            payload.put("type", txn.getType());
            payload.put("category", txn.getCategory());
            payload.put("date", txn.getDate() != null ? txn.getDate().toString() : null);
            payload.put("description", txn.getDescription());
            payload.put("paymentMethod", txn.getPaymentMethod());

            HttpResponse<String> response = sendRequest("POST", "/api/transactions", payload, true);
            transactionsCache.clear();
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                JsonNode root = objectMapper.readTree(response.body());
                return root.path("data").path("transactionId").asInt(1);
            }
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Create transaction failed: " + e.getMessage());
        }
        return 0;
    }

    public boolean updateTransaction(Transaction txn) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("amount", txn.getAmount());
            payload.put("type", txn.getType());
            payload.put("category", txn.getCategory());
            payload.put("date", txn.getDate() != null ? txn.getDate().toString() : null);
            payload.put("description", txn.getDescription());
            payload.put("paymentMethod", txn.getPaymentMethod());

            HttpResponse<String> response = sendRequest("PUT", "/api/transactions/" + txn.getTransactionId(), payload, true);
            transactionsCache.clear();
            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Update transaction failed: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteTransaction(int id) {
        try {
            HttpResponse<String> response = sendRequest("DELETE", "/api/transactions/" + id, null, true);
            transactionsCache.clear();
            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Delete transaction failed: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // BUDGETS
    // =========================================================================

    public ArrayList<Budget> getBudgets(int userId) {
        CacheEntry<ArrayList<Budget>> cached = budgetsCache.get(userId);
        if (cached != null && cached.isValid()) {
            return new ArrayList<>(cached.data);
        }
        try {
            HttpResponse<String> response = sendRequest("GET", "/api/budgets", null, true);
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode data = root.path("data");
                if (data.isArray()) {
                    List<Budget> list = objectMapper.readerFor(new TypeReference<List<Budget>>() {}).readValue(data);
                    ArrayList<Budget> result = new ArrayList<>(list);
                    budgetsCache.put(userId, new CacheEntry<>(result, DEFAULT_TTL_MS));
                    return new ArrayList<>(result);
                }
            }
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Get budgets failed: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public int createBudget(Budget budget) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("category", budget.getCategory());
            payload.put("budgetAmount", budget.getBudgetAmount());
            payload.put("period", budget.getPeriod());
            payload.put("warningThreshold", budget.getWarningThreshold());

            HttpResponse<String> response = sendRequest("POST", "/api/budgets", payload, true);
            budgetsCache.clear();
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                JsonNode root = objectMapper.readTree(response.body());
                return root.path("data").path("budgetId").asInt(1);
            }
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Create budget failed: " + e.getMessage());
        }
        return 0;
    }

    public boolean updateBudget(Budget budget) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("category", budget.getCategory());
            payload.put("budgetAmount", budget.getBudgetAmount());
            payload.put("period", budget.getPeriod());
            payload.put("warningThreshold", budget.getWarningThreshold());

            HttpResponse<String> response = sendRequest("PUT", "/api/budgets/" + budget.getBudgetId(), payload, true);
            budgetsCache.clear();
            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Update budget failed: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteBudget(int id) {
        try {
            HttpResponse<String> response = sendRequest("DELETE", "/api/budgets/" + id, null, true);
            budgetsCache.clear();
            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Delete budget failed: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // SAVINGS GOALS
    // =========================================================================

    public ArrayList<Goal> getGoals(int userId) {
        CacheEntry<ArrayList<Goal>> cached = goalsCache.get(userId);
        if (cached != null && cached.isValid()) {
            return new ArrayList<>(cached.data);
        }
        try {
            HttpResponse<String> response = sendRequest("GET", "/api/goals", null, true);
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode data = root.path("data");
                if (data.isArray()) {
                    List<Goal> list = objectMapper.readerFor(new TypeReference<List<Goal>>() {}).readValue(data);
                    ArrayList<Goal> result = new ArrayList<>(list);
                    goalsCache.put(userId, new CacheEntry<>(result, DEFAULT_TTL_MS));
                    return new ArrayList<>(result);
                }
            }
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Get goals failed: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public int createGoal(Goal goal) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("goalName", goal.getGoalName());
            payload.put("targetAmount", goal.getTargetAmount());
            payload.put("savedAmount", goal.getSavedAmount());
            payload.put("deadline", goal.getDeadline() != null ? goal.getDeadline().toString() : null);
            payload.put("priority", goal.getPriority());
            payload.put("category", goal.getCategory());
            payload.put("monthlyContribution", goal.getMonthlyContribution());
            payload.put("expectedReturn", goal.getExpectedReturn());

            HttpResponse<String> response = sendRequest("POST", "/api/goals", payload, true);
            goalsCache.clear();
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                JsonNode root = objectMapper.readTree(response.body());
                return root.path("data").path("goalId").asInt(1);
            }
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Create goal failed: " + e.getMessage());
        }
        return 0;
    }

    public boolean updateGoal(Goal goal) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("goalName", goal.getGoalName());
            payload.put("targetAmount", goal.getTargetAmount());
            payload.put("savedAmount", goal.getSavedAmount());
            payload.put("deadline", goal.getDeadline() != null ? goal.getDeadline().toString() : null);
            payload.put("priority", goal.getPriority());
            payload.put("category", goal.getCategory());
            payload.put("monthlyContribution", goal.getMonthlyContribution());
            payload.put("expectedReturn", goal.getExpectedReturn());

            HttpResponse<String> response = sendRequest("PUT", "/api/goals/" + goal.getGoalId(), payload, true);
            goalsCache.clear();
            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Update goal failed: " + e.getMessage());
            return false;
        }
    }

    public boolean contributeGoal(int id, double amount) {
        try {
            Map<String, Object> payload = Map.of("amount", amount);
            HttpResponse<String> response = sendRequest("POST", "/api/goals/" + id + "/contribute", payload, true);
            goalsCache.clear();
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Contribute to goal failed: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteGoal(int id) {
        try {
            HttpResponse<String> response = sendRequest("DELETE", "/api/goals/" + id, null, true);
            goalsCache.clear();
            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Delete goal failed: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // INVESTMENTS
    // =========================================================================

    public ArrayList<Investment> getInvestments(int userId) {
        CacheEntry<ArrayList<Investment>> cached = investmentsCache.get(userId);
        if (cached != null && cached.isValid()) {
            return new ArrayList<>(cached.data);
        }
        try {
            HttpResponse<String> response = sendRequest("GET", "/api/investments", null, true);
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode data = root.path("data");
                if (data.isArray()) {
                    List<Investment> list = objectMapper.readerFor(new TypeReference<List<Investment>>() {}).readValue(data);
                    ArrayList<Investment> result = new ArrayList<>(list);
                    investmentsCache.put(userId, new CacheEntry<>(result, DEFAULT_TTL_MS));
                    return new ArrayList<>(result);
                }
            }
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Get investments failed: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public int createInvestment(Investment inv) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", inv.getType());
            payload.put("amount", inv.getAmount());
            payload.put("returnRate", inv.getReturnRate());
            payload.put("startDate", inv.getStartDate() != null ? inv.getStartDate().toString() : null);

            HttpResponse<String> response = sendRequest("POST", "/api/investments", payload, true);
            investmentsCache.clear();
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                JsonNode root = objectMapper.readTree(response.body());
                return root.path("data").path("investmentId").asInt(1);
            }
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Create investment failed: " + e.getMessage());
        }
        return 0;
    }

    public boolean updateInvestment(Investment inv) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", inv.getType());
            payload.put("amount", inv.getAmount());
            payload.put("returnRate", inv.getReturnRate());
            payload.put("startDate", inv.getStartDate() != null ? inv.getStartDate().toString() : null);

            HttpResponse<String> response = sendRequest("PUT", "/api/investments/" + inv.getInvestmentId(), payload, true);
            investmentsCache.clear();
            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Update investment failed: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteInvestment(int id) {
        try {
            HttpResponse<String> response = sendRequest("DELETE", "/api/investments/" + id, null, true);
            investmentsCache.clear();
            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Delete investment failed: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // SUBSCRIPTIONS
    // =========================================================================

    public ArrayList<Subscription> getSubscriptions(int userId) {
        CacheEntry<ArrayList<Subscription>> cached = subscriptionsCache.get(userId);
        if (cached != null && cached.isValid()) {
            return new ArrayList<>(cached.data);
        }
        try {
            HttpResponse<String> response = sendRequest("GET", "/api/subscriptions", null, true);
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode data = root.path("data");
                if (data.isArray()) {
                    List<Subscription> list = objectMapper.readerFor(new TypeReference<List<Subscription>>() {}).readValue(data);
                    ArrayList<Subscription> result = new ArrayList<>(list);
                    subscriptionsCache.put(userId, new CacheEntry<>(result, DEFAULT_TTL_MS));
                    return new ArrayList<>(result);
                }
            }
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Get subscriptions failed: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public int createSubscription(Subscription sub) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("serviceName", sub.getServiceName());
            payload.put("amount", sub.getAmount());
            payload.put("billingCycle", sub.getBillingCycle());
            payload.put("nextBillingDate", sub.getNextBillingDate() != null ? sub.getNextBillingDate().toString() : null);
            payload.put("category", sub.getCategory());

            HttpResponse<String> response = sendRequest("POST", "/api/subscriptions", payload, true);
            subscriptionsCache.clear();
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                JsonNode root = objectMapper.readTree(response.body());
                return root.path("data").path("subscriptionId").asInt(1);
            }
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Create subscription failed: " + e.getMessage());
        }
        return 0;
    }

    public boolean updateSubscription(Subscription sub) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("serviceName", sub.getServiceName());
            payload.put("amount", sub.getAmount());
            payload.put("billingCycle", sub.getBillingCycle());
            payload.put("nextBillingDate", sub.getNextBillingDate() != null ? sub.getNextBillingDate().toString() : null);
            payload.put("category", sub.getCategory());
            payload.put("status", sub.getStatus());

            HttpResponse<String> response = sendRequest("PUT", "/api/subscriptions/" + sub.getSubscriptionId(), payload, true);
            subscriptionsCache.clear();
            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Update subscription failed: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteSubscription(int id) {
        try {
            HttpResponse<String> response = sendRequest("DELETE", "/api/subscriptions/" + id, null, true);
            subscriptionsCache.clear();
            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Delete subscription failed: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // ASSETS & LIABILITIES
    // =========================================================================

    public ArrayList<Asset> getAssets(int userId) {
        try {
            HttpResponse<String> response = sendRequest("GET", "/api/assets", null, true);
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode data = root.path("data");
                if (data.isArray()) {
                    List<Asset> list = objectMapper.readerFor(new TypeReference<List<Asset>>() {}).readValue(data);
                    return new ArrayList<>(list);
                }
            }
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Get assets failed: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public int createAsset(Asset asset) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("name", asset.getName());
            payload.put("type", asset.getType());
            payload.put("value", asset.getValue());
            payload.put("notes", asset.getNotes());

            HttpResponse<String> response = sendRequest("POST", "/api/assets", payload, true);
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                JsonNode root = objectMapper.readTree(response.body());
                return root.path("data").path("assetId").asInt(1);
            }
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Create asset failed: " + e.getMessage());
        }
        return 0;
    }

    public ArrayList<Liability> getLiabilities(int userId) {
        try {
            HttpResponse<String> response = sendRequest("GET", "/api/liabilities", null, true);
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode data = root.path("data");
                if (data.isArray()) {
                    List<Liability> list = objectMapper.readerFor(new TypeReference<List<Liability>>() {}).readValue(data);
                    return new ArrayList<>(list);
                }
            }
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Get liabilities failed: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public int createLiability(Liability liab) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("name", liab.getName());
            payload.put("principal", liab.getPrincipal());
            payload.put("remainingBalance", liab.getRemainingBalance());
            payload.put("emi", liab.getEmi());
            payload.put("interestRate", liab.getInterestRate());

            HttpResponse<String> response = sendRequest("POST", "/api/liabilities", payload, true);
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                JsonNode root = objectMapper.readTree(response.body());
                return root.path("data").path("liabilityId").asInt(1);
            }
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Create liability failed: " + e.getMessage());
        }
        return 0;
    }

    // =========================================================================
    // PROFILE UPDATE
    // =========================================================================

    public boolean updateProfile(User user) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("name", user.getName());
            payload.put("phone", user.getPhone());
            payload.put("currency", user.getCurrency());
            payload.put("incomeRange", user.getIncomeRange());

            HttpResponse<String> response = sendRequest("PUT", "/api/users/profile", payload, true);
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("[FinvisIQ API] Update profile failed: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // NOTIFICATIONS
    // =========================================================================

    public ArrayList<Notification> getNotifications(int userId) {
        CacheEntry<ArrayList<Notification>> cached = notificationsCache.get(userId);
        if (cached != null && cached.isValid()) {
            return new ArrayList<>(cached.data);
        }
        try {
            HttpResponse<String> response = sendRequest("GET", "/api/notifications", null, true);
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode data = root.path("data");
                if (data.isArray()) {
                    List<Notification> list = objectMapper.readerFor(new TypeReference<List<Notification>>() {}).readValue(data);
                    ArrayList<Notification> result = new ArrayList<>(list);
                    notificationsCache.put(userId, new CacheEntry<>(result, DEFAULT_TTL_MS));
                    return new ArrayList<>(result);
                }
            }
        } catch (Exception e) {
            // graceful fallback
        }
        return new ArrayList<>();
    }

    public boolean markNotificationAsRead(int id) {
        try {
            HttpResponse<String> response = sendRequest("PUT", "/api/notifications/" + id + "/read", null, true);
            notificationsCache.clear();
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteNotification(int id) {
        try {
            HttpResponse<String> response = sendRequest("DELETE", "/api/notifications/" + id, null, true);
            notificationsCache.clear();
            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            return false;
        }
    }

    // =========================================================================
    // GENERIC HTTP DISPATCHER
    // =========================================================================

    private HttpResponse<String> sendRequest(String method, String path, Object body, boolean requireAuth) throws Exception {
        String baseUrl = ApiConfig.getBaseUrl();
        String fullUrl = baseUrl + (path.startsWith("/") ? path : "/" + path);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");

        if (requireAuth && authToken != null) {
            builder.header("Authorization", "Bearer " + authToken);
        }

        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.noBody();
        if (body != null) {
            String jsonStr = objectMapper.writeValueAsString(body);
            publisher = HttpRequest.BodyPublishers.ofString(jsonStr);
        }

        switch (method.toUpperCase()) {
            case "GET":
                builder.GET();
                break;
            case "POST":
                builder.POST(publisher);
                break;
            case "PUT":
                builder.PUT(publisher);
                break;
            case "DELETE":
                builder.DELETE();
                break;
            default:
                builder.method(method, publisher);
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
