package com.example.placementmanagementsystem.dto;

import java.util.List;

/**
 * Data Transfer Object for Chart Data displayed by Chart JS
 */
public class ChartDataDTO {

    private List<ChartDatasetDTO> datasets;
    private List<String> labels;

    public List<ChartDatasetDTO> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<ChartDatasetDTO> datasets) {
        this.datasets = datasets;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }
}