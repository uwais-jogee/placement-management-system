package com.example.placementmanagementsystem.controller;

import com.example.placementmanagementsystem.dto.ChartDataDTO;
import com.example.placementmanagementsystem.dto.PlacementDTO;
import com.example.placementmanagementsystem.enumeration.PlacementAuthRequestStatus;
import com.example.placementmanagementsystem.enumeration.PlacementStatus;
import com.example.placementmanagementsystem.model.*;
import com.example.placementmanagementsystem.repository.*;
import com.example.placementmanagementsystem.service.*;
import com.google.gson.Gson;
import com.mailersend.sdk.exceptions.MailerSendException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller class for handling admin specific requests
 */
@Controller
public class AdminController {

    @Autowired
    private PlacementRepo placementRepo;
    @Autowired
    private PlacementAuthRequestRepo placementAuthRequestRepo;
    @Autowired
    private EmailTokenRepo emailTokenRepo;
    @Autowired
    private TutorRepo tutorRepo;
    @Autowired
    private CompanyRepo companyRepo;
    @Autowired
    private AddressRepo addressRepo;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private PlacementService placementService;
    @Autowired
    private ChartService chartService;
    @Autowired
    private EmailService emailService;
    @Value("${HOST}")
    private String host;
    @Autowired
    private UserService userService;

    /**
     * Admin dashboard endpoint
     *
     * @param model The model to add attributes to
     * @return The admin dashboard view
     */
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        // Get the data for the info cards
        int pendingInitialAdminApproval = placementAuthRequestRepo.countAllByStatusIs(PlacementAuthRequestStatus.PENDING_INITIAL_ADMIN_APPROVAL);
        int pendingCompanyApproval = placementAuthRequestRepo.countAllByStatusIs(PlacementAuthRequestStatus.PENDING_COMPANY_APPROVAL);
        int pendingFinalAdminApproval = placementAuthRequestRepo.countAllByStatusIs(PlacementAuthRequestStatus.PENDING_FINAL_ADMIN_APPROVAL);
        int inProgressPlacements = placementRepo.countAllByStatusIs(PlacementStatus.IN_PROGRESS);
        model.addAttribute("pendingInitialAdminApproval", pendingInitialAdminApproval);
        model.addAttribute("pendingCompanyApproval", pendingCompanyApproval);
        model.addAttribute("pendingFinalAdminApproval", pendingFinalAdminApproval);
        model.addAttribute("inProgressPlacements", inProgressPlacements);

        // Get the placement address data for the map
        List<Placement> placements = placementRepo.findAllByStatusIs(PlacementStatus.IN_PROGRESS);
        model.addAttribute("placementAddresses", placementService.getPlacementAddressesJSON(placements));

        // Get the chart data for the placements over time
        ChartDataDTO chartData = chartService.getPlacementsOverTime();
        Gson gson = new Gson();
        String chartDataJson = gson.toJson(chartData);
        model.addAttribute("chartData", chartDataJson);

        return "admin/dashboard";
    }

    /**
     * Admin profile page endpoint
     *
     * @return The admin profile view
     */
    @GetMapping("/admin/profile")
    public String profile() {
        return "admin/profile";
    }

    /**
     * Admin submit change password endpoint
     *
     * @param currentPassword    The current password provided by the user
     * @param newPassword        The new password provided by the user
     * @param repeatNewPassword  The repeated new password provided by the user
     * @param redirectAttributes The redirect attributes to add flash attributes to
     * @return A redirect to the admin profile page with a success message if the password was changed successfully, or an error message if the current password was incorrect
     */
    @PostMapping("/admin/profile/change-password")
    public String changePassword(@RequestParam String currentPassword, @RequestParam String newPassword, @RequestParam String repeatNewPassword, RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentLoggedInUser();
        // Check the new password and repeat new password match
        if (!newPassword.equals(repeatNewPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to change password. New password and repeat new password do not match.");
            return "redirect:/admin/profile";
        }
        if (userService.changeUserPassword(currentUser, currentPassword, newPassword)) {
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to change password. Incorrect current password.");
        }
        return "redirect:/admin/profile";
    }

    /**
     * View all placement authorisation requests endpoint
     *
     * @param status The status of the placement authorisation requests to filter by on the view
     * @param model  The model to add attributes to
     * @return The placement authorisation requests view
     */
    @GetMapping("/admin/auth-requests/{status}")
    public String authRequests(@PathVariable String status, Model model) {
        switch (status.toLowerCase()) {
            case "pending-initial-admin-approval":
                List<PlacementAuthRequest> authRequestsInitialAdmin = placementAuthRequestRepo.findAllByStatusIs(PlacementAuthRequestStatus.PENDING_INITIAL_ADMIN_APPROVAL);
                Collections.reverse(authRequestsInitialAdmin);  // Reverse the list in place to show the most recent requests first
                model.addAttribute("authRequests", authRequestsInitialAdmin);
                model.addAttribute("tab", "Pending Initial Admin Approval");
                break;
            case "pending-company-approval":
                List<PlacementAuthRequest> authRequestsCompany = placementAuthRequestRepo.findAllByStatusIs(PlacementAuthRequestStatus.PENDING_COMPANY_APPROVAL);
                Collections.reverse(authRequestsCompany);  // Reverse the list in place to show the most recent requests first
                model.addAttribute("authRequests", authRequestsCompany);
                model.addAttribute("tab", "Pending Company Approval");
                break;
            case "pending-final-admin-approval":
                List<PlacementAuthRequest> authRequestsFinalAdmin = placementAuthRequestRepo.findAllByStatusIs(PlacementAuthRequestStatus.PENDING_FINAL_ADMIN_APPROVAL);
                Collections.reverse(authRequestsFinalAdmin);  // Reverse the list in place to show the most recent requests first
                model.addAttribute("authRequests", authRequestsFinalAdmin);
                model.addAttribute("tab", "Pending Final Admin Approval");
                break;
            case "approved":
                List<PlacementAuthRequest> authRequestsApproved = placementAuthRequestRepo.findAllByStatusIs(PlacementAuthRequestStatus.APPROVED);
                Collections.reverse(authRequestsApproved);  // Reverse the list in place to show the most recent requests first
                model.addAttribute("authRequests", authRequestsApproved);
                model.addAttribute("tab", "Approved");
                break;
            case "rejected":
                List<PlacementAuthRequest> authRequestsRejected = placementAuthRequestRepo.findAllByStatusIn(Arrays.asList(PlacementAuthRequestStatus.REJECTED_INITIAL_BY_ADMIN, PlacementAuthRequestStatus.REJECTED_BY_COMPANY, PlacementAuthRequestStatus.REJECTED_FINAL_BY_ADMIN));
                Collections.reverse(authRequestsRejected);  // Reverse the list in place to show the most recent requests first
                model.addAttribute("authRequests", authRequestsRejected);
                model.addAttribute("tab", "Rejected");
                break;
            case "all":
                List<PlacementAuthRequest> authRequestsAll = (List<PlacementAuthRequest>) placementAuthRequestRepo.findAll();
                Collections.reverse(authRequestsAll);  // Reverse the list in place to show the most recent requests first
                model.addAttribute("authRequests", authRequestsAll);
                model.addAttribute("tab", "All");
                break;
            default:
                // If the status is invalid, return a Bad Request response
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to view auth requests - Invalid status");
        }
        return "admin/authRequests";
    }

    /**
     * Admin view details of a specific placement authorisation request endpoint
     *
     * @param id    The ID of the placement authorisation request to view
     * @param model The model to add attributes to
     * @return The view placement authorisation request view
     */
    @GetMapping("/admin/auth-request/view")
    public String viewAuthRequest(@RequestParam Long id, Model model) {
        Optional<PlacementAuthRequest> authRequest = placementAuthRequestRepo.findById(id);
        if (authRequest.isPresent()) {
            model.addAttribute("authRequest", authRequest.get());
            // Add the tutors to the model for the final approval
            if (authRequest.get().getStatus() == PlacementAuthRequestStatus.PENDING_FINAL_ADMIN_APPROVAL) {
                model.addAttribute("tutors", tutorRepo.findAll());
            }
            // Add the placement id to the model if approved
            if (authRequest.get().getStatus() == PlacementAuthRequestStatus.APPROVED) {
                Optional<Placement> placementOptional = placementRepo.getPlacementByPlacementAuthRequest_Id(id);
                if (placementOptional.isPresent()) {
                    model.addAttribute("placementId", placementOptional.get().getId());
                }
            }
            return "admin/viewAuthRequest";
        } else {
            throw new EntityNotFoundException("Placement authorisation request not found");
        }
    }

    /**
     * Endpoint for the admin to reject a placement authorisation request
     *
     * @param id                 The ID of the placement authorisation request to reject
     * @param rejectionReason    The reason for rejecting the request
     * @param redirectAttributes The redirect attributes to add a flash attribute to
     * @return Redirect to the view all placement authorisation request page
     */
    @PostMapping("/admin/auth-request/reject")
    public String rejectAuthRequest(@RequestParam Long id, @RequestParam String rejectionReason, RedirectAttributes redirectAttributes) {
        Optional<PlacementAuthRequest> authRequest = placementAuthRequestRepo.findById(id);
        if (authRequest.isPresent()) {
            PlacementAuthRequest placementAuthRequest = authRequest.get();
            // Check the current stage and set the rejection stage accordingly
            if (placementAuthRequest.getStatus() == PlacementAuthRequestStatus.PENDING_INITIAL_ADMIN_APPROVAL) {
                placementAuthRequest.setStatus(PlacementAuthRequestStatus.REJECTED_INITIAL_BY_ADMIN);
            } else if (placementAuthRequest.getStatus() == PlacementAuthRequestStatus.PENDING_FINAL_ADMIN_APPROVAL) {
                placementAuthRequest.setStatus(PlacementAuthRequestStatus.REJECTED_FINAL_BY_ADMIN);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Placement authorisation request is not in a state to be rejected");
            }
            // Set the rejection reason
            placementAuthRequest.setRejectionReason(rejectionReason);
            placementAuthRequestRepo.save(placementAuthRequest);

            // Create a notification for the student
            notificationService.createStudentNotification(placementAuthRequest.getStudent(), "Placement Authorisation Request Rejected", "Your placement authorisation request #" + id + " has been rejected.", "/student/auth-request/view?id=" + id, "View Request");

            // Add a success message as a flash attribute
            redirectAttributes.addFlashAttribute("successMessage", "Placement authorisation request #" + id + " rejected successfully.");

            return "redirect:/admin/auth-request/view?id=" + id;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Placement authorisation request not found");
        }
    }

    /**
     * Endpoint for the admin to initially approve a placement authorisation request
     *
     * @param id                 The ID of the placement authorisation request to initially approve
     * @param redirectAttributes The redirect attributes to add a flash attribute to
     * @return Redirect to the view all placement authorisation request page
     */
    @PostMapping("/admin/auth-request/initial-approval")
    public String initialApproval(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        Optional<PlacementAuthRequest> authRequest = placementAuthRequestRepo.findById(id);
        // If the auth request is found, set the status to PENDING_COMPANY_APPROVAL
        if (authRequest.isPresent()) {
            PlacementAuthRequest placementAuthRequest = authRequest.get();
            if (placementAuthRequest.getStatus() == PlacementAuthRequestStatus.PENDING_INITIAL_ADMIN_APPROVAL) {
                // Email the company contact to request placement approval
                try {
                    String companyName;
                    if (placementAuthRequest.getCompany() == null) {
                        // If the student proposes a company that does not exist in the database
                        companyName = placementAuthRequest.getCompanyNameOther();
                    } else {
                        // If the company exists in the database
                        companyName = placementAuthRequest.getCompany().getCompanyName();
                    }

                    // Create a new token for the email link and assign it to the auth request
                    EmailToken emailToken = new EmailToken(7200);
                    emailTokenRepo.save(emailToken);
                    placementAuthRequest.setEmailToken(emailToken);
                    placementAuthRequestRepo.save(placementAuthRequest);

                    String toEmail = placementAuthRequest.getCompanyContactEmail();
                    String studentName = placementAuthRequest.getStudent().getFirstName() + " " + placementAuthRequest.getStudent().getLastName();
                    String toName = placementAuthRequest.getCompanyContactName();
                    String link = host + "/company/auth-request/view?token=" + placementAuthRequest.getEmailToken().getToken();
                    String validTill = placementAuthRequest.getEmailToken().getExpiresAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm"));

                    // Send the email
                    emailService.sendEmailToCompany(toEmail, toName, link, validTill, companyName, studentName);

                    // Update the status of the placement auth request
                    placementAuthRequest.setStatus(PlacementAuthRequestStatus.PENDING_COMPANY_APPROVAL);
                    placementAuthRequestRepo.save(placementAuthRequest);
                } catch (MailerSendException mse) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email, error from MailerSend API: " + mse.getMessage());
                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email: " + e.getMessage());
                }

                // Add a success message as a flash attribute
                redirectAttributes.addFlashAttribute("successMessage", "Placement authorisation request #" + id + " approved successfully. Company approval requested.");

                return "redirect:/admin/auth-request/view?id=" + id;
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Placement authorisation request is not in a state to be initially approved");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Placement authorisation request not found");
        }
    }

    /**
     * Endpoint for the admin to finally approve a placement authorisation request
     *
     * @param id                 The ID of the placement authorisation request to finally approve
     * @param tutorUsername      The username of the tutor to assign the placement to
     * @param redirectAttributes The redirect attributes to add a flash attribute to
     * @return Redirect to the view all placement authorisation request page
     */
    @PostMapping("/admin/auth-request/final-approval")
    public String finalApproval(@RequestParam Long id, @RequestParam String tutorUsername, RedirectAttributes redirectAttributes) {
        Optional<PlacementAuthRequest> authRequest = placementAuthRequestRepo.findById(id);
        Tutor tutor = tutorRepo.findTutorByUsername(tutorUsername);
        if (authRequest.isPresent() && tutor != null) {
            PlacementAuthRequest placementAuthRequest = authRequest.get();
            if (placementAuthRequest.getStatus() == PlacementAuthRequestStatus.PENDING_FINAL_ADMIN_APPROVAL) {

                Student student = placementAuthRequest.getStudent();

                // Approve the request after checking the parameters
                placementAuthRequest.setStatus(PlacementAuthRequestStatus.APPROVED);
                placementAuthRequestRepo.save(placementAuthRequest);

                // Create the new placement object
                Placement placement = new Placement();
                // Set the placement status based on the start date
                if (placementAuthRequest.getPlacementStartDate().isBefore(LocalDate.now()) || placementAuthRequest.getPlacementStartDate().isEqual(LocalDate.now())) {
                    placement.setStatus(PlacementStatus.IN_PROGRESS); // If the start date is today or before today then the placement is in progress already
                } else {
                    placement.setStatus(PlacementStatus.UPCOMING);
                }
                placement.setStudent(placementAuthRequest.getStudent());
                student.getPlacements().add(placement);
                placement.setTutor(tutor);
                tutor.getPlacements().add(placement);
                placement.setPlacementAuthRequest(placementAuthRequest);
                placement.setStartDate(placementAuthRequest.getPlacementStartDate());
                placement.setEndDate(placementAuthRequest.getPlacementEndDate());

                if (placementAuthRequest.getCompany() == null) {
                    // If the student proposes a company that does not exist in the database
                    Company company = new Company();
                    company.setCompanyName(placementAuthRequest.getCompanyNameOther());
                    Address companyAddress = new Address(placementAuthRequest.getCompanyPlaceId(), placementAuthRequest.getCompanyFormattedAddress());
                    addressRepo.save(companyAddress);
                    company.setAddress(companyAddress);
                    company.setIndustry(placementAuthRequest.getCompanyIndustry());
                    company.setWebAddress(placementAuthRequest.getCompanyWebAddress());
                    companyRepo.save(company);
                    placement.setCompany(company);
                    company.getPlacements().add(placement);
                } else {
                    // If the company already exists in the database
                    Company company = placementAuthRequest.getCompany();
                    placement.setCompany(company);
                    company.getPlacements().add(placement);
                }
                placementRepo.save(placement);

                // Create a notification for the student and the tutor
                notificationService.createStudentNotification(placementAuthRequest.getStudent(), "Placement Authorisation Request Approved", "Your placement authorisation request #" + id + " has been approved.", "/student/auth-request/view?id=" + id, "View Request");
                notificationService.createTutorNotification(tutor, "New Placement Assigned", "You have been assigned as the tutor for " + student.getFirstName() + " " + student.getLastName() + " at " + placement.getCompany().getCompanyName() + ".", "/tutor/placement/view?id=" + placement.getId(), "View Placement");

                // Add a success message as a flash attribute
                redirectAttributes.addFlashAttribute("successMessage", "Placement authorisation request #" + id + " approved successfully.");

                return "redirect:/admin/auth-request/view?id=" + id;
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Placement authorisation request is not in a state to be finally approved");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Placement authorisation request / tutor not found");
        }
    }

    /**
     * Admin view all placements endpoint
     *
     * @param status The status of the placements to filter by on the view
     * @param model  The model to add attributes to
     * @return The all placements view
     */
    @GetMapping("/admin/placements/{status}")
    public String placements(@PathVariable String status, Model model) {
        Gson gson = new Gson();
        switch (status.toLowerCase()) {
            case "upcoming":
                List<Placement> upcomingPlacements = placementRepo.findAllByStatusIs(PlacementStatus.UPCOMING);
                Collections.reverse(upcomingPlacements);  // Reverse the list in place to show the most recent placements first
                model.addAttribute("placements", upcomingPlacements);
                model.addAttribute("tab", "Upcoming");
                // Add the chart data to the model
                ChartDataDTO placementByProgramChartData = chartService.getPlacementsByProgramme(upcomingPlacements);
                ChartDataDTO remoteVsOnsiteChartData = chartService.getRemoteVsOnsitePlacements(upcomingPlacements);
                ChartDataDTO travelArrangementsChartData = chartService.getTravelArrangements(upcomingPlacements);
                ChartDataDTO residentialArrangementsChartData = chartService.getResidentialArrangements(upcomingPlacements);
                model.addAttribute("placementByProgramChartData", gson.toJson(placementByProgramChartData));
                model.addAttribute("remoteVsOnsiteChartData", gson.toJson(remoteVsOnsiteChartData));
                model.addAttribute("travelArrangementsChartData", gson.toJson(travelArrangementsChartData));
                model.addAttribute("residentialArrangementsChartData", gson.toJson(residentialArrangementsChartData));
                break;
            case "in-progress":
                List<Placement> inProgressPlacements = placementRepo.findAllByStatusIs(PlacementStatus.IN_PROGRESS);
                Collections.reverse(inProgressPlacements);  // Reverse the list in place to show the most recent placements first
                model.addAttribute("placements", inProgressPlacements);
                model.addAttribute("tab", "In Progress");
                // Add the chart data to the model
                ChartDataDTO placementByProgramChartDataInProgress = chartService.getPlacementsByProgramme(inProgressPlacements);
                ChartDataDTO remoteVsOnsiteChartDataInProgress = chartService.getRemoteVsOnsitePlacements(inProgressPlacements);
                ChartDataDTO travelArrangementsChartDataInProgress = chartService.getTravelArrangements(inProgressPlacements);
                ChartDataDTO residentialArrangementsChartDataInProgress = chartService.getResidentialArrangements(inProgressPlacements);
                model.addAttribute("placementByProgramChartData", gson.toJson(placementByProgramChartDataInProgress));
                model.addAttribute("remoteVsOnsiteChartData", gson.toJson(remoteVsOnsiteChartDataInProgress));
                model.addAttribute("travelArrangementsChartData", gson.toJson(travelArrangementsChartDataInProgress));
                model.addAttribute("residentialArrangementsChartData", gson.toJson(residentialArrangementsChartDataInProgress));
                break;
            case "completed":
                List<Placement> completedPlacements = placementRepo.findAllByStatusIs(PlacementStatus.COMPLETED);
                Collections.reverse(completedPlacements);  // Reverse the list in place to show the most recent placements first
                model.addAttribute("placements", completedPlacements);
                model.addAttribute("tab", "Completed");
                // Add the chart data to the model
                ChartDataDTO placementByProgramChartDataCompleted = chartService.getPlacementsByProgramme(completedPlacements);
                ChartDataDTO remoteVsOnsiteChartDataCompleted = chartService.getRemoteVsOnsitePlacements(completedPlacements);
                ChartDataDTO travelArrangementsChartDataCompleted = chartService.getTravelArrangements(completedPlacements);
                ChartDataDTO residentialArrangementsChartDataCompleted = chartService.getResidentialArrangements(completedPlacements);
                model.addAttribute("placementByProgramChartData", gson.toJson(placementByProgramChartDataCompleted));
                model.addAttribute("remoteVsOnsiteChartData", gson.toJson(remoteVsOnsiteChartDataCompleted));
                model.addAttribute("travelArrangementsChartData", gson.toJson(travelArrangementsChartDataCompleted));
                model.addAttribute("residentialArrangementsChartData", gson.toJson(residentialArrangementsChartDataCompleted));
                break;
            case "all":
                List<Placement> allPlacements = (List<Placement>) placementRepo.findAll();
                Collections.reverse(allPlacements);  // Reverse the list in place to show the most recent placements first
                model.addAttribute("placements", allPlacements);
                model.addAttribute("tab", "All");
                // Add the chart data to the model
                ChartDataDTO placementByProgramChartDataAll = chartService.getPlacementsByProgramme(allPlacements);
                ChartDataDTO remoteVsOnsiteChartDataAll = chartService.getRemoteVsOnsitePlacements(allPlacements);
                ChartDataDTO travelArrangementsChartDataAll = chartService.getTravelArrangements(allPlacements);
                ChartDataDTO residentialArrangementsChartDataAll = chartService.getResidentialArrangements(allPlacements);
                model.addAttribute("placementByProgramChartData", gson.toJson(placementByProgramChartDataAll));
                model.addAttribute("remoteVsOnsiteChartData", gson.toJson(remoteVsOnsiteChartDataAll));
                model.addAttribute("travelArrangementsChartData", gson.toJson(travelArrangementsChartDataAll));
                model.addAttribute("residentialArrangementsChartData", gson.toJson(residentialArrangementsChartDataAll));
                break;
            default:
                // If the status is invalid, return a Bad Request response
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to view placements - Invalid status");
        }
        return "admin/placements";
    }

    /**
     * Admin view details of a specific placement endpoint
     *
     * @param id    The ID of the placement to view
     * @param model The model to add attributes to
     * @return The view placement view
     */
    @GetMapping("/admin/placement/view")
    public String viewPlacement(@RequestParam Long id, Model model) {
        Optional<Placement> placement = placementRepo.findById(id);
        if (placement.isPresent()) {
            // Add the placement to the modal
            model.addAttribute("placement", placement.get());

            // Add the tutors to the modal, for the tutor change modal
            model.addAttribute("tutors", tutorRepo.findAll());
            return "admin/viewPlacement";
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Placement not found");
        }
    }

    /**
     * Endpoint for the admin to change the tutor of a placement
     *
     * @param placementId        ID of the placement
     * @param newTutorUsername   Username of the new tutor to be assigned to the placement
     * @param redirectAttributes Redirect attributes to add a flash attribute to
     * @return Redirect to the view placement page
     */
    @PostMapping("/admin/placement/change-tutor")
    public String changePlacementTutor(@RequestParam Long placementId, @RequestParam String newTutorUsername, RedirectAttributes redirectAttributes) {
        Optional<Placement> placementOptional = placementRepo.findById(placementId);
        Tutor newTutor = tutorRepo.findTutorByUsername(newTutorUsername);
        if (placementOptional.isPresent() && newTutor != null) {
            Placement placement = placementOptional.get();
            Tutor oldTutor = placement.getTutor();

            // Check the tutor is different
            if (oldTutor.equals(newTutor)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The new tutor cannot be the same as the current tutor");
            }

            // Remove the placement from the old tutor and add to the new tutor
            oldTutor.getPlacements().remove(placement);
            newTutor.getPlacements().add(placement);

            // Update the placement tutor
            placement.setTutor(newTutor);

            // Clear the message chat
            placement.getMessageChat().clear();

            // Clear the visits
            placement.getVisits().clear();

            // Save the placement and the tutors
            placementRepo.save(placement);
            tutorRepo.save(oldTutor);
            tutorRepo.save(newTutor);

            // Create a notification for the student, the old tutor, and the new tutor
            notificationService.createStudentNotification(placement.getStudent(), "Placement Tutor Changed", "The tutor for your placement at " + placement.getCompany().getCompanyName() + " has been changed.", "/student/tutor", "View Tutor");
            notificationService.createTutorNotification(oldTutor, "Placement Unassigned", "You are no longer the tutor for " + placement.getStudent().getFirstName() + " " + placement.getStudent().getLastName() + " at " + placement.getCompany().getCompanyName() + ".", "", "");
            notificationService.createTutorNotification(newTutor, "New Placement Assigned", "You have been assigned as the tutor for " + placement.getStudent().getFirstName() + " " + placement.getStudent().getLastName() + " at " + placement.getCompany().getCompanyName() + ".", "/tutor/placement/view?id=" + placement.getId(), "View Placement");

            // Add a success message as a flash attribute
            redirectAttributes.addFlashAttribute("successMessage", "Placement tutor changed successfully.");
            return "redirect:/admin/placement/view?id=" + placementId;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Placement / tutor not found");
        }
    }

    /**
     * Admin view all tutors endpoint
     *
     * @param model The model to add attributes to
     * @return The all tutors view
     */
    @GetMapping("/admin/tutors/all")
    public String tutors(Model model) {
        model.addAttribute("tutors", tutorRepo.findAll());
        return "admin/tutors";
    }

    /**
     * AJAX endpoint to get the details of placements assigned to a tutor
     *
     * @param username The username of the tutor
     * @return A list of PlacementDTO objects
     */
    @ResponseBody
    @GetMapping("/admin/tutors/placements")
    public ResponseEntity<List<PlacementDTO>> getTutorPlacements(@RequestParam String username) {
        // Get the tutor entity and check if it exists
        Tutor tutor = tutorRepo.findTutorByUsername(username);
        if (tutor == null) {
            return ResponseEntity.notFound().build();
        }

        // Get the placements assigned to the tutor
        List<Placement> tutorPlacements = tutor.getPlacements();

        // Create a list of PlacementDTO objects to return
        List<PlacementDTO> placementDTOList = new ArrayList<>();
        for (Placement placement : tutorPlacements) {
            PlacementDTO placementDTO = new PlacementDTO();
            placementDTO.setPlacementId(placement.getId());
            placementDTO.setStudentName(placement.getStudent().getFirstName() + " " + placement.getStudent().getLastName());
            placementDTO.setCompanyName(placement.getCompany().getCompanyName());
            placementDTO.setStartDate(placement.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            placementDTO.setEndDate(placement.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            placementDTO.setStatus(placement.getStatus().getFormattedStatus());
            placementDTOList.add(placementDTO);
        }

        return ResponseEntity.ok(placementDTOList);
    }

    /**
     * Admin view all companies endpoint
     *
     * @param model The model to add attributes to
     * @return The all companies view
     */
    @GetMapping("/admin/companies/all")
    public String companies(Model model) {
        // Get the list of companies and their addresses as JSON for the map
        List<Company> companies = (List<Company>) companyRepo.findAll();
        model.addAttribute("companies", companies);
        model.addAttribute("companyAddresses", placementService.getCompanyAddressesJSON(companies));
        return "admin/companies";
    }

    /**
     * AJAX endpoint to get the details of placements assigned to a company
     *
     * @param id The ID of the company
     * @return A list of PlacementDTO objects
     */
    @ResponseBody
    @GetMapping("/admin/companies/placements")
    public ResponseEntity<List<PlacementDTO>> getCompanyCurrentPlacements(@RequestParam Long id) {
        // Get the company entity and check if it exists
        Optional<Company> companyOptional = companyRepo.findCompanyById(id);
        if (companyOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Company company = companyOptional.get();

        // Get the placements assigned to the company
        List<Placement> companyCurrentPlacements = company.getCurrentPlacements();

        // Create a list of PlacementDTO objects to return
        List<PlacementDTO> placementDTOList = new ArrayList<>();
        for (Placement placement : companyCurrentPlacements) {
            PlacementDTO placementDTO = new PlacementDTO();
            placementDTO.setPlacementId(placement.getId());
            placementDTO.setStudentName(placement.getStudent().getFirstName() + " " + placement.getStudent().getLastName());
            placementDTO.setCompanyName(placement.getCompany().getCompanyName());
            placementDTO.setStartDate(placement.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            placementDTO.setEndDate(placement.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            placementDTO.setStatus(placement.getStatus().getFormattedStatus());
            placementDTOList.add(placementDTO);
        }

        return ResponseEntity.ok(placementDTOList);
    }

    /**
     * AJAX endpoint to get the chart data for the top companies chart, by a given feedback category
     *
     * @param category The feedback category to get the top companies for
     * @return The chart data as a ChartDataDTO object, for Chart.js to render
     */
    @ResponseBody
    @GetMapping("/admin/companies/top-companies-chart/get")
    public ResponseEntity<ChartDataDTO> topCompaniesByFeedbackChart(@RequestParam String category) {
        ChartDataDTO chartData = chartService.getTopCompaniesByFeedbackCategory(category);
        System.out.println(chartData.getDatasets().get(0).getData());
        return ResponseEntity.ok(chartData);
    }

    /**
     * Endpoint for the admin to create a new company, called from the new company modal
     *
     * @param name               The name of the company
     * @param address            The address of the company
     * @param industry           The industry of the company
     * @param webAddress         The web address of the company
     * @param placeId            The Google Place ID of the company
     * @param formattedAddress   The formatted address of the company
     * @param redirectAttributes The redirect attributes to add a flash attribute to
     * @return Redirect to the view all companies page
     */
    @PostMapping("/admin/company/new/submit")
    public String submitNewCompany(@RequestParam String name, @RequestParam String address, @RequestParam String industry, @RequestParam String webAddress, @RequestParam String placeId, @RequestParam String formattedAddress, RedirectAttributes redirectAttributes) {
        // Create a new company object
        Company company = new Company();
        company.setCompanyName(name);
        Address newAddress = new Address(placeId, formattedAddress);
        addressRepo.save(newAddress);
        company.setAddress(newAddress);
        company.setIndustry(industry);
        company.setWebAddress(webAddress);
        companyRepo.save(company);

        // Add a success message as a flash attribute
        redirectAttributes.addFlashAttribute("successMessage", "Company '" + company.getCompanyName() + "' created successfully.");

        return "redirect:/admin/companies/all";
    }

    /**
     * Admin view details of a specific company endpoint
     *
     * @param id    The ID of the company to view
     * @param model The model to add attributes to
     * @return The view company view
     */
    @GetMapping("/admin/company/view")
    public String viewCompany(@RequestParam Long id, Model model) {
        Optional<Company> companyOptional = companyRepo.findById(id);

        // Check if the company exists
        if (companyOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found");
        }

        // Add the company to the model
        Company company = companyOptional.get();
        model.addAttribute("company", company);
        // Add the company's average ratings to the model
        ChartDataDTO ratingsChartData = chartService.getCompanyAverageRatings(id);
        Gson gson = new Gson();
        String companyRatings = gson.toJson(ratingsChartData);
        model.addAttribute("ratingsChartData", companyRatings);
        return "admin/viewCompany";
    }
}