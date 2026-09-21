package com.smartfinance;

import com.smartfinance.controller.LoginController;
import com.smartfinance.controller.MainController;
import com.smartfinance.model.User;
import com.smartfinance.util.ThemeManager;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * FinvisIQ — Personal Finance Intelligence Platform
 * Main Application Entry Point.
 */
public class App extends Application {
    private Stage primaryStage;
    private ThemeManager themeManager;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // Initialize theme manager
        themeManager = ThemeManager.getInstance();

        // Configure stage
        stage.setTitle("FinvisIQ — Personal Finance Intelligence Platform");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setWidth(1280);
        stage.setHeight(800);
        stage.centerOnScreen();

        // Initialize desktop in secure REST API client mode (No direct JDBC or DB passwords needed)
        com.smartfinance.api.ApiConfig.setClientMode(true);
        com.smartfinance.api.ApiClient.getInstance().init();

        // Show login screen directly
        showLoginScreen();

        stage.show();
    }

    /** Show the login screen. */
    public void showLoginScreen() {
        LoginController loginController = new LoginController(this);
        Scene scene = new Scene(loginController.createView(), 1280, 800);
        themeManager.setScene(scene);
        primaryStage.setScene(scene);
    }

    /** Show the main application after successful login. */
    public void showMainApp(User user) {
        MainController mainController = new MainController(this, user);
        Scene scene = new Scene(mainController.createView(), 1280, 800);
        themeManager.setScene(scene);
        primaryStage.setScene(scene);
    }

    /** Get the primary stage. */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /** Get the theme manager. */
    public ThemeManager getThemeManager() {
        return themeManager;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
