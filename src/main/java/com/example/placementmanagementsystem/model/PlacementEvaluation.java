package com.example.placementmanagementsystem.model;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Placement Evaluation model, represents the evaluation of a completed placement by a student.
 * Maps to the placement evaluation form completed by the student at the end of their placement
 */
@Entity
public class PlacementEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
    private LocalDate evaluationDate;
    // Ratings
    private int trainingRating;
    private int supportRating;
    private int feedbackRating;
    private int industrySkillsRating;
    private int softSkillsRating;
    private int resourcesRating;
    private int workEnvironmentRating;
    private int recommendationRating;
    private int overallRating;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public LocalDate getEvaluationDate() {
        return evaluationDate;
    }

    public void setEvaluationDate(LocalDate evaluationDate) {
        this.evaluationDate = evaluationDate;
    }

    public int getTrainingRating() {
        return trainingRating;
    }

    public void setTrainingRating(int trainingRating) {
        this.trainingRating = trainingRating;
    }

    public int getSupportRating() {
        return supportRating;
    }

    public void setSupportRating(int supportRating) {
        this.supportRating = supportRating;
    }

    public int getFeedbackRating() {
        return feedbackRating;
    }

    public void setFeedbackRating(int feedbackRating) {
        this.feedbackRating = feedbackRating;
    }

    public int getIndustrySkillsRating() {
        return industrySkillsRating;
    }

    public void setIndustrySkillsRating(int industrySkillsRating) {
        this.industrySkillsRating = industrySkillsRating;
    }

    public int getSoftSkillsRating() {
        return softSkillsRating;
    }

    public void setSoftSkillsRating(int softSkillsRating) {
        this.softSkillsRating = softSkillsRating;
    }

    public int getResourcesRating() {
        return resourcesRating;
    }

    public void setResourcesRating(int resourcesRating) {
        this.resourcesRating = resourcesRating;
    }

    public int getWorkEnvironmentRating() {
        return workEnvironmentRating;
    }

    public void setWorkEnvironmentRating(int workEnvironmentRating) {
        this.workEnvironmentRating = workEnvironmentRating;
    }

    public int getRecommendationRating() {
        return recommendationRating;
    }

    public void setRecommendationRating(int recommendationRating) {
        this.recommendationRating = recommendationRating;
    }

    public int getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(int overallRating) {
        this.overallRating = overallRating;
    }
}