package com.example.placementmanagementsystem.repository;

import com.example.placementmanagementsystem.enumeration.PlacementAuthRequestStatus;
import com.example.placementmanagementsystem.model.EmailToken;
import com.example.placementmanagementsystem.model.PlacementAuthRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CRUD repository for PlacementAuthRequest entity
 */
@Repository
public interface PlacementAuthRequestRepo extends CrudRepository<PlacementAuthRequest, Long> {

    /**
     * Query to all PlacementAuthRequest by the given status
     *
     * @param placementAuthRequestStatus status enum of the PlacementAuthRequest
     * @return List of PlacementAuthRequest with the given status
     */
    List<PlacementAuthRequest> findAllByStatusIs(PlacementAuthRequestStatus placementAuthRequestStatus);

    /**
     * Query to find all PlacementAuthRequest by the given statuses, provided in a list
     *
     * @param statuses List of PlacementAuthRequestStatus enums to search by
     * @return List of PlacementAuthRequest with the given statuses
     */
    List<PlacementAuthRequest> findAllByStatusIn(List<PlacementAuthRequestStatus> statuses);

    /**
     * Query to find PlacementAuthRequest by the given emailToken
     *
     * @param emailToken EmailToken object to search by
     * @return PlacementAuthRequest that is assigned to the given emailToken
     */
    PlacementAuthRequest findByEmailToken(EmailToken emailToken);

    /**
     * Query to count all PlacementAuthRequest that have the given status
     *
     * @param placementAuthRequestStatus Status enum to search by
     * @return Count of PlacementAuthRequest with the given status
     */
    int countAllByStatusIs(PlacementAuthRequestStatus placementAuthRequestStatus);
}