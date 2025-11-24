package com.example.placementmanagementsystem.controller;

import com.example.placementmanagementsystem.enumeration.PlacementStatus;
import com.example.placementmanagementsystem.model.*;
import com.example.placementmanagementsystem.repository.*;
import com.example.placementmanagementsystem.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller class for handling student specific requests
 */
@Controller
public class StudentController {

    @Autowired
    private CompanyRepo companyRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private PlacementAuthRequestRepo placementAuthRequestRepo;
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private PlacementService placementService;
    @Autowired
    private MessageRepo messageRepo;
    @Autowired
    private VisitRepo visitRepo;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private PlacementEvaluationRepo placementEvaluationRepo;
    @Autowired
    private PlacementRepo placementRepo;
    @Autowired
    private GoogleGeminiService googleGeminiService;
    @Autowired
    private FileService fileService;

    /**
     * Student dashboard endpoint
     *
     * @param model The model to add attributes to
     * @return The student dashboard view
     */
    @GetMapping("/student/dashboard")
    public String studentDashboard(Model model) {
        Student currentStudent = userService.getCurrentLoggedInStudent();

        // Add a flag to indicate if the student has a pending placement authorisation request
        boolean hasPendingAuthRequest = placementService.hasStudentPendingAuthRequest(currentStudent);
        model.addAttribute("hasPendingAuthRequest", hasPendingAuthRequest);

        // Add the student's placement auth requests to the model - if none, this will be an empty list
        List<PlacementAuthRequest> placementAuthRequests = currentStudent.getPlacementAuthRequests();
        // Reverse the order of the list, to sort them by most recent first
        Collections.reverse(placementAuthRequests);
        model.addAttribute("authRequests", placementAuthRequests);

        // Add a flag to indicate if the student is currently on placement
        boolean onPlacement = placementService.isStudentOnPlacement(currentStudent);
        model.addAttribute("onPlacement", onPlacement);

        // Add a flag to indicate if the student has an upcoming placement
        boolean upcomingPlacement = placementService.isStudentPlacementUpcoming(currentStudent);
        model.addAttribute("upcomingPlacement", upcomingPlacement);

        // Add a flag to indicate if the student has a pending placement evaluation
        boolean hasPendingEvaluation = placementService.hasStudentPendingEvaluation(currentStudent);
        model.addAttribute("hasPendingEvaluation", hasPendingEvaluation);

        // Add the student's current placement to the model - if not on placement or pending evaluation, this will be null
        Placement currentPlacement = placementService.getStudentCurrentPlacement(currentStudent);
        model.addAttribute("placement", currentPlacement);

        // Add the student unread messages count to the model
        if (currentPlacement != null) {
            int messageCount = messageRepo.findMessagesByReceiverAndIsRead(currentStudent, false).size();
            model.addAttribute("unreadMessageCount", messageCount);
        } else {
            model.addAttribute("unreadMessageCount", 0);
        }

        // Add the student's notifications to the model
        model.addAttribute("unreadNotifications", currentStudent.getNotifications());

        return "student/dashboard";
    }

    /**
     * Student profile page endpoint
     *
     * @return The student profile view
     */
    @GetMapping("/student/profile")
    public String profile() {
        return "student/profile";
    }

    /**
     * Student submit change password endpoint
     *
     * @param currentPassword    The current password provided by the user
     * @param newPassword        The new password provided by the user
     * @param repeatNewPassword  The repeated new password provided by the user
     * @param redirectAttributes The redirect attributes to add flash attributes to
     * @return A redirect to the student profile page with a success message if the password was changed successfully, or an error message if the current password was incorrect
     */
    @PostMapping("/student/profile/change-password")
    public String changePassword(@RequestParam String currentPassword, @RequestParam String newPassword, @RequestParam String repeatNewPassword, RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentLoggedInUser();
        // Check the new password and repeat new password match
        if (!newPassword.equals(repeatNewPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to change password. New password and repeat new password do not match.");
            return "redirect:/student/profile";
        }
        if (userService.changeUserPassword(currentUser, currentPassword, newPassword)) {
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to change password. Incorrect current password.");
        }
        return "redirect:/student/profile";
    }

    /**
     * AJAX endpoint for students to acknowledge a notification
     *
     * @param id The ID of the notification to acknowledge
     * @return A response entity with a success message if the notification was acknowledged, or a 404 Not Found response if the notification was not found
     */
    @PostMapping("/student/notification/acknowledge")
    @ResponseBody
    public ResponseEntity<String> acknowledgeNotification(@RequestParam Long id) {
        Student student = userService.getCurrentLoggedInStudent();
        boolean success = notificationService.acknowledgeNotification(student, id);

        // If the notification was not acknowledged
        if (!success) {
            // Return a 404 Not Found response
            return ResponseEntity.notFound().build();
        } else {
            // Return a 200 OK response
            return ResponseEntity.ok("Notification acknowledged successfully");
        }
    }

    /**
     * Student placement authorisation request form endpoint
     *
     * @param model The model to add attributes to
     * @return The placement authorisation request form view
     */
    @GetMapping("/student/auth-request/new")
    public String placementAuthorisationForm(Model model) {
        Student currentStudent = userService.getCurrentLoggedInStudent();

        // Check if the student is already on a placement or has one upcoming
        boolean onPlacement = placementService.isStudentOnPlacement(currentStudent);
        boolean upcomingPlacement = placementService.isStudentPlacementUpcoming(currentStudent);
        if (onPlacement || upcomingPlacement) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot raise a new placement authorisation request, student already has a placement");
        }
        // Check if the student has a pending placement authorisation request
        boolean hasPendingAuthRequest = placementService.hasStudentPendingAuthRequest(currentStudent);
        if (hasPendingAuthRequest) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot raise a new placement authorisation request, student already has a pending request");
        }
        // Check if the student has a pending placement evaluation
        boolean hasPendingEvaluation = placementService.hasStudentPendingEvaluation(currentStudent);
        if (hasPendingEvaluation) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot raise a new placement authorisation request, student has a pending evaluation");
        }

        model.addAttribute("placementAuthRequest", new PlacementAuthRequest());
        model.addAttribute("allCompanies", companyRepo.findAll());
        return "student/placementAuthorisationForm";
    }

    /**
     * Submit endpoint for the student placement authorisation request form
     *
     * @param placementAuthRequest The placement authorisation request form object to submit
     * @param redirectAttributes   The redirect attributes to add flash attributes to
     * @return A redirect to the student dashboard with a success message if the placement authorisation request was submitted successfully
     */
    @PostMapping("/student/auth-request/new/submit")
    public String submitPlacementAuthorisationForm(@ModelAttribute PlacementAuthRequest placementAuthRequest, RedirectAttributes redirectAttributes) {
        Student currentStudent = userService.getCurrentLoggedInStudent();

        // Check if the student is already on a placement or has one upcoming
        boolean onPlacement = placementService.isStudentOnPlacement(currentStudent);
        boolean upcomingPlacement = placementService.isStudentPlacementUpcoming(currentStudent);
        if (onPlacement || upcomingPlacement) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot raise a new placement authorisation request, student already has a current placement");
        }
        // Check if the student has a pending placement authorisation request
        boolean hasPendingAuthRequest = placementService.hasStudentPendingAuthRequest(currentStudent);
        if (hasPendingAuthRequest) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot raise a new placement authorisation request, student already has a pending request");
        }
        // Check if the student has a pending placement evaluation
        boolean hasPendingEvaluation = placementService.hasStudentPendingEvaluation(currentStudent);
        if (hasPendingEvaluation) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot raise a new placement authorisation request, student has a pending evaluation");
        }

        // Check the start date is not in the past
        if (placementAuthRequest.getPlacementStartDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Placement start date must be in the future");
        }
        // Check the start date is before the end date
        if (placementAuthRequest.getPlacementStartDate().isAfter(placementAuthRequest.getPlacementEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Placement start date must be before the end date");
        }

        // Set the company for the placement auth request, if an existing company was selected
        if (!placementAuthRequest.getCompanySelect().equals("Other")) {
            Optional<Company> selectedCompany = companyRepo.findCompanyById(Long.valueOf(placementAuthRequest.getCompanySelect()));
            if (selectedCompany.isPresent()) {
                placementAuthRequest.setCompany(selectedCompany.get());
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found");
            }
        }

        // Add the creation date to the placement auth request
        placementAuthRequest.setRequestedOn(LocalDate.now());
        // Save the placement auth request, and add it to the student's list of placement auth requests
        currentStudent.addPlacementAuthRequest(placementAuthRequest);
        placementAuthRequest.setStudent(currentStudent);
        placementAuthRequestRepo.save(placementAuthRequest);
        studentRepo.save(currentStudent);

        // Add a success message as a flash attribute
        redirectAttributes.addFlashAttribute("successMessage", "Placement authorisation request submitted successfully.");
        return "redirect:/student/dashboard";
    }

    /**
     * View placement authorisation request endpoint for students to view their placement authorisation requests
     *
     * @param id    The ID of the placement authorisation request to view
     * @param model The model to add attributes to
     * @return The view placement authorisation request view
     */
    @GetMapping("/student/auth-request/view")
    public String viewPlacementAuthRequest(@RequestParam Long id, Model model) {
        Optional<PlacementAuthRequest> placementAuthRequest = placementAuthRequestRepo.findById(id);
        if (placementAuthRequest.isPresent()) {
            model.addAttribute("authRequest", placementAuthRequest.get());
            return "student/viewAuthRequest";
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Placement authorisation request not found");
        }
    }

    /**
     * View placement endpoint for students to view their current placement
     *
     * @param model The model to add attributes to
     * @return The view placement view
     */
    @GetMapping("student/placement")
    public String viewPlacement(Model model) {
        Student currentStudent = userService.getCurrentLoggedInStudent();

        // Add a flag to indicate if the student is currently on placement, has an upcoming placement, or has a pending placement evaluation
        boolean onPlacement = placementService.isStudentOnPlacement(currentStudent);
        boolean upcomingPlacement = placementService.isStudentPlacementUpcoming(currentStudent);
        boolean hasPendingEvaluation = placementService.hasStudentPendingEvaluation(currentStudent);
        model.addAttribute("onPlacement", onPlacement);
        model.addAttribute("upcomingPlacement", upcomingPlacement);
        model.addAttribute("hasPendingEvaluation", hasPendingEvaluation);

        // If the student is on placement currently, add the placement to the model
        if (onPlacement || upcomingPlacement) {
            Placement currentPlacement = placementService.getStudentCurrentPlacement(currentStudent);
            model.addAttribute("placement", currentPlacement);
        }
        return "student/myPlacement";
    }

    /**
     * View tutor endpoint for students to view their tutor
     *
     * @param model The model to add attributes to
     * @return The view tutor view
     */
    @GetMapping("/student/tutor")
    public String viewTutor(Model model) {
        Student currentStudent = userService.getCurrentLoggedInStudent();

        // Add a flag to indicate if the student is currently on placement, has an upcoming placement, or has a pending placement evaluation
        boolean onPlacement = placementService.isStudentOnPlacement(currentStudent);
        boolean upcomingPlacement = placementService.isStudentPlacementUpcoming(currentStudent);
        model.addAttribute("onPlacement", onPlacement);
        model.addAttribute("upcomingPlacement", upcomingPlacement);

        // If the student is not on placement, return the view without the placement details
        if (!onPlacement && !upcomingPlacement) {
            return "student/myTutor";
        }

        // Add the placement to the model
        Placement currentPlacement = placementService.getStudentCurrentPlacement(currentStudent);
        model.addAttribute("placement", currentPlacement);

        // Add the tutor to the model
        Tutor tutor = placementService.getStudentPlacementTutor(currentStudent);
        model.addAttribute("tutor", tutor);

        // Set the unread messages to read, if the student is the receiver
        for (Message message : currentPlacement.getMessageChat()) {
            if (!message.isRead() && message.getReceiver().getUsername().equals(currentStudent.getUsername())) {
                message.setRead(true);
                messageRepo.save(message);
            }
        }
        // Add the messages to the model
        model.addAttribute("messages", currentPlacement.getMessageChat());
        return "student/myTutor";
    }

    /**
     * Download calendar event endpoint for students to download a calendar event for a visit
     *
     * @param visitId The ID of the visit to download the calendar event for
     * @return A response entity with the calendar event as a download
     */
    @GetMapping("/student/tutor/visits/download-event")
    public ResponseEntity<byte[]> downloadVisitCalendarEvent(@RequestParam Long visitId) {
        // Fetch visit details from your database
        Optional<Visit> visitOptional = visitRepo.getVisitById(visitId);
        if (visitOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Visit not found");
        }
        Visit visit = visitOptional.get();
        String eventName = visit.getStatus().getFormattedStatus();
        String eventStart = visit.getDate().atTime(visit.getStartTime()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String eventEnd = visit.getDate().atTime(visit.getEndTime()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String location;
        if (visit.isOnline()) {
            location = "Microsoft Teams";
        } else {
            location = visit.getPlacement().getCompany().getAddress().getFormattedAddress();
        }

        String icsContent = "BEGIN:VCALENDAR\n" + "VERSION:2.0\n" + "BEGIN:VEVENT\n" + "SUMMARY:" + eventName + "\n" + "DTSTART:" + eventStart.replace("-", "").replace(":", "") + "Z\n" + "DTEND:" + eventEnd.replace("-", "").replace(":", "") + "Z\n" + "LOCATION:" + location + "\n" + "END:VEVENT\n" + "END:VCALENDAR";

        // Return ICS file as a download
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=event.ics").header(HttpHeaders.CONTENT_TYPE, "text/calendar; charset=utf-8").body(icsContent.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Resources page endpoint for students
     *
     * @param category The category of resources to view
     * @param model    The model to add attributes to
     * @return The resources view
     */
    @GetMapping("/student/resources/{category}")
    public String viewResources(@PathVariable String category, Model model) {
        // If the category is valid, add the category label to the model
        if (category.equals("all") || category.equals("applying-for-placements") || category.equals("guides") || category.equals("skills-development") || category.equals("placement-policies") || category.equals("support")) {
            model.addAttribute("category", category);
        } else {
            // If the category is invalid, return a Bad Request response
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to view resources - Invalid category");
        }
        return "student/resources";
    }

    /**
     * Student placement evaluation form page endpoint
     *
     * @param model The model to add attributes to
     * @return The placement evaluation form view
     */
    @GetMapping("/student/evaluation/new")
    public String placementEvaluationForm(Model model) {
        Student currentStudent = userService.getCurrentLoggedInStudent();

        // Check if the student is on placement or has an upcoming placement
        boolean onPlacement = placementService.isStudentOnPlacement(currentStudent);
        boolean upcomingPlacement = placementService.isStudentPlacementUpcoming(currentStudent);
        if (onPlacement || upcomingPlacement) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot submit a placement evaluation, student currently has a placement");
        }
        // Check if the student has a pending placement evaluation
        boolean hasPendingEvaluation = placementService.hasStudentPendingEvaluation(currentStudent);
        if (!hasPendingEvaluation) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot submit a placement evaluation, student does not have any pending evaluations");
        }

        // Add the placement evaluation form to the model
        model.addAttribute("placementEvaluation", new PlacementEvaluation());
        // Add the student's pending placement to the model
        model.addAttribute("placement", placementService.getPlacementPendingEvaluationByStudent(currentStudent));
        return "student/placementEvaluationForm";
    }

    /**
     * Submit endpoint for the student placement evaluation form
     *
     * @param placementEvaluation The placement evaluation form object to submit
     * @param redirectAttributes  The redirect attributes to add flash attributes to
     * @return A redirect to the student dashboard with a success message if the placement evaluation was submitted successfully
     */
    @PostMapping("/student/evaluation/new/submit")
    public String submitPlacementEvaluationForm(@ModelAttribute PlacementEvaluation placementEvaluation, RedirectAttributes redirectAttributes) {
        Student currentStudent = userService.getCurrentLoggedInStudent();

        // Check if the student is on placement or has an upcoming placement
        boolean onPlacement = placementService.isStudentOnPlacement(currentStudent);
        boolean upcomingPlacement = placementService.isStudentPlacementUpcoming(currentStudent);
        if (onPlacement || upcomingPlacement) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot submit a placement evaluation, student currently has a placement");
        }
        // Check if the student has a pending placement evaluation
        boolean hasPendingEvaluation = placementService.hasStudentPendingEvaluation(currentStudent);
        if (!hasPendingEvaluation) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot submit a placement evaluation, student does not have any pending evaluations");
        }

        // Set the evaluation date to the current date
        placementEvaluation.setEvaluationDate(LocalDate.now());
        // Get the id of the placement pending evaluation
        Placement pendingPlacement = placementService.getPlacementPendingEvaluationByStudent(currentStudent);
        // Set the placement company to the evaluation
        placementEvaluation.setCompany(pendingPlacement.getCompany());
        pendingPlacement.getCompany().getEvaluations().add(placementEvaluation);
        // Save the placement evaluation
        placementEvaluationRepo.save(placementEvaluation);

        // Update the placement status to completed
        pendingPlacement.setStatus(PlacementStatus.COMPLETED);
        // Link the placement evaluation to the placement
        pendingPlacement.setPlacementEvaluation(placementEvaluation);
        placementRepo.save(pendingPlacement);

        // Add a success message as a flash attribute
        redirectAttributes.addFlashAttribute("successMessage", "Placement evaluation submitted successfully.");
        return "redirect:/student/dashboard";
    }

    /**
     * View previous placements endpoint for students to view their previous placements
     *
     * @param model The model to add attributes to
     * @return The previous placements view
     */
    @GetMapping("/student/previous-placements")
    public String viewPreviousPlacements(Model model) {
        Student currentStudent = userService.getCurrentLoggedInStudent();

        // Add the student's completed placements to the model
        List<Placement> previousPlacements = placementRepo.findPlacementsByStudentAndStatusIn(currentStudent, Arrays.asList(PlacementStatus.PENDING_STUDENT_EVALUATION, PlacementStatus.COMPLETED));
        model.addAttribute("placements", previousPlacements);
        return "student/previousPlacements";
    }

    /**
     * View placement endpoint for students to view a specific placement
     *
     * @param id    The ID of the placement to view
     * @param model The model to add attributes to
     * @return The view placement view
     */
    @GetMapping("/student/placement/view")
    public String viewPlacement(@RequestParam Long id, Model model) {
        Student currentStudent = userService.getCurrentLoggedInStudent();
        Optional<Placement> placementOptional = placementRepo.findById(id);
        if (placementOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Placement not found");
        }
        Placement placement = placementOptional.get();

        // Check if the student is authorised to view the placement
        if (!placement.getStudent().equals(currentStudent)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorised to view this placement");
        }

        // Add the placement to the model
        model.addAttribute("placement", placement);
        return "student/viewPlacement";
    }

    /**
     * Update placement dates form endpoint, for students to update the start and end dates of their placement
     *
     * @param newStartDate       The new start date of the placement
     * @param newEndDate         The new end date of the placement
     * @param redirectAttributes The redirect attributes to add flash attributes to
     * @return A redirect to the student dashboard with a success message if the placement dates were updated successfully
     */
    @PostMapping("/student/placement/update-start-end-date/submit")
    public String updatePlacementDates(@RequestParam LocalDate newStartDate, @RequestParam LocalDate newEndDate, RedirectAttributes redirectAttributes) {
        Student currentStudent = userService.getCurrentLoggedInStudent();

        // Get the student's current placement
        Placement placement = placementService.getStudentCurrentPlacement(currentStudent);
        if (placement == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update placement dates, student does not have a current placement");
        }

        // If placement is upcoming either start date or end date can be changed
        // If placement is in progress, only end date can be changed
        // If placement is completed/evaluation pending, no changes can be made

        // Start must be before end
        // If start date is changed, it must be in the future
        // If end date is changed, it must be after the start date

        // Check the new start date and end date are valid
        if (placement.getStatus() == PlacementStatus.UPCOMING) {
            if (newStartDate.isBefore(LocalDate.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New start date must be in the future");
            }
            if (newStartDate.isAfter(newEndDate)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New start date must be before the new end date");
            }
        } else if (placement.getStatus() == PlacementStatus.IN_PROGRESS) {
            if (newStartDate.isAfter(newEndDate)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New end date must be after the start date");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update placement dates, placement is not in progress or upcoming");
        }

        // Check the end date is not past the start of the next academic year
        LocalDate nextAcademicYearStart;
        // Calculate when the start of the next academic year is
        if (LocalDate.now().getMonthValue() < Month.SEPTEMBER.getValue()) {
            nextAcademicYearStart = LocalDate.of(LocalDate.now().getYear(), Month.SEPTEMBER, 30);
        } else {
            nextAcademicYearStart = LocalDate.of(LocalDate.now().getYear() + 1, Month.SEPTEMBER, 30);
        }
        // Check the new end date is before the start of the next academic year
        if (newEndDate.isAfter(nextAcademicYearStart)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New end date must be before the start of the next academic year");
        }

        // Update the start and end dates of the placement
        placement.setStartDate(newStartDate);
        placement.setEndDate(newEndDate);
        placementRepo.save(placement);

        // Update the placement status in case the dates have changed it
        placementService.updatePlacementStatus(placement);

        // Add a success message as a flash attribute
        redirectAttributes.addFlashAttribute("successMessage", "Your placement start/end dates have been updated successfully.");

        return "redirect:/student/placement";
    }

    /**
     * View AI cover letter generator page endpoint
     *
     * @return The AI cover letter generator view
     */
    @GetMapping("/student/resources/applying-for-placements/ai-cover-letter")
    public String aiCoverLetter() {
        return "student/aiCoverLetter";
    }

    /**
     * AJAX endpoint for students to generate a cover letter using the Google Gemini API
     *
     * @param cv             The student's CV file - PDF or DOCX
     * @param jobTitle       The job title of the placement
     * @param jobDescription The job description of the placement
     * @return A JSON response with the generated cover letter
     */
    @PostMapping("/student/resources/applying-for-placements/ai-cover-letter/generate")
    @ResponseBody
    public Map<String, String> generateCoverLetter(@RequestParam MultipartFile cv, @RequestParam String companyName, @RequestParam String jobTitle, @RequestParam String jobDescription) {
        try {
            // Check that the file type is valid - PDF or DOCX
            String cvString;
            if (cv.getContentType().equals("application/pdf")) {
                // Convert the PDF CV into a string
                cvString = fileService.extractTextFromPdf(cv);
            } else if (cv.getContentType().equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                // Convert the DOCX CV into a string
                cvString = fileService.extractTextFromDocx(cv);
            } else {
                // If the valid file types are not provided, return a 400 Bad Request response
                throw new Exception("Invalid file type, please upload a PDF or DOCX file");
            }
            // Generate the cover letter using the Google Gemini API
            String coverLetter = googleGeminiService.generateCoverLetter(cvString, companyName, jobTitle, jobDescription);
            // Replace \n with <br> for HTML formatting
            String coverLetterFormatted = coverLetter.replace("\n", "<br>");
            // Add the cover letter to the response map to produce a JSON response
            return Map.of("coverLetter", coverLetterFormatted);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating cover letter: " + e.getMessage());
        }
    }
}