package com.example.placementmanagementsystem.service;

import com.example.placementmanagementsystem.dto.ChartDataDTO;
import com.example.placementmanagementsystem.dto.ChartDatasetDTO;
import com.example.placementmanagementsystem.model.Placement;
import com.example.placementmanagementsystem.repository.PlacementEvaluationRepo;
import com.example.placementmanagementsystem.repository.PlacementRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * Service class for generating data for the Chart.js charts in frontend
 */
@Service
public class ChartService {

    @Autowired
    private PlacementRepo placementRepo;
    @Autowired
    private PlacementEvaluationRepo placementEvaluationRepo;

    /**
     * Get the chart data for the placements over time line chart.
     * Displayed on the Admin dashboard
     *
     * @return ChartDataDTO containing the data for the placements over time chart, in the correct format for Chart.js to render
     */
    public ChartDataDTO getPlacementsOverTime() {
        // Get the date range
        Object[] dateRange = placementRepo.findDateRange().get(0);

        // Check if the data is null, if so return an empty ChartDataDTO
        if (dateRange[0] == null || dateRange[1] == null) {
            return new ChartDataDTO();
        }

        LocalDate startDate = LocalDate.parse(dateRange[0].toString());
        LocalDate endDate = LocalDate.parse(dateRange[1].toString());

        // Fetch placements grouped by month
        List<Object[]> rawData = placementRepo.findPlacementsCountGroupedByMonth();
        // Map raw data into a quick lookup map
        Map<String, Long> rawPlacementData = new HashMap<>();
        for (Object[] row : rawData) {
            rawPlacementData.put((String) row[0], (Long) row[1]);
        }

        // Prepare labels and data for Chart.js
        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();

        YearMonth currentMonth = YearMonth.from(startDate);
        while (!currentMonth.isAfter(YearMonth.from(endDate))) {
            // Format month as "YYYY-MM"
            String monthKey = String.format("%d-%02d", currentMonth.getYear(), currentMonth.getMonthValue());

            // Add the month to labels and its corresponding count to data
            labels.add(monthKey);
            data.add(rawPlacementData.getOrDefault(monthKey, 0L).intValue());

            currentMonth = currentMonth.plusMonths(1);
        }

        // Create the dataset
        ChartDatasetDTO dataset = new ChartDatasetDTO();
        dataset.setLabel("Number of Placements Started");
        dataset.setData(data);
        dataset.setBackgroundColor("rgba(13, 148, 136, 0.1)"); // Teal-600 with 10% opacity
        dataset.setBorderColor("#0d9488"); // Teal-600
        dataset.setFill(true);

        // Create the ChartDataDTO
        ChartDataDTO chartData = new ChartDataDTO();
        chartData.setLabels(labels);
        chartData.setDatasets(List.of(dataset));
        return chartData;
    }

    /**
     * Get the chart data for the top companies bar chart, by the given feedback category.
     * Displayed on the Admin companies page
     *
     * @param category The feedback category to get the top companies for
     * @return ChartDataDTO containing the data for the top companies chart, in the correct format for Chart.js to render
     */
    public ChartDataDTO getTopCompaniesByFeedbackCategory(String category) {
        List<Object[]> rawData = placementEvaluationRepo.findTopCompaniesByFeedbackCategory(category);

        // Prepare labels and data for Chart.js
        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();

        for (Object[] row : rawData) {
            labels.add((String) row[0]);
            data.add(((Number) row[1]));
        }

        // Create the dataset
        ChartDatasetDTO dataset = new ChartDatasetDTO();
        // Format the name of the category for the dataset label, e.g. "overall_rating" -> "Average overall rating"
        dataset.setLabel("Average " + category.replace("_", " "));
        dataset.setData(data);

        // Create the ChartDataDTO
        ChartDataDTO chartData = new ChartDataDTO();
        chartData.setLabels(labels);
        chartData.setDatasets(List.of(dataset));
        return chartData;
    }

    /**
     * Get the chart data for the company average ratings chart for a given company.
     * Displayed on the Admin view company page
     *
     * @param companyId
     * @return
     */
    public ChartDataDTO getCompanyAverageRatings(Long companyId) {
        List<Object[]> rawData = placementEvaluationRepo.findCompanyAverageRatings(companyId);

        // Prepare labels and data for Chart.js
        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();

        // Add the average ratings and labels to the data and labels lists
        Object[] row = rawData.get(0);
        labels.add("Training");
        data.add((Number) row[0]);
        labels.add("Support");
        data.add((Number) row[1]);
        labels.add("Feedback");
        data.add((Number) row[2]);
        labels.add("Industry Skills");
        data.add((Number) row[3]);
        labels.add("Soft Skills");
        data.add((Number) row[4]);
        labels.add("Resources");
        data.add((Number) row[5]);
        labels.add("Work Environment");
        data.add((Number) row[6]);
        labels.add("Recommendation");
        data.add((Number) row[7]);

        // Create the dataset
        ChartDatasetDTO dataset = new ChartDatasetDTO();
        dataset.setLabel("Average Rating");
        dataset.setData(data);
        dataset.setBackgroundColor("rgba(13, 148, 136, 0.1)"); // Teal-600 with 10% opacity
        dataset.setBorderColor("#0d9488"); // Teal-600
        dataset.setFill(true);

        // Create the ChartDataDTO
        ChartDataDTO chartData = new ChartDataDTO();
        chartData.setLabels(labels);
        chartData.setDatasets(List.of(dataset));
        return chartData;
    }

    /**
     * Get the chart data for the placements by programme pie chart.
     * Displayed on the Admin placements page
     *
     * @param placements List of placements to get the data from
     * @return ChartDataDTO containing the data for the placements by programme chart, in the correct format for Chart.js to render
     */
    public ChartDataDTO getPlacementsByProgramme(List<Placement> placements) {
        // Count the number of placements for each programme using a HashMap
        Map<String, Integer> programmeCount = new HashMap<>();
        for (Placement placement : placements) {
            String programme = placement.getPlacementAuthRequest().getProgrammeOfStudy();
            programmeCount.put(programme, programmeCount.getOrDefault(programme, 0) + 1);
        }

        // Prepare labels and data for Chart.js
        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();

        // Add the programme names and counts to the labels and data lists
        for (Map.Entry<String, Integer> entry : programmeCount.entrySet()) {
            labels.add(entry.getKey());
            data.add(entry.getValue());
        }

        // Create the dataset
        ChartDatasetDTO dataset = new ChartDatasetDTO();
        dataset.setLabel("Number of Placements");
        dataset.setData(data);

        // Create the ChartDataDTO
        ChartDataDTO chartData = new ChartDataDTO();
        chartData.setLabels(labels);
        chartData.setDatasets(List.of(dataset));
        return chartData;
    }

    /**
     * Get the chart data for remote vs onsite placements doughnut chart.
     * Displayed on the Admin placements page
     *
     * @param placements List of placements to get the data from
     * @return ChartDataDTO containing the data for the remote vs onsite placements chart, in the correct format for Chart.js to render
     */
    public ChartDataDTO getRemoteVsOnsitePlacements(List<Placement> placements) {
        // Count the number of placements that are remote and onsite
        int remoteCount = 0;
        int onsiteCount = 0;

        for (Placement placement : placements) {
            if (placement.getPlacementAuthRequest().getRemote().equals("Yes")) {
                remoteCount++;
            } else {
                onsiteCount++;
            }
        }

        // Prepare labels and data for Chart.js
        List<String> labels = List.of("Remote", "Onsite");
        List<Number> data;
        if (remoteCount == 0 && onsiteCount == 0) {
            // If there are no placements, set the data to an empty list, so the front end does not display the chart
            data = new ArrayList<>();
        } else {
            data = List.of(remoteCount, onsiteCount);
        }

        // Create the dataset
        ChartDatasetDTO dataset = new ChartDatasetDTO();
        dataset.setLabel("Number of Placements");
        dataset.setData(data);

        // Create the ChartDataDTO
        ChartDataDTO chartData = new ChartDataDTO();
        chartData.setLabels(labels);
        chartData.setDatasets(List.of(dataset));
        return chartData;
    }

    /**
     * Get the chart data for the travel arrangements bar chart.
     *
     * @param placements List of placements to get the data from
     * @return ChartDataDTO containing the data for the travel arrangements chart, in the correct format for Chart.js to render
     */
    public ChartDataDTO getTravelArrangements(List<Placement> placements) {
        // Count the number of placements by travel arrangement type
        // Possible travel arrangements are: "Public Transport", "Walking", "Cycle", "Own Car", "Other"
        Map<String, Integer> travelArrangementCount = new HashMap<>();
        for (Placement placement : placements) {
            String travelArrangement = placement.getPlacementAuthRequest().getTravelArrangements();
            travelArrangementCount.put(travelArrangement, travelArrangementCount.getOrDefault(travelArrangement, 0) + 1);
        }

        // Prepare labels and data for Chart.js
        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();

        // Add the travel arrangement types and counts to the labels and data lists
        for (Map.Entry<String, Integer> entry : travelArrangementCount.entrySet()) {
            labels.add(entry.getKey());
            data.add(entry.getValue());
        }

        // Create the dataset
        ChartDatasetDTO dataset = new ChartDatasetDTO();
        dataset.setLabel("Number of Placements");
        dataset.setData(data);

        // Create the ChartDataDTO
        ChartDataDTO chartData = new ChartDataDTO();
        chartData.setLabels(labels);
        chartData.setDatasets(List.of(dataset));
        return chartData;
    }

    /**
     * Get the chart data for the residential arrangements bar chart.
     *
     * @param placements List of placements to get the data from
     * @return ChartDataDTO containing the data for the residential arrangements chart, in the correct format for Chart.js to render
     */
    public ChartDataDTO getResidentialArrangements(List<Placement> placements) {
        // Count the number of placements by residential arrangement type
        // Possible residential arrangements are: "Living at home", "Renting", "University Accommodation", "Other"
        Map<String, Integer> residentialArrangementCount = new HashMap<>();
        for (Placement placement : placements) {
            String residentialArrangement = placement.getPlacementAuthRequest().getResidentialArrangements();
            residentialArrangementCount.put(residentialArrangement, residentialArrangementCount.getOrDefault(residentialArrangement, 0) + 1);
        }

        // Prepare labels and data for Chart.js
        List<String> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();

        // Add the residential arrangement types and counts to the labels and data lists
        for (Map.Entry<String, Integer> entry : residentialArrangementCount.entrySet()) {
            labels.add(entry.getKey());
            data.add(entry.getValue());
        }

        // Create the dataset
        ChartDatasetDTO dataset = new ChartDatasetDTO();
        dataset.setLabel("Number of Placements");
        dataset.setData(data);

        // Create the ChartDataDTO
        ChartDataDTO chartData = new ChartDataDTO();
        chartData.setLabels(labels);
        chartData.setDatasets(List.of(dataset));
        return chartData;
    }
}