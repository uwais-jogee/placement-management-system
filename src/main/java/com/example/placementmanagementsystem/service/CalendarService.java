package com.example.placementmanagementsystem.service;

import com.azure.core.credential.TokenCredential;
import com.example.placementmanagementsystem.dto.CalendarEventDTO;
import com.example.placementmanagementsystem.dto.VisitSuggestionDTO;
import com.example.placementmanagementsystem.dto.VisitSuggestionReqDTO;
import com.example.placementmanagementsystem.model.CalendarToken;
import com.example.placementmanagementsystem.model.Tutor;
import com.example.placementmanagementsystem.model.Visit;
import com.microsoft.graph.models.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import jakarta.servlet.http.HttpSession;
import kotlin.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.*;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for handling FullCalendar.js / Microsoft Calendar related operations
 */
@Service
public class CalendarService {

    @Autowired
    private UserService userService;

    /**
     * Save the calendar access token to the session
     *
     * @param session     The session to save the token to
     * @param accessToken The access token
     * @param expiresIn   The number of seconds until the token expires
     */
    public void saveCalendarToken(HttpSession session, String accessToken, int expiresIn) {
        Tutor tutor = userService.getCurrentLoggedInTutor();
        CalendarToken calendarToken = new CalendarToken();
        calendarToken.setTutorUsername(tutor.getUsername());
        calendarToken.setAccessToken(accessToken);
        calendarToken.setExpiresAt(LocalTime.now().plusSeconds(expiresIn));
        session.setAttribute("calendarToken", calendarToken);
    }

    /**
     * Get the calendar events in a given date range
     *
     * @param session The session containing the calendar token
     * @param start   The start of the date range
     * @param end     The end of the date range
     * @return A list of calendar events, in CalendarEventDTO format
     */
    public List<CalendarEventDTO> getEventsInRange(HttpSession session, LocalDateTime start, LocalDateTime end) {
        // Specify required scopes
        final String[] scopes = new String[]{"Calendars.ReadWrite"};

        // Create credential from access token
        String accessToken = ((CalendarToken) session.getAttribute("calendarToken")).getAccessToken();
        TokenCredential credential = request -> Mono.just(new com.azure.core.credential.AccessToken(accessToken, OffsetDateTime.now().plusHours(1)));

        // Initialise Graph client with credential and scopes
        final GraphServiceClient graphClient = new GraphServiceClient(credential, scopes);

        // Format dates for Graph API
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        String formattedStart = start.format(formatter);
        String formattedEnd = end.format(formatter);

        try {
            // Query calendar view
            EventCollectionResponse response = graphClient.me().calendar().calendarView().get(requestConfiguration -> {
                requestConfiguration.headers.add("Prefer", "outlook.timezone=\"Europe/London\"");
                requestConfiguration.queryParameters.startDateTime = formattedStart;
                requestConfiguration.queryParameters.endDateTime = formattedEnd;
                requestConfiguration.queryParameters.top = 999;
            });

            // Map each event to a DTO
            List<CalendarEventDTO> calendarEventDTOs = new ArrayList<>();
            if (response != null) {
                List<Event> events = response.getValue();
                for (Event event : events) {
                    calendarEventDTOs.add(new CalendarEventDTO(0L, event.getSubject(), event.getStart().getDateTime(), event.getEnd().getDateTime(), event.getLocation().getDisplayName()));
                }
            }
            return calendarEventDTOs;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching calendar events: " + e.getMessage(), e);
        }
    }

    /**
     * Check if the current logged-in user has linked their Microsoft Calendar, by verifying the calendar token in the session is valid
     *
     * @param session The current session
     * @return True if the user has linked their calendar, false otherwise
     */
    public boolean isCalendarLinked(HttpSession session) {
        // Get the calendar token from the session
        CalendarToken calendarToken = (CalendarToken) session.getAttribute("calendarToken");

        boolean hasLinkedCalendar = false;

        // Check if token exists
        if (calendarToken != null) {
            // Check if the token belongs to the current tutor
            if (calendarToken.getTutorUsername().equals(userService.getCurrentLoggedInTutor().getUsername())) {
                hasLinkedCalendar = true;
            }
            // Check if the token has expired
            if (calendarToken.getExpiresAt().isBefore(LocalTime.now())) {
                // Token has expired
                hasLinkedCalendar = false;
            }
        }
        return hasLinkedCalendar;
    }

    /**
     * Add a visit to the user's Microsoft Calendar.
     * If the visit is in-person, a travel event is also added.
     *
     * @param session The session containing the calendar token
     * @param visit   The visit to add to the calendar
     */
    public void addVisitToCalendar(HttpSession session, Visit visit) {
        // Specify required scopes
        final String[] scopes = new String[]{"Calendars.ReadWrite"};

        // Create credential from access token
        String accessToken = ((CalendarToken) session.getAttribute("calendarToken")).getAccessToken();
        TokenCredential credential = request -> Mono.just(new com.azure.core.credential.AccessToken(accessToken, OffsetDateTime.now().plusHours(1)));

        // Initialise Graph client with credential and scopes
        final GraphServiceClient graphClient = new GraphServiceClient(credential, scopes);

        // Create new event
        Event meetingEvent = new Event();
        meetingEvent.setSubject("Placement Visit - " + visit.getPlacement().getStudent().getFirstName() + " " + visit.getPlacement().getStudent().getLastName() + " - " + visit.getPlacement().getCompany().getCompanyName());
        // Start of visit
        meetingEvent.setStart(new com.microsoft.graph.models.DateTimeTimeZone());
        LocalDateTime start = visit.getDate().atTime(visit.getStartTime());
        meetingEvent.getStart().setDateTime(start.format(DateTimeFormatter.ISO_DATE_TIME));
        meetingEvent.getStart().setTimeZone("Europe/London");
        // End of visit
        LocalDateTime end = visit.getDate().atTime(visit.getEndTime());
        meetingEvent.setEnd(new com.microsoft.graph.models.DateTimeTimeZone());
        meetingEvent.getEnd().setDateTime(end.format(DateTimeFormatter.ISO_DATE_TIME));
        meetingEvent.getEnd().setTimeZone("Europe/London");
        // Location - online or in-person at company address
        meetingEvent.setLocation(new com.microsoft.graph.models.Location());
        if (visit.isOnline()) {
            meetingEvent.getLocation().setDisplayName("Online");
        } else {
            meetingEvent.getLocation().setDisplayName(visit.getPlacement().getCompany().getAddress().getFormattedAddress());
        }

        try {
            // Add event to calendar
            graphClient.me().events().post(meetingEvent);
        } catch (Exception e) {
            throw new RuntimeException("Error adding event to calendar: " + e.getMessage(), e);
        }

        // If the meeting is in-person, create a calendar event for the travel time
        if (!visit.isOnline()) {
            // Create new event
            Event travelEvent = new Event();
            travelEvent.setSubject("Travel - Placement Visit - " + visit.getPlacement().getStudent().getFirstName() + " " + visit.getPlacement().getStudent().getLastName() + " - " + visit.getPlacement().getCompany().getCompanyName());
            // Start of travel
            travelEvent.setStart(new com.microsoft.graph.models.DateTimeTimeZone());
            LocalDateTime travelStart = start.minusMinutes(visit.getTravelDuration());
            travelEvent.getStart().setDateTime(travelStart.format(DateTimeFormatter.ISO_DATE_TIME));
            travelEvent.getStart().setTimeZone("Europe/London");
            // End of travel
            travelEvent.setEnd(new com.microsoft.graph.models.DateTimeTimeZone());
            travelEvent.getEnd().setDateTime(start.format(DateTimeFormatter.ISO_DATE_TIME));
            travelEvent.getEnd().setTimeZone("Europe/London");

            try {
                // Add travel event to calendar
                graphClient.me().events().post(travelEvent);
            } catch (Exception e) {
                throw new RuntimeException("Error adding travel event to calendar: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Get the suggested visit times for a given week.
     * The algorithm finds available slots by manually iterating through the week's days and events.
     * Each potential slot is scored based on heuristics, and the top 3 are returned.
     * This algorithm finds the available slots by manually as opposed to using the Graph API's findMeetingTimes endpoint, as the API requires organisation-wide permissions, which I do not have for the scope of this project.
     *
     * @param session               The session containing the calendar token
     * @param visitSuggestionReqDTO The request DTO containing the placement ID, week, duration, online status, and travel duration
     * @return A list of VisitSuggestionDTOs containing the suggested visit times for the week
     */
    public List<VisitSuggestionDTO> getSuggestedVisitTimes(HttpSession session, VisitSuggestionReqDTO visitSuggestionReqDTO) {
        int meetingAndTravelDuration = visitSuggestionReqDTO.getDuration() + visitSuggestionReqDTO.getTravelDuration();

        // Parse the week and determine its start and end dates
        String week = visitSuggestionReqDTO.getWeek();
        int year = Integer.parseInt(week.split("-W")[0]);
        int weekNumber = Integer.parseInt(week.split("-W")[1]);
        LocalDate weekStart = LocalDate.of(year, 1, 1).with(DayOfWeek.MONDAY).plusWeeks(weekNumber - 1);
        LocalDate weekEnd = weekStart.with(DayOfWeek.FRIDAY);

        // Adjust start day if it is in the past
        if (weekStart.isBefore(LocalDate.now())) {
            weekStart = LocalDate.now();
            // Adjust week start to be the next day, if it is after 5pm
            if (LocalTime.now().plusMinutes(meetingAndTravelDuration).isAfter(LocalTime.of(17, 0))) {
                weekStart = weekStart.plusDays(1);
            }
            // Adjust week start to be the next Monday, if it is a weekend
            if (weekStart.getDayOfWeek() == DayOfWeek.SATURDAY || weekStart.getDayOfWeek() == DayOfWeek.SUNDAY) {
                weekStart = weekStart.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
                weekEnd = weekStart.with(DayOfWeek.FRIDAY);
            }
        }

        // Get all events in the week and group them by day
        List<CalendarEventDTO> events = getEventsInRange(session, weekStart.atStartOfDay(), weekEnd.atTime(23, 59, 59));
        Map<LocalDate, List<CalendarEventDTO>> eventsByDay = events.stream().collect(Collectors.groupingBy(event -> LocalDate.parse(event.getStart(), DateTimeFormatter.ISO_DATE_TIME)));

        List<Pair<VisitSuggestionDTO, Integer>> suggestions = new ArrayList<>();

        // Iterate through each day in the week and find free time slots
        for (LocalDate date = weekStart; !date.isAfter(weekEnd); date = date.plusDays(1)) {
            // Set work hours
            LocalTime workStart = LocalTime.of(9, 0);
            LocalTime workEnd = LocalTime.of(17, 0);
            // Adjust work hours if it is the current day
            if (date.equals(LocalDate.now()) && LocalTime.now().isAfter(workStart) && LocalTime.now().isBefore(workEnd)) {
                workStart = LocalTime.now();
            }

            // Get the events for the day
            List<CalendarEventDTO> dailyEvents = eventsByDay.getOrDefault(date, new ArrayList<>());

            // Sort events by start time
            dailyEvents.sort(Comparator.comparing(CalendarEventDTO::getStart));

            // Collect all free slots
            List<Pair<LocalTime, LocalTime>> freeTimeSlots = new ArrayList<>();
            LocalTime currentTime = workStart;

            for (CalendarEventDTO event : dailyEvents) {
                LocalTime eventStart = LocalTime.parse(event.getStart(), DateTimeFormatter.ISO_DATE_TIME);
                if (Duration.between(currentTime, eventStart).toMinutes() >= meetingAndTravelDuration) {
                    freeTimeSlots.add(new Pair<>(currentTime, eventStart));
                }
                currentTime = LocalTime.parse(event.getEnd(), DateTimeFormatter.ISO_DATE_TIME);
            }

            // Check remaining time after last event
            if (Duration.between(currentTime, workEnd).toMinutes() >= meetingAndTravelDuration) {
                freeTimeSlots.add(new Pair<>(currentTime, workEnd));
            }

            // Find the free slots that are long enough for the meeting
            for (Pair<LocalTime, LocalTime> slot : freeTimeSlots) {
                LocalTime slotStart = slot.getFirst();
                LocalTime slotEnd = slot.getSecond();

                // Try placing the meeting later in the slot
                for (LocalTime potentialStart = slotStart; !potentialStart.plusMinutes(meetingAndTravelDuration).isAfter(slotEnd); potentialStart = potentialStart.plusMinutes(15)) {  // Try every 15-minute step

                    LocalTime travelAdjustedStart = potentialStart.plusMinutes(visitSuggestionReqDTO.getTravelDuration());

                    VisitSuggestionDTO suggestion = new VisitSuggestionDTO(visitSuggestionReqDTO.getDuration(), visitSuggestionReqDTO.isOnline(), visitSuggestionReqDTO.getTravelDuration(), date.format(DateTimeFormatter.ISO_DATE), travelAdjustedStart.format(DateTimeFormatter.ISO_TIME), potentialStart.format(DateTimeFormatter.ISO_TIME));

                    // Score the suggestion based on heuristics
                    int score = scoreVisitSuggestion(potentialStart, slotStart, slotEnd, date, visitSuggestionReqDTO);
                    suggestions.add(new Pair<>(suggestion, score));
                }
            }
        }

        // Sort by best score and return the top 3 suggestions
        suggestions.sort(Comparator.comparing(Pair::getSecond, Comparator.reverseOrder()));
        List<VisitSuggestionDTO> top3Suggestions = new ArrayList<>(suggestions.stream().limit(3).map(Pair::getFirst).toList());
        // Return the top 3 in order of date and time
        top3Suggestions.sort(Comparator.comparing(VisitSuggestionDTO::getDate).thenComparing(VisitSuggestionDTO::getMeetingTime));
        return top3Suggestions;
    }

    /**
     * Score a visit suggestion using heuristics
     *
     * @param potentialStart The proposed start time of the meeting
     * @param slotStart      The start of the empty time slot
     * @param slotEnd        The end of the full time slot
     * @param date           The date of the meeting
     * @param request        The visit suggestion request
     * @return The score of the suggestion
     */
    private int scoreVisitSuggestion(LocalTime potentialStart, LocalTime slotStart, LocalTime slotEnd, LocalDate date, VisitSuggestionReqDTO request) {
        int score = 0;
        LocalTime potentialEnd = potentialStart.plusMinutes(request.getDuration() + request.getTravelDuration());

        // Buffer time before meeting
        if (potentialStart.isAfter(LocalTime.of(9, 0, 0))) { // If it is the first meeting of the day, a buffer before the meeting is not necessary
            if (potentialStart.compareTo(slotStart.plusMinutes(30)) >= 0) {
                score += 4; // At least 30-minute buffer after previous meeting
            } else if (potentialStart.compareTo(slotStart.plusMinutes(15)) >= 0) {
                score += 2; // At least 15-minute buffer after previous meeting
            } else {
                score -= 2; // Less than 15-minute gap after previous meeting
            }
        }

        // Buffer time after meeting
        if (potentialEnd.isBefore(LocalTime.of(17, 0, 0))) { // If the meeting is the last of the day, a buffer after the meeting is not necessary
            if (potentialEnd.compareTo(slotEnd.minusMinutes(30)) <= 0) {
                score += 4; // At least 30-minute buffer before next meeting
            } else if (potentialEnd.compareTo(slotEnd.minusMinutes(15)) <= 0) {
                score += 2; // At least 15-minute buffer before next meeting
            } else {
                score -= 2; // Less than 15-minute gap before next meeting
            }
        }

        // Travel Efficiency - Morning rush hour overlap
        if (!request.isOnline()) {
            LocalTime travelStart = potentialStart;
            LocalTime travelEnd = potentialStart.plusMinutes(request.getTravelDuration());
            LocalTime rushHourStart = LocalTime.of(8, 0);
            LocalTime rushHourEnd = LocalTime.of(9, 0);
            if ((travelStart.isBefore(rushHourEnd) && travelEnd.isAfter(rushHourStart))) {
                score -= 15; // Overlap with morning rush hour
            }
        }

        // Lunchtime Overlap (12:00 - 13:00)
        LocalTime lunchStart = LocalTime.of(12, 0);
        LocalTime lunchEnd = LocalTime.of(13, 0);
        if ((potentialStart.isBefore(lunchEnd) && potentialEnd.isAfter(lunchStart))) {
            score -= 15;
        }

        // Earlier in the week is better
        score += (6 - date.getDayOfWeek().getValue()) * 2; // Monday = 10, Friday = 2

        return score;
    }
}