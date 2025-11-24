package com.example.placementmanagementsystem.repository;

import com.example.placementmanagementsystem.enumeration.VisitStatus;
import com.example.placementmanagementsystem.model.Visit;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CRUD repository for Visit entity
 */
@Repository
public interface VisitRepo extends CrudRepository<Visit, Long> {

    /**
     * Query to find Visit by the given unique visit id
     *
     * @param visitId The visit id to search for
     * @return The Visit object if found, null otherwise
     */
    Optional<Visit> getVisitById(Long visitId);

    /**
     * Query to find all visits with the given visit status
     *
     * @param visitStatus The visit status to filter by
     * @return List of visits with the given visit status
     */
    List<Visit> getVisitByStatus(VisitStatus visitStatus);
}