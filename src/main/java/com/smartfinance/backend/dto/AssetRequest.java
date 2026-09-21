package com.smartfinance.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AssetRequest {
    @NotBlank(message = "Asset name is required")
    private String name;

    @NotBlank(message = "Asset type is required")
    private String type;

    @NotNull(message = "Asset value is required")
    @DecimalMin(value = "0.0", message = "Value cannot be negative")
    private Double value;

    private String notes;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
