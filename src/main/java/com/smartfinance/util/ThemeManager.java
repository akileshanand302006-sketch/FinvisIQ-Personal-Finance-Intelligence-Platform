package com.smartfinance.util;

import javafx.scene.Scene;


/**
 * Theme Manager - Handles light/dark mode switching.
 * Demonstrates: Encapsulation, Abstraction.
 */
public class ThemeManager {
    private static ThemeManager instance;
    private boolean darkMode = false;
    private Scene currentScene;

    private static final String LIGHT_THEME = "/css/light-theme.css";
    private static final String DARK_THEME = "/css/dark-theme.css";

    private ThemeManager() {}

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    public void setScene(Scene scene) {
        this.currentScene = scene;
        applyTheme();
    }

    public void toggleTheme() {
        darkMode = !darkMode;
        applyTheme();
    }

    public void setDarkMode(boolean dark) {
        this.darkMode = dark;
        applyTheme();
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    private void applyTheme() {
        if (currentScene == null) return;

        currentScene.getStylesheets().clear();
        String themePath = darkMode ? DARK_THEME : LIGHT_THEME;
        String css = getClass().getResource(themePath) != null ?
            getClass().getResource(themePath).toExternalForm() : null;

        if (css != null) {
            currentScene.getStylesheets().add(css);
        }
    }

    /** Get current theme CSS path. */
    public String getCurrentThemePath() {
        return darkMode ? DARK_THEME : LIGHT_THEME;
    }

    /** Get the theme toggle icon text. */
    public String getThemeIcon() {
        return darkMode ? "\u2600\uFE0F" : "\uD83C\uDF19";
    }

    /** Get the theme name. */
    public String getThemeName() {
        return darkMode ? "Dark Mode" : "Light Mode";
    }
}
