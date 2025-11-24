package com.example.placementmanagementsystem.controller;

import com.example.placementmanagementsystem.dto.CalendarEventDTO;
import com.example.placementmanagementsystem.dto.VisitSuggestionDTO;
import com.example.placementmanagementsystem.dto.VisitSuggestionReqDTO;
import com.example.placementmanagementsystem.dto.VisitDTO;
import com.example.placementmanagementsystem.enumeration.PlacementStatus;
import com.example.placementmanagementsystem.enumeration.VisitStatus;
import com.example.placementmanagementsystem.model.*;
import com.example.placementmanagementsystem.repository.AddressRepo;
import com.example.placementmanagementsystem.repository.MessageRepo;
import com.example.placementmanagementsystem.repository.PlacementRepo;
import com.example.placementmanagementsystem.repository.VisitRepo;
import com.example.placementmanagementsystem.service.CalendarService;
import com.example.placementmanagementsystem.service.NotificationService;
import com.example.placementmanagementsystem.service.PlacementService;
import com.example.placementmanagementsystem.service.UserService;
import com.google.gson.Gson;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Controller for handling tutor specific requests
 */
@Controller
public class TutorController {
    @Autowired
    private PlacementRepo placementRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private PlacementService placementService;
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private VisitRepo visitRepo;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private AddressRepo addressRepo;
    @Autowired
    private MessageRepo messageRepo;
    @Value("${spring.security.oauth2.client.registration.microsoft.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.microsoft.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.provider.microsoft.token-uri}")
    private String tokenUri;
    @Value("${spring.security.oauth2.client.registration.microsoft.redirect-uri}")
    private String redirectUri;
    @Value("${spring.security.oauth2.client.registration.microsoft.authorization-grant-type}")
    private String authorisationGrantType;

    /**
     * Tutor dashboard endpoint
     *
     * @param model The model to add attributes to
     * @return The tutor dashboard view
     */
    @GetMapping("/tutor/dashboard")
    public String tutorDashboard(Model model) {
        Tutor currentTutor = userService.getCurrentLoggedInTutor();

        // Add the placements the tutor is overseeing to the model
        List<PlacementStatus> statuses = Arrays.asList(PlacementStatus.UPCOMING, PlacementStatus.IN_PROGRESS);
        List<Placement> placements = placementRepo.findPlacementsByTutorAndStatusIn(currentTutor, statuses);
        model.addAttribute("placements", placements);

        // Add the tutor's visits to the model
        model.addAttribute("visits", placementService.getTutorVisits(currentTutor));

        // Add the placement addresses to the model, to be used by the Google Maps API
        model.addAttribute("placementAddresses", placementService.getPlacementAddressesJSON(placements));

        // Add the tutor's unread notifications to the model
        model.addAttribute("unreadNotifications", currentTutor.getNotifications());

        // Add the tutor's unread message count to the model
        int messageCount = messageRepo.findMessagesByReceiverAndIsRead(currentTutor, false).size();
        model.addAttribute("unreadMessageCount", messageCount);

        return "tutor/dashboard";
    }

    /**
     * Tutor profile endpoint
     *
     * @return The tutor profile view
     */
    @GetMapping("/tutor/profile")
    public String profile() {
        return "tutor/profile";
    }

    /**
     * Tutor submit change password endpoint
     *
     * @param currentPassword    The current password provided by the user
     * @param newPassword        The new password provided by the user
     * @param repeatNewPassword  The repeated new password provided by the user
     * @param redirectAttributes The redirect attributes to add flash attributes to
     * @return A redirect to the tutor profile page with a success message if the password was changed successfully, or an error message if the current password was incorrect
     */
    @PostMapping("/tutor/profile/change-password")
    public String changePassword(@RequestParam String currentPassword, @RequestParam String newPassword, @RequestParam String repeatNewPassword, RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentLoggedInUser();
        // Check the new password and repeat new password match
        if (!newPassword.equals(repeatNewPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to change password. New password and repeat new password do not match.");
            return "redirect:/tutor/profile";
        }
        if (userService.changeUserPassword(currentUser, currentPassword, newPassword)) {
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to change password. Incorrect current password.");
        }
        return "redirect:/tutor/profile";
    }

    /**
     * AJAX endpoint for tutors to acknowledge a notification
     *
     * @param id The ID of the notification to acknowledge
     * @return A response entity with a success message if the notification was acknowledged, or a 404 Not Found response if the notification was not found
     */
    @PostMapping("/tutor/notification/acknowledge")
    @ResponseBody
    public ResponseEntity<String> acknowledgeNotification(@RequestParam Long id) {
        Tutor tutor = userService.getCurrentLoggedInTutor();
        boolean success = notificationService.acknowledgeNotification(tutor, id);

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
     * Tutor view all placements endpoint
     *
     * @param model The model to add attributes to
     * @return The tutor placements view
     */
    @GetMapping("/tutor/placements")
    public String tutorPlacements(Model model) {
        Tutor tutor = userService.getCurrentLoggedInTutor();
        model.addAttribute("placements", placementRepo.findAllByTutor(tutor));
        return "tutor/placements";
    }

    /**
     * Tutor view details of a specific placement endpoint
     *
     * @param id    The ID of the placement to view
     * @param model The model to add attributes to
     * @return The tutor view placement view
     */
    @GetMapping("/tutor/placement/view")
    public String viewPlacement(@RequestParam Long id, Model model) {
        Optional<Placement> placement = placementRepo.findById(id);
        if (placement.isPresent()) {
            if (placement.get().getTutor().equals(userService.getCurrentLoggedInTutor())) {
                model.addAttribute("placement", placement.get());
                return "tutor/viewPlacement";
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to view this placement.");
            }
        } else {
            throw new EntityNotFoundException("Placement not found");
        }
    }

    /**
     * Tutor messages page endpoint
     *
     * @param model The model to add attributes to
     * @return The tutor messages view
     */
    @GetMapping("/tutor/messages")
    public String tutorMessages(Model model) {
        // Add the upcoming and current placements the tutor is overseeing to the model
        Tutor tutor = userService.getCurrentLoggedInTutor();
        List<PlacementStatus> statuses = List.of(PlacementStatus.UPCOMING, PlacementStatus.IN_PROGRESS);
        model.addAttribute("placements", placementRepo.findPlacementsByTutorAndStatusIn(tutor, statuses));
        return "tutor/messages";
    }

    /**
     * Tutor visits page endpoint
     *
     * @param model The model to add attributes to
     * @return The tutor visits view
     */
    @GetMapping("/tutor/visits")
    public String tutorVisits(Model model) {
        // Add the tutor's visits to the model
        Tutor tutor = userService.getCurrentLoggedInTutor();
        List<Visit> visits = placementService.getTutorVisits(tutor);
        model.addAttribute("visits", visits);

        // Exclude the cancelled visits from the model
        List<Visit> upcomingAndCompletedVisits = new ArrayList<>();
        for (Visit visit : visits) {
            if (visit.getStatus() != VisitStatus.CANCELLED) {
                upcomingAndCompletedVisits.add(visit);
            }
        }
        // Add visits in FullCalendar format to the model
        List<CalendarEventDTO> calendarEventDTOs = new ArrayList<>();
        for (Visit visit : upcomingAndCompletedVisits) {
            // Get the title, start and end date for the visit
            String title = visit.getPlacement().getStudent().getFirstName() + " " + visit.getPlacement().getStudent().getLastName() + " | " + visit.getPlacement().getCompany().getCompanyName();
            String start = visit.getDate().atTime(visit.getStartTime()).toString();
            String end = visit.getDate().atTime(visit.getEndTime()).toString();
            // Determine the location of the visit, if it is online or at the company
            String location;
            if (visit.isOnline()) {
                location = "Microsoft Teams";
            } else {
                location = visit.getPlacement().getCompany().getAddress().getFormattedAddress();
            }
            calendarEventDTOs.add(new CalendarEventDTO(visit.getId(), title, start, end, location));
        }
        // Convert the DTOs to JSON and add to the model
        Gson gson = new Gson();
        String visitsJson = gson.toJson(calendarEventDTOs);
        model.addAttribute("visitsJson", visitsJson);
        return "tutor/visits";
    }

    /**
     * Tutor new visit page endpoint
     *
     * @param model   The model to add attributes to
     * @param session The session object which contains a flag attribute, to check if the tutor has linked their calendar
     * @return The tutor new visit view
     */
    @GetMapping("/tutor/visits/new")
    public String newVisit(Model model, HttpSession session) {
        // Get the current logged in tutor
        Tutor tutor = userService.getCurrentLoggedInTutor();

        // Check if the tutor has linked their calendar
        boolean hasLinkedCalendar = calendarService.isCalendarLinked(session);
        model.addAttribute("hasLinkedCalendar", hasLinkedCalendar);

        // Add the tutor's upcoming/current placements to the model
        List<PlacementStatus> statuses = Arrays.asList(PlacementStatus.UPCOMING, PlacementStatus.IN_PROGRESS);
        List<Placement> placements = placementRepo.findPlacementsByTutorAndStatusIn(tutor, statuses);
        model.addAttribute("placements", placements);

        // Add the address of each placement to the model, each with the company name, student name and placement id each in an array
        model.addAttribute("placementAddresses", placementService.getPlacementAddressesJSON(placements));

        // Add a new Visit object to the model
        model.addAttribute("visitDTO", new VisitDTO());

        return "tutor/newVisit";
    }

    /**
     * Redirect to Microsoft OAuth2 authorisation endpoint to link the user's calendar
     *
     * @return A redirect to the Microsoft OAuth2 authorisation endpoint
     */
    @GetMapping("/tutor/visits/new/calendar-auth/send")
    public String calendarAuthSend() {
        return "redirect:/oauth2/authorization/microsoft";
    }

    /**
     * Callback endpoint for Microsoft OAuth2 authorisation, to save the access token in the session
     *
     * @param code      The authorisation code returned by Microsoft
     * @param principal The principal object to get the user's details
     * @param session   The session object to save the access token
     * @return A redirect to the new visit page
     */
    @GetMapping("/tutor/visits/new/calendar-auth/callback")
    public String calendarAuthCallback(@RequestParam String code, Principal principal, HttpSession session) {
        try {
            // Exchange code for access token
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("code", code);
            body.add("grant_type", authorisationGrantType);
            body.add("redirect_uri", redirectUri);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(tokenUri, HttpMethod.POST, requestEntity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new IllegalStateException("Token response body is empty.");
            }

            // Extract tokens
            String accessToken = (String) responseBody.get("access_token");
            int expiresIn = (Integer) responseBody.get("expires_in");

            // Save the token in the session
            calendarService.saveCalendarToken(session, accessToken, expiresIn);

            return "redirect:/tutor/visits/new";
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to link calendar", e);
        }
    }

    /**
     * Get the events for the given week from the calendar, called by FullCalendar to dynamically load events
     *
     * @param start   The start date of the week
     * @param end     The end date of the week
     * @param session The session object to get the API access token
     * @return A JSON response containing the calendar events in the range
     */
    @GetMapping("/tutor/visits/new/calendar-data/get")
    @ResponseBody
    public ResponseEntity<?> getWeekEvents(@RequestParam String start, @RequestParam String end, HttpSession session, RedirectAttributes redirectAttributes) {

        // Check if the calendar is linked
        if (!calendarService.isCalendarLinked(session)) {
            return ResponseEntity.status(401).body("Not authenticated with Outlook");
        }

        try {
            // Parse the dates string to LocalDateTime
            LocalDateTime startDateTime = OffsetDateTime.parse(start).toLocalDateTime();
            LocalDateTime endDateTime = OffsetDateTime.parse(end).toLocalDateTime();
            // Returns the list of events for the given week from the start date
            List<CalendarEventDTO> calendarEvents = calendarService.getEventsInRange(session, startDateTime, endDateTime);
            System.out.println("Fetched calendar events: " + calendarEvents.size());
            return ResponseEntity.ok(calendarEvents);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid date format. Use ISO format (e.g. 2024-03-14T00:00:00)");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching calendar events: " + e.getMessage());
        }
    }

    /**
     * Endpoint for tutor to submit a new visit
     *
     * @param visitDTO           The VisitDTO object containing the new visit details
     * @param session            The session object to check if the tutor has linked their calendar
     * @param redirectAttributes The redirect attributes to add a success message
     * @return A redirect to the tutor visits page
     */
    @PostMapping("/tutor/visits/new/submit")
    public String submitVisit(@ModelAttribute VisitDTO visitDTO, HttpSession session, RedirectAttributes redirectAttributes) {
        // Map the VisitDTO to a Visit object
        Visit visit = new Visit();

        // Find and set the placement for the visit
        Optional<Placement> placement = placementRepo.findById(visitDTO.getPlacementId());
        if (placement.isPresent()) {
            visit.setPlacement(placement.get());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Placement not found");
        }

        // Set the status to UPCOMING
        visit.setStatus(VisitStatus.UPCOMING);

        // Set the date and time for the visit, first checking if they are not in the past
        if (visitDTO.getDate().isBefore(LocalDateTime.now().toLocalDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Visit date cannot be in the past");
        }
        visit.setDate(visitDTO.getDate());
        visit.setStartTime(visitDTO.getTime());

        // Set the meeting duration and end time, first checking if the duration is valid
        if (visitDTO.getMeetingDuration() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Visit duration must be greater than 0");
        }
        visit.setMeetingDuration(visitDTO.getMeetingDuration());
        visit.setEndTime(visitDTO.getTime().plusMinutes(visitDTO.getMeetingDuration()));

        // Set the visit type - if it is online or in-person
        if (visitDTO.getMeetingType().equals("online")) {
            visit.setOnline(true);
        } else if (visitDTO.getMeetingType().equals("in-person")) {
            visit.setOnline(false);
            // If the visit is in-person, set the from address and travel time
            Address fromAddress = new Address();
            fromAddress.setPlaceId(visitDTO.getFromPlaceId());
            fromAddress.setFormattedAddress(visitDTO.getFromFormattedAddress());
            addressRepo.save(fromAddress);
            visit.setFromAddress(fromAddress);
            // Set the travel duration, casting to int as the frontend sends it as a string
            visit.setTravelDuration(Integer.parseInt(String.valueOf(visitDTO.getTravelDuration())));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid meeting type");
        }

        // Save the visit in the database
        visitRepo.save(visit);
        Placement placementObj = placement.get();
        placementObj.getVisits().add(visit);
        placementRepo.save(placementObj);

        // Add the visit to the user's Microsoft Outlook calendar, if they have linked it
        if (calendarService.isCalendarLinked(session)) {
            try {
                calendarService.addVisitToCalendar(session, visit);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to add visit to calendar", e);
            }
        }

        // Create a notification for the student
        notificationService.createStudentNotification(visit.getPlacement().getStudent(), "Visit Scheduled", "Your tutor has scheduled a visit for your placement on " + visit.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yy")) + " at " + visit.getStartTime().toString() + ".", "/student/tutor", "View Visit");

        // Add success message as a flash attribute
        redirectAttributes.addFlashAttribute("successMessage", "New visit scheduled successfully.");

        return "redirect:/tutor/visits";
    }

    /**
     * Endpoint for tutor to cancel a visit
     *
     * @param id                 The ID of the visit to cancel
     * @param redirectAttributes The redirect attributes to add a success message
     * @return A redirect to the tutor visits page
     */
    @PostMapping("/tutor/visits/cancel")
    public String cancelVisit(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        Optional<Visit> visitOptional = visitRepo.findById(id);
        // Check if the visit exists
        if (visitOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Visit not found");
        }
        // Check if the visit status is upcoming, so it can be cancelled
        if (visitOptional.get().getStatus() == VisitStatus.UPCOMING) {
            // Set the visit to cancelled
            Visit visit = visitOptional.get();
            visit.setStatus(VisitStatus.CANCELLED);
            visitRepo.save(visit);

            // Create a notification for the student
            notificationService.createStudentNotification(visit.getPlacement().getStudent(), "Visit Cancelled", "Your tutor has cancelled the visit for your placement on " + visit.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yy")) + " at " + visit.getStartTime().toString() + ".", "/student/tutor", "View Visit");

            // Add success message as a flash attribute
            redirectAttributes.addFlashAttribute("successMessage", "Visit cancelled successfully.");

            return "redirect:/tutor/visits";
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Visit cannot be cancelled - invalid status");
        }
    }

    /**
     * AJAX endpoint for to get suggested visit times for a given week, based on the tutor's calendar
     *
     * @param visitSuggestionReqDTO The VisitSuggestionReqDTO object containing the proposed visit details
     * @param session               The session object to check if the tutor has linked their calendar
     * @return A JSON response containing the suggested visit times
     */
    @PostMapping("/tutor/visits/new/suggested-times/get")
    @ResponseBody
    public ResponseEntity<?> getSuggestedVisitTimes(@RequestBody VisitSuggestionReqDTO visitSuggestionReqDTO, HttpSession session) {
        /*
         * The system manually finds suggested times by getting all calendar events and checking for suitable free time slots.
         * If the system were to be used in a production environment, it would use Microsoft's FindMeetingTimes endpoint, which only supports Office 365 accounts, to find the top 3 suggested times accounting for both the student and the tutor's timetable.
         * In this scenario, I do not have the relevant permissions to use this endpoint in the University's domain, therefore the manual solution only takes in account the tutor's timetable events.
         */

        // Check if the calendar is linked
        if (!calendarService.isCalendarLinked(session)) {
            return ResponseEntity.status(401).body("Not authenticated with Outlook");
        }

        try {
            List<VisitSuggestionDTO> suggestedTimes = calendarService.getSuggestedVisitTimes(session, visitSuggestionReqDTO);
            Gson gson = new Gson();
            return ResponseEntity.ok(gson.toJson(suggestedTimes));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching suggested times: " + e.getMessage());
        }
    }
}