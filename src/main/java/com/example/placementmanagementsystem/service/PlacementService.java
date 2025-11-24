package com.example.placementmanagementsystem.service;

import com.example.placementmanagementsystem.enumeration.PlacementAuthRequestStatus;
import com.example.placementmanagementsystem.enumeration.PlacementStatus;
import com.example.placementmanagementsystem.enumeration.VisitStatus;
import com.example.placementmanagementsystem.model.*;
import com.example.placementmanagementsystem.repository.PlacementRepo;
import com.example.placementmanagementsystem.repository.TutorRepo;
import com.example.placementmanagementsystem.repository.VisitRepo;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for placement related operations
 */
@Service
public class PlacementService {

    @Autowired
    private PlacementRepo placementRepo;
    @Autowired
    private VisitRepo visitRepo;
    @Autowired
    private TutorRepo tutorRepo;
    @Autowired
    private NotificationService notificationService;

    /**
     * Check if a student is on an in progress placement
     *
     * @param student The student to check
     * @return True if the student is on an in progress placement, false otherwise
     */
    public boolean isStudentOnPlacement(Student student) {
        boolean onPlacement = false;
        List<Placement> studentPlacements = student.getPlacements();
        for (Placement placement : studentPlacements) {
            if (placement.getStatus() == PlacementStatus.IN_PROGRESS) {
                onPlacement = true;
                break;
            }
        }
        return onPlacement;
    }

    /**
     * Check if a student has an upcoming placement
     *
     * @param student The student to check
     * @return True if the student has an upcoming placement, false otherwise
     */
    public boolean isStudentPlacementUpcoming(Student student) {
        boolean upcomingPlacement = false;
        List<Placement> studentPlacements = student.getPlacements();
        for (Placement placement : studentPlacements) {
            if (placement.getStatus() == PlacementStatus.UPCOMING) {
                upcomingPlacement = true;
                break;
            }
        }
        return upcomingPlacement;
    }

    /**
     * Get a student's current placement, if they are on one
     *
     * @param student The student to get the placement for
     * @return The student's current placement, or null if they are not on a placement
     */
    public Placement getStudentCurrentPlacement(Student student) {
        List<Placement> studentPlacements = student.getPlacements();
        for (Placement placement : studentPlacements) {
            if (placement.getStatus() == PlacementStatus.IN_PROGRESS || placement.getStatus() == PlacementStatus.UPCOMING) {
                return placement;
            }
        }
        return null;
    }

    /**
     * Check if a student has a pending placement authorisation request
     *
     * @param student The student to check
     * @return True if the student has a pending placement authorisation request, false otherwise
     */
    public boolean hasStudentPendingAuthRequest(Student student) {
        List<PlacementAuthRequest> placementAuthRequests = student.getPlacementAuthRequests();
        for (PlacementAuthRequest authRequest : placementAuthRequests) {
            if (authRequest.getStatus() == PlacementAuthRequestStatus.PENDING_INITIAL_ADMIN_APPROVAL || authRequest.getStatus() == PlacementAuthRequestStatus.PENDING_COMPANY_APPROVAL || authRequest.getStatus() == PlacementAuthRequestStatus.PENDING_FINAL_ADMIN_APPROVAL) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the tutor that a student on placement is assigned to
     *
     * @param student The student to search by
     * @return The tutor assigned to the student's placement, or null if the student is not on placement
     */
    public Tutor getStudentPlacementTutor(Student student) {
        if (isStudentOnPlacement(student) || isStudentPlacementUpcoming(student)) {
            Placement currentPlacement = getStudentCurrentPlacement(student);
            return currentPlacement.getTutor();
        }
        return null;
    }


    /**
     * Update the status of all placements in the database, calling the updatePlacementStatus method on each placement.
     * This method is scheduled via the ApplicationScheduler class.
     */
    public void updateAllPlacementStatuses() {
        // Iterate through all placements
        for (Placement placement : placementRepo.findAll()) {
            updatePlacementStatus(placement);
        }
    }

    /**
     * Update a placement's status from upcoming to in progress, and from in progress to pending student evaluation based on the current date.
     *
     * @param placement The placement to update the status for
     */
    public void updatePlacementStatus(Placement placement) {
        // Check if the placement start date is today or in the past, and the status is upcoming
        if ((placement.getStartDate().isEqual(LocalDate.now()) || placement.getStartDate().isBefore(LocalDate.now())) && placement.getStatus() == PlacementStatus.UPCOMING) {
            // Update the placement status to in progress
            placement.setStatus(PlacementStatus.IN_PROGRESS);
            placementRepo.save(placement);
        }
        // Check if the placement end date is in the past, and the status is in progress
        if (placement.getEndDate().isBefore(LocalDate.now()) && placement.getStatus() == PlacementStatus.IN_PROGRESS) {
            // If the placement has ended, update the status to pending student evaluation
            placement.setStatus(PlacementStatus.PENDING_STUDENT_EVALUATION);

            // Set any unread messages to read
            for (Message message : placement.getMessageChat()) {
                if (!message.isRead()) {
                    message.setRead(true);
                }
            }

            // Remove the tutor assigned to the placement, on both sides of the relationship
            Tutor tutor = placement.getTutor();
            tutor.getPlacements().remove(placement);
            placement.setTutor(null);
            placementRepo.save(placement);
            tutorRepo.save(tutor);

            // Send a notification to the tutor to inform them that the placement has ended
            notificationService.createTutorNotification(tutor, "Placement Ended", "The placement for student " + placement.getStudent().getFirstName() + " " + placement.getStudent().getLastName() + " at " + placement.getCompany().getCompanyName() + " has ended on " + placement.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yy")) + ".", "", "");
        }
    }

    /**
     * Get all visits for a tutor, from all placements the tutor is overseeing
     *
     * @param tutor The tutor to get the visits for
     * @return A list of visits belonging to the tutor
     */
    public List<Visit> getTutorVisits(Tutor tutor) {
        // Iterate through the placements the tutor is overseeing, and get the visits for each placement
        List<Visit> visits = new ArrayList<>();
        for (Placement placement : placementRepo.findAllByTutor(tutor)) {
            visits.addAll(placement.getVisits());
        }
        // Sort the visits by date, newest first, as defined in the Visit class implementation of the Comparable interface
        visits.sort(null);
        return visits;
    }

    /**
     * Updates the status of all visits in the database to completed based on if the visit date and end time have passed.
     * This method is scheduled via the ApplicationScheduler class.
     */
    public void updateVisitStatuses() {
        // Iterate through all visits that are Upcoming
        List<Visit> upcomingVisits = visitRepo.getVisitByStatus(VisitStatus.UPCOMING);
        for (Visit visit : upcomingVisits) {
            // Check if the visit date and end time have passed
            LocalDateTime visitEndDateTime = visit.getDate().atTime(visit.getEndTime());
            if (LocalDateTime.now().isAfter(visitEndDateTime)) {
                // Update the visit status to completed
                visit.setStatus(VisitStatus.COMPLETED);
                visitRepo.save(visit);
            }
        }
    }

    /**
     * Get the student associated with a placement, given the placement ID
     *
     * @param placementId The ID of the placement
     * @return The student associated with the placement, or null if the placement does not exist
     */
    public Student getStudentByPlacementId(Long placementId) {
        Optional<Placement> placementOptional = placementRepo.findById(placementId);
        if (placementOptional.isPresent()) {
            Placement placement = placementOptional.get();
            return placement.getStudent();
        }
        return null;
    }

    /**
     * Get a JSON representation of the addresses of placements, to be used in the Google Maps API to display markers
     *
     * @param placements A list of placements to get the addresses for
     * @return A JSON representation of the placement company addresses in JSON map format
     */
    public String getPlacementAddressesJSON(List<Placement> placements) {
        List<Map<String, String>> addresses = new ArrayList<>();
        for (Placement placement : placements) {
            Map<String, String> address = new java.util.HashMap<>();
            address.put("placementId", placement.getId().toString());
            address.put("studentName", placement.getStudent().getFirstName() + " " + placement.getStudent().getLastName());
            address.put("companyName", placement.getCompany().getCompanyName());
            address.put("formattedAddress", placement.getCompany().getAddress().getFormattedAddress());
            address.put("placeId", placement.getCompany().getAddress().getPlaceId());
            addresses.add(address);
        }
        // Convert the addresses to JSON
        Gson gson = new Gson();
        return gson.toJson(addresses);
    }

    /**
     * Get a JSON representation of the addresses of companies, to be used in the Google Maps API to display markers
     *
     * @param companies A list of companies to get the addresses for
     * @return A JSON representation of the company addresses in JSON map format
     */
    public String getCompanyAddressesJSON(List<Company> companies) {
        List<Map<String, String>> addresses = new ArrayList<>();
        for (Company company : companies) {
            Map<String, String> address = new java.util.HashMap<>();
            address.put("companyName", company.getCompanyName());
            address.put("formattedAddress", company.getAddress().getFormattedAddress());
            address.put("placeId", company.getAddress().getPlaceId());
            addresses.add(address);
        }
        // Convert the addresses to JSON
        Gson gson = new Gson();
        return gson.toJson(addresses);
    }

    /**
     * Check if a student has a placement pending evaluation
     *
     * @param currentStudent The student to check
     * @return True if the student has a placement pending evaluation, false otherwise
     */
    public boolean hasStudentPendingEvaluation(Student currentStudent) {
        List<Placement> studentPlacements = currentStudent.getPlacements();
        for (Placement placement : studentPlacements) {
            if (placement.getStatus() == PlacementStatus.PENDING_STUDENT_EVALUATION) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the placement pending evaluation for a student
     *
     * @param currentStudent The student to get the placement for
     * @return The placement pending evaluation, or null if the student does not have one
     */
    public Placement getPlacementPendingEvaluationByStudent(Student currentStudent) {
        List<Placement> studentPlacements = currentStudent.getPlacements();
        for (Placement placement : studentPlacements) {
            if (placement.getStatus() == PlacementStatus.PENDING_STUDENT_EVALUATION) {
                return placement;
            }
        }
        return null;
    }
}