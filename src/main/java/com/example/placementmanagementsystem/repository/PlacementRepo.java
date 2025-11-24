package com.example.placementmanagementsystem.repository;

import com.example.placementmanagementsystem.enumeration.PlacementStatus;
import com.example.placementmanagementsystem.model.Placement;
import com.example.placementmanagementsystem.model.Student;
import com.example.placementmanagementsystem.model.Tutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CRUD repository for Placement entity
 */
@Repository
public interface PlacementRepo extends CrudRepository<Placement, Long> {

    /**
     * Query to find all placements by a given status
     *
     * @param placementStatus Status enum to filter placements by
     * @return List of placements with the given status
     */
    List<Placement> findAllByStatusIs(PlacementStatus placementStatus);

    /**
     * Query to find all placements assigned to a given tutor
     *
     * @param tutor Tutor entity to filter placements by
     * @return List of placements assigned to the given tutor
     */
    List<Placement> findAllByTutor(Tutor tutor);

    /**
     * Query to find a placement by its unique id
     *
     * @param placementId Unique id of the placement to search for
     * @return Placement entity with the given id if found, otherwise null
     */
    Placement findPlacementById(Long placementId);

    /**
     * Query to find all placements assigned to a given tutor and have one of the given statuses
     *
     * @param tutor             Tutor entity to filter placements by
     * @param placementStatuses List of statuses to filter placements by
     * @return List of placements assigned to the given tutor and have one of the given statuses
     */
    List<Placement> findPlacementsByTutorAndStatusIn(Tutor tutor, List<PlacementStatus> placementStatuses);

    /**
     * Query to find all placements assigned to a given student and have one of the given statuses
     *
     * @param student           Student entity to filter placements by
     * @param placementStatuses List of statuses to filter placements by
     * @return List of placements assigned to the given student and have one of the given statuses
     */
    List<Placement> findPlacementsByStudentAndStatusIn(Student student, List<PlacementStatus> placementStatuses);

    /**
     * Query to count the number of placements that have the given status
     *
     * @param placementStatus Status enum to search placements by
     * @return Number of placements with the given status
     */
    int countAllByStatusIs(PlacementStatus placementStatus);

    /**
     * Custom query to find the date range of all placements
     *
     * @return an array of two objects, the first object is the minimum date and the second object is the maximum date
     */
    @Query(value = "SELECT MIN(p.startDate), MAX(p.startDate) FROM  Placement p")
    List<Object[]> findDateRange();

    /**
     * Custom query to find the number of placements grouped by month/year format - YYYY-MM
     *
     * @return A list of arrays, each array contains two objects, the first object is the month/year format and the second object is the count of placements, ordered by month/year
     */
    @Query(value = "SELECT FUNCTION('DATE_FORMAT', p.startDate, '%Y-%m') AS placementMonth, COUNT(p) AS placementCount FROM Placement p GROUP BY FUNCTION('DATE_FORMAT', p.startDate, '%Y-%m')")
    List<Object[]> findPlacementsCountGroupedByMonth();

    /**
     * Query to find a placement by its unique id
     *
     * @param id Unique id of the placement to search for
     * @return Placement entity with the given id if found, otherwise null
     */
    Optional<Placement> getPlacementByPlacementAuthRequest_Id(Long id);
}