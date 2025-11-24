package com.example.placementmanagementsystem.repository;

import com.example.placementmanagementsystem.model.PlacementEvaluation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CRUD repository for PlacementEvaluation entity
 */
@Repository
public interface PlacementEvaluationRepo extends CrudRepository<PlacementEvaluation, Long> {

    /**
     * Custom query to find the top 10 companies by a specific feedback category
     *
     * @param category The feedback category to filter by
     * @return A list of arrays, each array contains two objects, the first object is the company name and the second object is the average rating for the feedback category.
     * Each array is ordered by the average rating in descending order.
     */
    @Query("SELECT c.companyName, AVG(CASE WHEN :category = 'training_rating' THEN p.trainingRating WHEN :category = 'support_rating' THEN p.supportRating WHEN :category = 'feedback_rating' THEN p.feedbackRating WHEN :category = 'industry_skills_rating' THEN p.industrySkillsRating WHEN :category = 'soft_skills_rating' THEN p.softSkillsRating WHEN :category = 'resources_rating' THEN p.resourcesRating WHEN :category = 'work_environment_rating' THEN p.workEnvironmentRating WHEN :category = 'recommendation_rating' THEN p.recommendationRating WHEN :category = 'overall_rating' THEN p.overallRating ELSE NULL END) AS averageRating FROM PlacementEvaluation p JOIN p.company c GROUP BY c.companyName ORDER BY averageRating DESC LIMIT 10")
    List<Object[]> findTopCompaniesByFeedbackCategory(@Param("category") String category);


    /**
     * Custom query to get the given company's average ratings for each evaluation category
     *
     * @param companyId The ID of the company to be queried
     * @return A list of arrays, each array contains the average rating for each evaluation category in the order specified in the query.
     */
    @Query("SELECT AVG(p.trainingRating) AS avgTrainingRating, AVG(p.supportRating) AS avgSupportRating, AVG(p.feedbackRating) AS avgFeedbackRating, AVG(p.industrySkillsRating) AS avgIndustrySkillsRating, AVG(p.softSkillsRating) AS avgSoftSkillsRating, AVG(p.resourcesRating) AS avgResourcesRating, AVG(p.workEnvironmentRating) AS avgWorkEnvironmentRating, AVG(p.recommendationRating) AS avgRecommendationRating, AVG(p.overallRating) AS avgOverallRating FROM PlacementEvaluation p WHERE p.company.id = :companyId")
    List<Object[]> findCompanyAverageRatings(@Param("companyId") Long companyId);
}