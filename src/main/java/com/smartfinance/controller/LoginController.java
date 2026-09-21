package com.smartfinance.controller;

import com.smartfinance.App;
import com.smartfinance.model.User;
import com.smartfinance.service.AuthService;
import com.smartfinance.util.AnimationUtils;
import com.smartfinance.util.PasswordUtil;
import com.smartfinance.util.ThemeManager;
import com.smartfinance.util.ValidationUtils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

import java.io.InputStream;

/**
 * Premium Login & Registration Controller for FinvisIQ.
 * Includes Theme Switcher (Light/Dark mode) and Exit options.
 * Fully functional tab switching between Sign In & Registration.
 */
public class LoginController {
    private final App app;
    private final AuthService authService;
    private final ThemeManager themeManager;

    private StackPane formContainer;
    private Label messageLabel;
    private Button loginTabBtn;
    private Button registerTabBtn;
    private Button themeToggleBtn;
    private boolean showingLogin = true;

    public LoginController(App app) {
        this.app = app;
        this.authService = new AuthService();
        this.themeManager = ThemeManager.getInstance();
    }

    public StackPane createView() {
        StackPane root = new StackPane();
        root.getStyleClass().add("login-bg");

        // Background Image Layer with smooth scaling
        try {
            InputStream is = getClass().getResourceAsStream("/images/login-bg.png");
            if (is != null) {
                Image bgImg = new Image(is);
                ImageView bgView = new ImageView(bgImg);
                bgView.setPreserveRatio(false);
                bgView.fitWidthProperty().bind(root.widthProperty());
                bgView.fitHeightProperty().bind(root.heightProperty());
                root.getChildren().add(bgView);
            }
        } catch (Exception ignore) {}

        // Floating decorative shapes overlay
        Pane decorations = createDecorations();

        // Top Bar (Theme Toggle & Sign Out / Exit)
        HBox topBar = createTopBar();

        // Center Glass Login Card
        VBox card = createLoginCard();
        card.getStyleClass().add("login-card");
        card.setMaxWidth(450);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        // Ensure card is 100% opaque and fully visible by default
        card.setOpacity(1.0);
        card.setScaleX(1.0);
        card.setScaleY(1.0);

        root.getChildren().addAll(decorations, topBar, card);
        StackPane.setAlignment(topBar, Pos.TOP_RIGHT);
        StackPane.setAlignment(card, Pos.CENTER);

        return root;
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(24, 30, 0, 0));

        // Theme Switch Button
        themeToggleBtn = new Button(themeManager.getThemeIcon() + " " + themeManager.getThemeName());
        themeToggleBtn.getStyleClass().add("glass-button");
        themeToggleBtn.setStyle("-fx-padding: 8 16; -fx-font-size: 13px;");
        themeToggleBtn.setOnAction(e -> {
            themeManager.toggleTheme();
            themeToggleBtn.setText(themeManager.getThemeIcon() + " " + themeManager.getThemeName());
        });

        // Sign Out / Exit Button
        Button signOutBtn = new Button("\u2190 Exit App");
        signOutBtn.getStyleClass().add("btn-secondary");
        signOutBtn.setStyle("-fx-padding: 8 16; -fx-font-size: 13px;");
        signOutBtn.setOnAction(e -> {
            authService.logout();
            app.getPrimaryStage().close();
        });

        topBar.getChildren().addAll(themeToggleBtn, signOutBtn);
        return topBar;
    }

    private Pane createDecorations() {
        Pane pane = new Pane();
        pane.setPickOnBounds(false);

        String[] colors = {
            "rgba(124, 58, 237, ", "rgba(6, 182, 212, ",
            "rgba(16, 185, 129, ", "rgba(236, 72, 153, ",
            "rgba(99, 102, 241, "
        };

        for (int i = 0; i < 6; i++) {
            Region circle = new Region();
            double size = 70 + Math.random() * 150;
            circle.setMinSize(size, size);
            circle.setMaxSize(size, size);
            String color = colors[i % colors.length];
            circle.setStyle(
                "-fx-background-color: " + color + (0.02 + Math.random() * 0.05) + ");" +
                "-fx-background-radius: " + (size / 2) + ";"
            );
            circle.setLayoutX(Math.random() * 1200);
            circle.setLayoutY(Math.random() * 750);
            pane.getChildren().add(circle);
            AnimationUtils.float_(circle);
        }

        return pane;
    }

    private VBox createLoginCard() {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);

        // Logo / Branding Title
        Label emoji = new Label("\uD83D\uDCB0");
        emoji.setStyle("-fx-font-size: 48px;");

        Label title = new Label("FinvisIQ");
        title.getStyleClass().add("login-title");

        Label subtitle = new Label("Personal Finance Intelligence Platform");
        subtitle.getStyleClass().add("login-subtitle");

        VBox titleBox = new VBox(4, emoji, title, subtitle);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(0, 0, 16, 0));

        // Tab buttons (Login / Register)
        HBox tabs = createTabs();

        // Message label
        messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setTextAlignment(TextAlignment.CENTER);
        messageLabel.setMaxWidth(380);

        // Form container (swaps between login and register)
        formContainer = new StackPane();
        formContainer.getChildren().add(createLoginForm());

        card.getChildren().addAll(titleBox, tabs, messageLabel, formContainer);
        return card;
    }

    private HBox createTabs() {
        loginTabBtn = new Button("Sign In");
        loginTabBtn.getStyleClass().addAll("tab-btn", "tab-btn-active");
        loginTabBtn.setId("loginTab");

        registerTabBtn = new Button("Create Account");
        registerTabBtn.getStyleClass().add("tab-btn");
        registerTabBtn.setId("registerTab");

        loginTabBtn.setOnAction(e -> {
            clearMessage();
            showLoginTab();
        });
        registerTabBtn.setOnAction(e -> {
            clearMessage();
            showRegisterTab();
        });

        HBox tabs = new HBox(0, loginTabBtn, registerTabBtn);
        tabs.setAlignment(Pos.CENTER);
        tabs.setPadding(new Insets(0, 0, 10, 0));
        return tabs;
    }

    private void showLoginTab() {
        showingLogin = true;
        if (loginTabBtn != null && registerTabBtn != null) {
            if (!loginTabBtn.getStyleClass().contains("tab-btn-active")) {
                loginTabBtn.getStyleClass().add("tab-btn-active");
            }
            registerTabBtn.getStyleClass().remove("tab-btn-active");
        }
        switchForm(createLoginForm());
    }

    private void showRegisterTab() {
        showingLogin = false;
        if (loginTabBtn != null && registerTabBtn != null) {
            if (!registerTabBtn.getStyleClass().contains("tab-btn-active")) {
                registerTabBtn.getStyleClass().add("tab-btn-active");
            }
            loginTabBtn.getStyleClass().remove("tab-btn-active");
        }
        switchForm(createRegisterForm());
    }

    private void switchForm(VBox newForm) {
        formContainer.getChildren().setAll(newForm);
        AnimationUtils.fadeIn(newForm, 300);
    }

    private void clearMessage() {
        if (messageLabel != null) {
            messageLabel.setText("");
            messageLabel.getStyleClass().removeAll("error-label", "success-label");
        }
    }

    private VBox createLoginForm() {
        VBox form = new VBox(16);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(10, 0, 0, 0));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username or Email");
        usernameField.getStyleClass().add("text-field-modern");
        usernameField.setId("loginUsername");

        // Password field with Toggle visibility
        StackPane passwordPane = new StackPane();
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("password-field-modern");
        passwordField.setId("loginPassword");

        TextField visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("Password");
        visiblePasswordField.getStyleClass().add("text-field-modern");
        visiblePasswordField.setVisible(false);

        Button togglePassBtn = new Button("\uD83D\uDC41");
        togglePassBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #8B8FA3; -fx-cursor: hand; -fx-padding: 0 12;");
        StackPane.setAlignment(togglePassBtn, Pos.CENTER_RIGHT);

        togglePassBtn.setOnAction(e -> {
            if (passwordField.isVisible()) {
                visiblePasswordField.setText(passwordField.getText());
                passwordField.setVisible(false);
                visiblePasswordField.setVisible(true);
            } else {
                passwordField.setText(visiblePasswordField.getText());
                visiblePasswordField.setVisible(false);
                passwordField.setVisible(true);
            }
        });

        passwordPane.getChildren().addAll(passwordField, visiblePasswordField, togglePassBtn);

        Button loginBtn = new Button("Sign In to FinvisIQ");
        loginBtn.getStyleClass().add("btn-primary");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setId("loginBtn");

        Runnable loginAction = () -> {
            String pwd = passwordField.isVisible() ? passwordField.getText() : visiblePasswordField.getText();
            handleLogin(usernameField.getText(), pwd);
        };

        loginBtn.setOnAction(e -> loginAction.run());
        passwordField.setOnAction(e -> loginAction.run());
        visiblePasswordField.setOnAction(e -> loginAction.run());

        form.getChildren().addAll(usernameField, passwordPane, loginBtn);
        return form;
    }

    private VBox createRegisterForm() {
        VBox form = new VBox(12);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(6, 0, 0, 0));

        TextField nameField = new TextField();
        nameField.setPromptText("Username");
        nameField.getStyleClass().add("text-field-modern");

        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        emailField.getStyleClass().add("text-field-modern");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (min 4 chars)");
        passwordField.getStyleClass().add("password-field-modern");

        // Password strength meter
        ProgressBar strengthBar = new ProgressBar(0);
        strengthBar.setMaxWidth(Double.MAX_VALUE);
        strengthBar.setPrefHeight(4);
        strengthBar.getStyleClass().add("glass-progress");

        passwordField.textProperty().addListener((obs, o, n) -> {
            int score = PasswordUtil.calculatePasswordStrength(n);
            strengthBar.setProgress(score / 100.0);
        });

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm Password");
        confirmField.getStyleClass().add("password-field-modern");

        TextField ageField = new TextField();
        ageField.setPromptText("Age (e.g. 25)");
        ageField.getStyleClass().add("text-field-modern");

        // Role selection
        Label roleLabel = new Label("Account Role:");
        roleLabel.getStyleClass().add("label-bold");

        ToggleGroup roleGroup = new ToggleGroup();
        RadioButton userRadio = new RadioButton("Common User");
        userRadio.setToggleGroup(roleGroup);
        userRadio.setSelected(true);

        RadioButton adminRadio = new RadioButton("Admin");
        adminRadio.setToggleGroup(roleGroup);

        HBox roleBox = new HBox(20, userRadio, adminRadio);
        roleBox.setAlignment(Pos.CENTER);

        VBox roleSection = new VBox(4, roleLabel, roleBox);
        roleSection.setAlignment(Pos.CENTER);

        Button registerBtn = new Button("Register Account");
        registerBtn.getStyleClass().add("btn-primary");
        registerBtn.setMaxWidth(Double.MAX_VALUE);

        registerBtn.setOnAction(e -> {
            String role = adminRadio.isSelected() ? "ADMIN" : "USER";
            handleRegister(nameField.getText(), emailField.getText(),
                          passwordField.getText(), confirmField.getText(), ageField.getText(), role);
        });

        form.getChildren().addAll(nameField, emailField, passwordField, strengthBar, confirmField, ageField, roleSection, registerBtn);
        return form;
    }

    private void handleLogin(String username, String password) {
        if (username == null || username.isBlank()) {
            showError("Please enter your username or email.");
            return;
        }
        if (password == null || password.isBlank()) {
            showError("Please enter your password.");
            return;
        }

        User user = authService.login(username, password);
        if (user != null) {
            showSuccess("Authentication successful! Welcome, " + user.getName());
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(500));
            pause.setOnFinished(e -> app.showMainApp(user));
            pause.play();
        } else {
            showError("Invalid credentials or account suspended.");
            AnimationUtils.shake(formContainer);
        }
    }

    private void handleRegister(String name, String email, String password, String confirm, String ageStr, String role) {
        if (!ValidationUtils.isValidName(name)) {
            showError("Please enter a valid username (min 2 chars).");
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            showError("Password must be at least 4 characters.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            AnimationUtils.shake(formContainer);
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr.trim());
            if (age < 18 || age > 100) {
                showError("Age must be between 18 and 100.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid age.");
            return;
        }

        User user = authService.register(name, email, password, role, age);
        if (user != null) {
            showSuccess("Account registered successfully! Switching to Sign In...");
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1000));
            pause.setOnFinished(e -> {
                showLoginTab();
                showSuccess("Account registered! You may now sign in.");
            });
            pause.play();
        } else {
            showError("Registration failed. Email or username might already be registered.");
            AnimationUtils.shake(formContainer);
        }
    }

    private void showError(String msg) {
        messageLabel.setText(msg);
        messageLabel.getStyleClass().removeAll("success-label", "error-label");
        messageLabel.getStyleClass().add("error-label");
        AnimationUtils.fadeIn(messageLabel, 200);
    }

    private void showSuccess(String msg) {
        messageLabel.setText(msg);
        messageLabel.getStyleClass().removeAll("success-label", "error-label");
        messageLabel.getStyleClass().add("success-label");
        AnimationUtils.fadeIn(messageLabel, 200);
    }
}
