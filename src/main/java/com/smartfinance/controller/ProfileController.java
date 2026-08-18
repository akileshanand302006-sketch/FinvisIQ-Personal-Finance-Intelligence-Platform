package com.smartfinance.controller;

import com.smartfinance.dao.UserDAO;
import com.smartfinance.model.User;
import com.smartfinance.util.AnimationUtils;
import com.smartfinance.util.PasswordUtil;
import com.smartfinance.util.ValidationUtils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Profile Controller — View & update user account information, settings & security.
 * Demonstrates: Form handling, Input validation, Password change.
 */
public class ProfileController {
    private final User currentUser;
    private final UserDAO userDAO;
    private final MainController mainController;

    public ProfileController(User user, MainController mainController) {
        this.currentUser = user;
        this.mainController = mainController;
        this.userDAO = new UserDAO();
    }

    public VBox createView() {
        VBox root = new VBox(24);
        root.setPadding(new Insets(30));

        Label title = new Label("User Profile & Account");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Manage your account details and security settings");
        subtitle.getStyleClass().add("page-subtitle");

        HBox mainRow = new HBox(20);

        // Left card: Profile Overview & Edit Form
        VBox profileCard = createProfileCard();

        // Right card: Security & Password Change
        VBox securityCard = createSecurityCard();

        HBox.setHgrow(profileCard, Priority.ALWAYS);
        HBox.setHgrow(securityCard, Priority.ALWAYS);
        mainRow.getChildren().addAll(profileCard, securityCard);

        root.getChildren().addAll(title, subtitle, mainRow);
        javafx.application.Platform.runLater(() -> AnimationUtils.staggerChildren(root, 70));
        return root;
    }

    private VBox createProfileCard() {
        VBox card = new VBox(16);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(24));

        Label sectionTitle = new Label("\uD83D\uDC64 Personal Details");
        sectionTitle.getStyleClass().add("section-title");

        // Avatar preview
        HBox avatarBox = new HBox(16);
        avatarBox.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("sidebar-avatar");
        avatar.setStyle("-fx-min-width: 64; -fx-min-height: 64; -fx-max-width: 64; -fx-max-height: 64;");

        String initials = currentUser.getName() != null && !currentUser.getName().isBlank() ?
                currentUser.getName().substring(0, 1).toUpperCase() : "?";
        Label avatarText = new Label(initials);
        avatarText.getStyleClass().add("sidebar-avatar-text");
        avatar.getChildren().add(avatarText);

        VBox userDetails = new VBox(4);
        Label nameLbl = new Label(currentUser.getName());
        nameLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #E2E8F0;");

        Label roleBadge = new Label(currentUser.getRole());
        roleBadge.getStyleClass().add("sidebar-role");

        userDetails.getChildren().addAll(nameLbl, roleBadge);
        avatarBox.getChildren().addAll(avatar, userDetails);

        // Fields
        TextField nameField = new TextField(currentUser.getName());
        nameField.setPromptText("Username");
        nameField.getStyleClass().add("text-field-modern");

        TextField emailField = new TextField(currentUser.getEmail());
        emailField.setPromptText("Email Address");
        emailField.getStyleClass().add("text-field-modern");

        TextField phoneField = new TextField(currentUser.getPhone() != null ? currentUser.getPhone() : "");
        phoneField.setPromptText("Phone Number (Optional)");
        phoneField.getStyleClass().add("text-field-modern");

        TextField ageField = new TextField(String.valueOf(currentUser.getAge()));
        ageField.setPromptText("Age");
        ageField.getStyleClass().add("text-field-modern");

        ComboBox<String> currencyCombo = new ComboBox<>();
        currencyCombo.getItems().addAll("INR", "USD", "EUR", "GBP", "JPY");
        currencyCombo.setValue(currentUser.getCurrency());
        currencyCombo.getStyleClass().add("combo-modern");
        currencyCombo.setMaxWidth(Double.MAX_VALUE);

        Label msgLabel = new Label();

        Button saveBtn = new Button("Save Profile Changes");
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String ageStr = ageField.getText().trim();

            if (!ValidationUtils.isValidName(name)) {
                msgLabel.setText("Please enter a valid username.");
                msgLabel.getStyleClass().setAll("error-label");
                return;
            }
            if (!ValidationUtils.isValidEmail(email)) {
                msgLabel.setText("Please enter a valid email address.");
                msgLabel.getStyleClass().setAll("error-label");
                return;
            }
            int age;
            try {
                age = Integer.parseInt(ageStr);
                if (age < 18 || age > 100) throw new NumberFormatException();
            } catch (Exception ex) {
                msgLabel.setText("Age must be between 18 and 100.");
                msgLabel.getStyleClass().setAll("error-label");
                return;
            }

            currentUser.setName(name);
            currentUser.setEmail(email);
            currentUser.setPhone(phoneField.getText().trim());
            currentUser.setAge(age);
            currentUser.setCurrency(currencyCombo.getValue());

            if (userDAO.update(currentUser)) {
                msgLabel.setText("Profile updated successfully!");
                msgLabel.getStyleClass().setAll("success-label");
                AnimationUtils.fadeIn(msgLabel, 200);
            } else {
                msgLabel.setText("Failed to update profile.");
                msgLabel.getStyleClass().setAll("error-label");
            }
        });

        card.getChildren().addAll(sectionTitle, avatarBox, nameField, emailField, phoneField, ageField, currencyCombo, saveBtn, msgLabel);
        return card;
    }

    private VBox createSecurityCard() {
        VBox card = new VBox(16);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(24));

        Label sectionTitle = new Label("\uD83D\uDD12 Security & Password");
        sectionTitle.getStyleClass().add("section-title");

        PasswordField currentPass = new PasswordField();
        currentPass.setPromptText("Current Password");
        currentPass.getStyleClass().add("password-field-modern");

        PasswordField newPass = new PasswordField();
        newPass.setPromptText("New Password (min 4 chars)");
        newPass.getStyleClass().add("password-field-modern");

        PasswordField confirmPass = new PasswordField();
        confirmPass.setPromptText("Confirm New Password");
        confirmPass.getStyleClass().add("password-field-modern");

        Label msgLabel = new Label();

        Button updatePassBtn = new Button("Update Password");
        updatePassBtn.getStyleClass().add("btn-secondary");
        updatePassBtn.setOnAction(e -> {
            String curr = currentPass.getText();
            String newP = newPass.getText();
            String conf = confirmPass.getText();

            if (!PasswordUtil.verifyPassword(curr, currentUser.getPassword())) {
                msgLabel.setText("Current password is incorrect.");
                msgLabel.getStyleClass().setAll("error-label");
                return;
            }
            if (!ValidationUtils.isValidPassword(newP)) {
                msgLabel.setText("New password must be at least 4 characters.");
                msgLabel.getStyleClass().setAll("error-label");
                return;
            }
            if (!newP.equals(conf)) {
                msgLabel.setText("New passwords do not match.");
                msgLabel.getStyleClass().setAll("error-label");
                return;
            }

            String hashed = PasswordUtil.hashPassword(newP);
            currentUser.setPassword(hashed);

            if (userDAO.update(currentUser)) {
                msgLabel.setText("Password updated successfully!");
                msgLabel.getStyleClass().setAll("success-label");
                currentPass.clear();
                newPass.clear();
                confirmPass.clear();
                AnimationUtils.fadeIn(msgLabel, 200);
            } else {
                msgLabel.setText("Failed to update password.");
                msgLabel.getStyleClass().setAll("error-label");
            }
        });

        // Danger zone
        VBox dangerZone = new VBox(8);
        dangerZone.setPadding(new Insets(16, 0, 0, 0));

        Label dangerTitle = new Label("Danger Zone");
        dangerTitle.setStyle("-fx-text-fill: #F87171; -fx-font-weight: bold;");

        Button logoutBtn = new Button("Logout of Finora");
        logoutBtn.getStyleClass().add("btn-danger");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(ev -> mainController.getApp().showLoginScreen());

        dangerZone.getChildren().addAll(dangerTitle, logoutBtn);

        card.getChildren().addAll(sectionTitle, currentPass, newPass, confirmPass, updatePassBtn, msgLabel, dangerZone);
        return card;
    }
}
