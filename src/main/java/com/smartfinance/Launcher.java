package com.smartfinance;

/**
 * Launcher shim — Required for running JavaFX on the classpath (non-modular) with JDK 21+.
 * When JavaFX JARs are on the classpath instead of the module-path, the JVM requires
 * that the main class is NOT a subclass of javafx.application.Application.
 * This class delegates to App.main() to satisfy that requirement.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
