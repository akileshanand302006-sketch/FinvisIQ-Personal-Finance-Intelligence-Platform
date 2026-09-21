package com.smartfinance.controller;

import com.smartfinance.App;
import com.smartfinance.model.User;
import com.smartfinance.util.AnimationUtils;
import com.smartfinance.util.ThemeManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.HashMap;

/**
 * Main Application Controller — Sidebar navigation, Top Header Bar, and view management for FinvisIQ.
 * Features ALWAYS-VISIBLE Theme Switcher (Light/Dark mode) and ALWAYS-VISIBLE Sign Out Button.
 * Demonstrates: MVC Architecture, Event Handling, HashMap.
 */
public class MainController {
    private final App app;
    private final User currentUser;
    private final ThemeManager themeManager;

    private StackPane contentArea;
    private VBox navContainer;
    private Button activeNavBtn;
    private Label topPageTitle;
    private Button topThemeBtn;
    private Label sidebarThemeIcon;
    private Label sidebarThemeText;

    @SuppressWarnings("unused")
    private final HashMap<String, Node> viewCache = new HashMap<>();

    // Feature controllers
    private DashboardController dashboardController;
    private TransactionController transactionController;
    private ReportsController reportsController;
    private InvestmentController investmentController;
    private GoalController goalController;
    private NotificationController notificationController;
    private AdminController adminController;
    private ProfileController profileController;
    private SettingsController settingsController;
    private BudgetController budgetController;
    private SubscriptionController subscriptionController;
    private SIPPlannerController sipPlannerController;
    private AnalyticsController analyticsController;
    private InsightsController insightsController;
    private CalendarController calendarController;

    // Nav items with icons (using Unicode) — grouped by sections
    private static final String[][] NAV_MAIN = {
        {"\u2302", "Dashboard"},
        {"\u002B", "Transactions"},
        {"\uD83D\uDCF1", "Subscriptions"}
    };

    private static final String[][] NAV_PLANNING = {
        {"\uD83D\uDCCA", "Budget"},
        {"\u2605", "Goals"},
        {"\u2197", "Investments"},
        {"\uD83D\uDCA1", "SIP Planner"}
    };

    private static final String[][] NAV_ANALYTICS = {
        {"\u2261", "Reports"},
        {"\uD83D\uDCC8", "Analytics"},
        {"\uD83E\uDD16", "Insights"},
        {"\uD83D\uDCC5", "Calendar"},
        {"\u266A", "Notifications"},
        {"\uD83D\uDC64", "Profile"},
        {"\u2699", "Settings"}
    };

    private static final String[][] ADMIN_NAV = {
        {"\uD83D\uEE16", "Admin Panel"}
    };

    public MainController(App app, User user) {
        this.app = app;
        this.currentUser = user;
        this.themeManager = ThemeManager.getInstance();
        initControllers();
    }

    private void initControllers() {
        dashboardController = new DashboardController(currentUser, this);
        transactionController = new TransactionController(currentUser, this);
        reportsController = new ReportsController(currentUser);
        investmentController = new InvestmentController(currentUser);
        goalController = new GoalController(currentUser);
        notificationController = new NotificationController(currentUser);
        profileController = new ProfileController(currentUser, this);
        settingsController = new SettingsController(currentUser);
        budgetController = new BudgetController(currentUser);
        subscriptionController = new SubscriptionController(currentUser);
        sipPlannerController = new SIPPlannerController(currentUser);
        analyticsController = new AnalyticsController(currentUser);
        insightsController = new InsightsController(currentUser);
        calendarController = new CalendarController(currentUser);

        if (currentUser.isAdmin()) {
            adminController = new AdminController(currentUser);
        }
    }

    public BorderPane createView() {
        BorderPane root = new BorderPane();

        // Always-Visible Top Header Bar (Theme Toggle + Sign Out)
        HBox topHeader = createTopHeader();
        root.setTop(topHeader);

        // Sidebar
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        // Content area
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");

        ScrollPane scrollPane = new ScrollPane(contentArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("scroll-pane");

        root.setCenter(scrollPane);

        // Show dashboard by default
        javafx.application.Platform.runLater(() -> navigateTo("Dashboard"));

        return root;
    }

    private HBox createTopHeader() {
        HBox header = new HBox(16);
        header.getStyleClass().add("glass-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 24, 12, 24));

        topPageTitle = new Label("Dashboard");
        topPageTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        topPageTitle.getStyleClass().add("label-bold");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Notification Button
        Button topNotifBtn = new Button("\uD83D\uDD14 Notifications");
        topNotifBtn.getStyleClass().add("glass-button");
        topNotifBtn.setStyle("-fx-padding: 8 16; -fx-font-size: 13px;");
        topNotifBtn.setOnAction(e -> navigateTo("Notifications"));

        // Always-Visible Theme Switch Button
        topThemeBtn = new Button(themeManager.getThemeIcon() + " " + themeManager.getThemeName());
        topThemeBtn.getStyleClass().add("glass-button");
        topThemeBtn.setStyle("-fx-padding: 8 16; -fx-font-size: 13px;");
        topThemeBtn.setOnAction(e -> toggleThemeAll());

        // Always-Visible Sign Out Button
        Button signOutBtn = new Button("\uD83D\uDEAA Sign Out");
        signOutBtn.getStyleClass().add("btn-danger");
        signOutBtn.setStyle("-fx-padding: 8 18; -fx-font-size: 13px;");
        signOutBtn.setOnAction(e -> app.showLoginScreen());

        header.getChildren().addAll(topPageTitle, spacer, topNotifBtn, topThemeBtn, signOutBtn);
        return header;
    }

    private void toggleThemeAll() {
        themeManager.toggleTheme();
        String name = themeManager.getThemeName();
        String icon = themeManager.getThemeIcon();

        if (topThemeBtn != null) {
            topThemeBtn.setText(icon + " " + name);
        }
        if (sidebarThemeIcon != null) {
            sidebarThemeIcon.setText(icon);
        }
        if (sidebarThemeText != null) {
            sidebarThemeText.setText(name);
        }
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(264);

        // Header section
        VBox header = createSidebarHeader();

        // Separator
        Region sep1 = new Region();
        sep1.getStyleClass().add("sidebar-separator");
        sep1.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(sep1, new Insets(0, 16, 8, 16));

        // Navigation container
        navContainer = new VBox(2);
        navContainer.setPadding(new Insets(0, 12, 0, 12));

        // Main section
        Label mainLabel = new Label("MAIN");
        mainLabel.getStyleClass().add("nav-section-label");
        navContainer.getChildren().add(mainLabel);
        for (String[] item : NAV_MAIN) {
            navContainer.getChildren().add(createNavButton(item[0], item[1]));
        }

        // Planning section
        Region sep2 = new Region();
        sep2.getStyleClass().add("sidebar-separator");
        sep2.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(sep2, new Insets(6, 4, 4, 4));
        navContainer.getChildren().add(sep2);

        Label planLabel = new Label("PLANNING");
        planLabel.getStyleClass().add("nav-section-label");
        navContainer.getChildren().add(planLabel);
        for (String[] item : NAV_PLANNING) {
            navContainer.getChildren().add(createNavButton(item[0], item[1]));
        }

        // Analytics & System section
        Region sep3 = new Region();
        sep3.getStyleClass().add("sidebar-separator");
        sep3.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(sep3, new Insets(6, 4, 4, 4));
        navContainer.getChildren().add(sep3);

        Label analyticsLabel = new Label("SYSTEM & INTELLIGENCE");
        analyticsLabel.getStyleClass().add("nav-section-label");
        navContainer.getChildren().add(analyticsLabel);
        for (String[] item : NAV_ANALYTICS) {
            navContainer.getChildren().add(createNavButton(item[0], item[1]));
        }

        // Admin section
        if (currentUser.isAdmin()) {
            Region sep4 = new Region();
            sep4.getStyleClass().add("sidebar-separator");
            sep4.setMaxWidth(Double.MAX_VALUE);
            VBox.setMargin(sep4, new Insets(6, 4, 4, 4));
            navContainer.getChildren().add(sep4);

            Label adminLabel = new Label("ADMINISTRATION");
            adminLabel.getStyleClass().add("nav-section-label");
            navContainer.getChildren().add(adminLabel);

            for (String[] item : ADMIN_NAV) {
                navContainer.getChildren().add(createNavButton(item[0], item[1]));
            }
        }

        // Sidebar scroll pane so navigation items are NEVER cropped
        ScrollPane navScroll = new ScrollPane(navContainer);
        navScroll.setFitToWidth(true);
        navScroll.setFitToHeight(false);
        navScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        navScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        navScroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(navScroll, Priority.ALWAYS);

        // Bottom section (Theme toggle + Logout)
        VBox bottom = createBottomSection();

        sidebar.getChildren().addAll(header, sep1, navScroll, bottom);
        return sidebar;
    }

    private VBox createSidebarHeader() {
        VBox header = new VBox(8);
        header.getStyleClass().add("sidebar-header");
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(24, 20, 16, 20));

        Label logo = new Label("\uD83D\uDCB0 FinvisIQ");
        logo.getStyleClass().add("sidebar-logo");

        Label tagline = new Label("Personal Finance Intelligence");
        tagline.getStyleClass().add("sidebar-subtitle");

        String initials = getInitials(currentUser.getName());
        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("sidebar-avatar");
        Label avatarText = new Label(initials);
        avatarText.getStyleClass().add("sidebar-avatar-text");
        avatar.getChildren().add(avatarText);

        Label userName = new Label(currentUser.getName());
        userName.getStyleClass().add("sidebar-username");

        Label userRole = new Label(currentUser.getRole());
        userRole.getStyleClass().add("sidebar-role");

        VBox userInfo = new VBox(4, avatar, userName, userRole);
        userInfo.setAlignment(Pos.CENTER);
        userInfo.setPadding(new Insets(12, 0, 0, 0));

        header.getChildren().addAll(logo, tagline, userInfo);
        return header;
    }

    private Button createNavButton(String icon, String text) {
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("nav-icon");
        iconLabel.setMinWidth(28);
        iconLabel.setAlignment(Pos.CENTER);

        Button btn = new Button(text);
        btn.setGraphic(iconLabel);
        btn.getStyleClass().add("nav-item");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setId("nav_" + text.replace(" ", "_"));

        btn.setOnAction(e -> {
            navigateTo(text);
            setActiveNav(btn);
        });

        AnimationUtils.addHoverScale(btn, 1.02);
        return btn;
    }

    private void setActiveNav(Button btn) {
        if (activeNavBtn != null) {
            activeNavBtn.getStyleClass().remove("nav-item-active");
        }
        btn.getStyleClass().add("nav-item-active");
        activeNavBtn = btn;
    }

    public void navigateTo(String viewName) {
        if (topPageTitle != null) {
            topPageTitle.setText(viewName);
        }

        Node view;

        for (Node child : navContainer.getChildren()) {
            if (child instanceof Button navBtn) {
                if (navBtn.getText().equals(viewName)) {
                    setActiveNav(navBtn);
                    break;
                }
            }
        }

        switch (viewName) {
            case "Dashboard" -> {
                dashboardController = new DashboardController(currentUser, this);
                view = dashboardController.createView();
            }
            case "Transactions" -> {
                transactionController = new TransactionController(currentUser, this);
                view = transactionController.createView();
            }
            case "Subscriptions" -> {
                subscriptionController = new SubscriptionController(currentUser);
                view = subscriptionController.createView();
            }
            case "Budget" -> {
                budgetController = new BudgetController(currentUser);
                view = budgetController.createView();
            }
            case "Goals" -> {
                goalController = new GoalController(currentUser);
                view = goalController.createView();
            }
            case "Investments" -> {
                investmentController = new InvestmentController(currentUser);
                view = investmentController.createView();
            }
            case "SIP Planner" -> {
                sipPlannerController = new SIPPlannerController(currentUser);
                view = sipPlannerController.createView();
            }
            case "Reports" -> {
                reportsController = new ReportsController(currentUser);
                view = reportsController.createView();
            }
            case "Analytics" -> {
                analyticsController = new AnalyticsController(currentUser);
                view = analyticsController.createView();
            }
            case "Insights" -> {
                insightsController = new InsightsController(currentUser);
                view = insightsController.createView();
            }
            case "Calendar" -> {
                calendarController = new CalendarController(currentUser);
                view = calendarController.createView();
            }
            case "Notifications" -> {
                notificationController = new NotificationController(currentUser);
                view = notificationController.createView();
            }
            case "Profile" -> {
                profileController = new ProfileController(currentUser, this);
                view = profileController.createView();
            }
            case "Settings" -> {
                settingsController = new SettingsController(currentUser);
                view = settingsController.createView();
            }
            case "Admin Panel" -> {
                if (currentUser.isAdmin()) {
                    adminController = new AdminController(currentUser);
                    view = adminController.createView();
                } else return;
            }
            default -> { return; }
        }

        contentArea.getChildren().setAll(view);
        AnimationUtils.fadeIn(view, 300);
    }

    private VBox createBottomSection() {
        VBox bottom = new VBox(8);
        bottom.setPadding(new Insets(12, 16, 20, 16));

        HBox themeToggle = new HBox(10);
        themeToggle.getStyleClass().add("theme-toggle");
        themeToggle.setAlignment(Pos.CENTER_LEFT);

        sidebarThemeIcon = new Label(themeManager.getThemeIcon());
        sidebarThemeIcon.setStyle("-fx-font-size: 18px;");
        sidebarThemeText = new Label(themeManager.getThemeName());
        sidebarThemeText.setStyle("-fx-text-fill: #CBD5E1; -fx-font-size: 13px; -fx-font-weight: bold;");

        themeToggle.getChildren().addAll(sidebarThemeIcon, sidebarThemeText);
        themeToggle.setOnMouseClicked(e -> toggleThemeAll());

        Button logoutBtn = new Button("\u2190  Sign Out");
        logoutBtn.getStyleClass().add("nav-item");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setAlignment(Pos.CENTER_LEFT);
        logoutBtn.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
        logoutBtn.setId("logoutBtn");

        logoutBtn.setOnAction(e -> app.showLoginScreen());

        bottom.getChildren().addAll(themeToggle, logoutBtn);
        return bottom;
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        }
        return ("" + parts[0].charAt(0)).toUpperCase();
    }

    public User getCurrentUser() { return currentUser; }
    public App getApp() { return app; }
}
