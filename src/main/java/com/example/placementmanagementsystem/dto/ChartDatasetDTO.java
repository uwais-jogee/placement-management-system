package com.example.placementmanagementsystem.dto;

import java.util.List;

/**
 * Data Transfer Object for Chart Dataset JSON used by Chart.js to render charts
 */
public class ChartDatasetDTO {

    private String label;
    private List<Number> data;
    private String backgroundColor;
    private String borderColor;
    private boolean fill;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Number> getData() {
        return data;
    }

    public void setData(List<Number> data) {
        this.data = data;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    public boolean isFill() {
        return fill;
    }

    public void setFill(boolean fill) {
        this.fill = fill;
    }
}