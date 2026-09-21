package com.smartfinance.api;

/**
 * Configuration for FinvisIQ API connectivity.
 * Manages API base URLs and distinguishes between Desktop Client mode and Server mode.
 */
public class ApiConfig {

    public static final String DEFAULT_PRODUCTION_API_URL = "https://finvisiq-backend-production.up.railway.app";
    public static final String DEFAULT_LOCAL_API_URL = "http://localhost:8085";

    private static String customBaseUrl = null;
    private static Boolean clientMode = false; // Server mode by default; App.java sets to true for desktop

    /**
     * Resolve the active API base URL.
     * Priority:
     * 1. Explicitly set custom URL in runtime
     * 2. Environment variable API_BASE_URL or FINVISIQ_API_URL
     * 3. System property api.base.url
     * 4. Production Railway URL
     */
    public static String getBaseUrl() {
        if (customBaseUrl != null && !customBaseUrl.isBlank()) {
            return cleanUrl(customBaseUrl);
        }

        String envUrl = System.getenv("API_BASE_URL");
        if (envUrl == null || envUrl.isBlank()) {
            envUrl = System.getenv("FINVISIQ_API_URL");
        }
        if (envUrl != null && !envUrl.isBlank()) {
            return cleanUrl(envUrl);
        }

        String sysProp = System.getProperty("api.base.url");
        if (sysProp != null && !sysProp.isBlank()) {
            return cleanUrl(sysProp);
        }

        return DEFAULT_PRODUCTION_API_URL;
    }

    public static void setBaseUrl(String url) {
        customBaseUrl = url;
    }

    public static boolean isClientMode() {
        String prop = System.getProperty("finvisiq.client.mode");
        if (prop != null) {
            return Boolean.parseBoolean(prop);
        }
        return clientMode != null && clientMode;
    }

    public static void setClientMode(boolean isClient) {
        clientMode = isClient;
    }

    private static String cleanUrl(String url) {
        if (url == null) return "";
        String trimmed = url.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
