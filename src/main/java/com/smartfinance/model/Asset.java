package com.smartfinance.model;

/**
 * Asset Model — Represents user physical and financial assets (Real Estate, Cash, Gold, Stocks, Vehicles, etc.).
 */
public class Asset {
    private int assetId;
    private int userId;
    private String name;
    private String type; // Real Estate, Cash/Bank, Gold, Stocks, Crypto, Vehicle, Other
    private double value;
    private String notes;

    public Asset() {}

    public Asset(int userId, String name, String type, double value, String notes) {
        this.userId = userId;
        this.name = name;
        this.type = type != null ? type : "Other";
        this.value = value;
        this.notes = notes;
    }

    public Asset(int assetId, int userId, String name, String type, double value, String notes) {
        this.assetId = assetId;
        this.userId = userId;
        this.name = name;
        this.type = type != null ? type : "Other";
        this.value = value;
        this.notes = notes;
    }

    // Getters and Setters
    public int getAssetId() { return assetId; }
    public void setAssetId(int assetId) { this.assetId = assetId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
