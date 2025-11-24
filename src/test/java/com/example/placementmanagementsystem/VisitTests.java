package com.example.placementmanagementsystem;

import com.example.placementmanagementsystem.controller.TutorController;
import com.example.placementmanagementsystem.dto.CalendarEventDTO;
import com.example.placementmanagementsystem.dto.VisitSuggestionDTO;
import com.example.placementmanagementsystem.dto.VisitSuggestionReqDTO;
import com.example.placementmanagementsystem.enumeration.PlacementStatus;
import com.example.placementmanagementsystem.enumeration.VisitStatus;
import com.example.placementmanagementsystem.model.Placement;
import com.example.placementmanagementsystem.model.Student;
import com.example.placementmanagementsystem.model.Visit;
import com.example.placementmanagementsystem.repository.PlacementRepo;
import com.example.placementmanagementsystem.repository.VisitRepo;
import com.example.placementmanagementsystem.service.CalendarService;
import com.example.placementmanagementsystem.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * JUnit tests for visit scheduling system endpoints
 */
@ExtendWith(MockitoExtension.class)
public class VisitTests {

    private MockMvc mockMvc;
    @InjectMocks
    private TutorController tutorController;
    @Mock
    private VisitRepo visitRepo;
    @Mock
    private PlacementRepo placementRepo;
    @Mock
    private CalendarService calendarService;
    @Mock
    private NotificationService notificationService;
    @Captor
    private ArgumentCaptor<Visit> visitCaptor;
    @InjectMocks
    private CalendarService injectedCalendarService;
    @Spy
    private CalendarService spyCalendarService;
    @Mock
    private VisitSuggestionReqDTO visitSuggestionReqDTO;
    @Mock
    private HttpSession session;

    /**
     * Set up the mock MVC by creating a standalone setup of the tutor controller
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(tutorController).build();
    }

    /**
     * Test the tutor submit visit endpoint
     * Checks if the visit is created correctly and all fields are set
     *
     * @throws Exception If an error occurs during the test
     */
    @Test
    void testTutorSubmitVisit() throws Exception {
        // Create mock placement
        Placement mockPlacement = new Placement();
        mockPlacement.setId(1L);
        mockPlacement.setStartDate(LocalDate.now().minusMonths(1));
        mockPlacement.setEndDate(LocalDate.now().plusMonths(1));
        mockPlacement.setStatus(PlacementStatus.IN_PROGRESS);

        // Create mock student
        Student mockStudent = new Student();
        mockStudent.setUsername("mockStudent");
        mockStudent.setFirstName("Mock");
        mockStudent.setLastName("Student");
        mockPlacement.setStudent(mockStudent);
        mockStudent.getPlacements().add(mockPlacement);

        // Create mock visit DTO attributes
        int meetingDuration = 60;
        String meetingType = "online";
        LocalDate date = LocalDate.now().plusWeeks(1);
        LocalTime time = LocalTime.of(10, 0, 0);
        int travelDuration = 0;

        // Mock repository methods
        when(placementRepo.findById(1L)).thenReturn(Optional.of(mockPlacement));

        // Perform the POST request
        mockMvc.perform(post("/tutor/visits/new/submit").param("placementId", String.valueOf(1L)).param("meetingDuration", String.valueOf(meetingDuration)).param("meetingType", meetingType).param("date", date.toString()).param("time", time.toString()).param("travelDuration", String.valueOf(travelDuration)).contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/tutor/visits"));

        // Verify the visit has been created correctly and fields are set
        verify(visitRepo).save(visitCaptor.capture());
        Visit savedVisit = visitCaptor.getValue();
        assertEquals(mockPlacement, savedVisit.getPlacement());
        assertEquals(VisitStatus.UPCOMING, savedVisit.getStatus());
        assertEquals(date, savedVisit.getDate());
        assertEquals(meetingDuration, savedVisit.getMeetingDuration());
        assertEquals(time, savedVisit.getStartTime());
        assertEquals(time.plusMinutes(meetingDuration), savedVisit.getEndTime());
        assertTrue(savedVisit.isOnline()); // Meeting type is online = true
        assertTrue(mockPlacement.getVisits().contains(savedVisit));
    }

    /**
     * Test the tutor cancel visit endpoint
     * Checks if the status of the visit is updated to cancelled
     *
     * @throws Exception If an error occurs during the test
     */
    @Test
    void testTutorCancelVisit() throws Exception {
        // Create mock visit
        Visit mockVisit = new Visit();
        mockVisit.setId(1L);
        mockVisit.setStatus(VisitStatus.UPCOMING);
        mockVisit.setDate(LocalDate.now().plusWeeks(1));
        mockVisit.setStartTime(LocalTime.of(10, 0, 0));

        // Create mock placement
        Placement mockPlacement = new Placement();
        mockPlacement.setId(1L);
        mockPlacement.setStartDate(LocalDate.now().minusMonths(1));
        mockPlacement.setEndDate(LocalDate.now().plusMonths(1));
        mockPlacement.setStatus(PlacementStatus.IN_PROGRESS);
        mockPlacement.getVisits().add(mockVisit);
        mockVisit.setPlacement(mockPlacement);

        // Create mock student
        Student mockStudent = new Student();
        mockStudent.setUsername("mockStudent");
        mockStudent.setFirstName("Mock");
        mockStudent.setLastName("Student");
        mockPlacement.setStudent(mockStudent);
        mockStudent.getPlacements().add(mockPlacement);

        // Mock repository methods
        when(visitRepo.findById(1L)).thenReturn(Optional.of(mockVisit));

        // Perform the POST request
        mockMvc.perform(post("/tutor/visits/cancel").param("id", String.valueOf(1L)).contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/tutor/visits"));

        // Verify the visit has been cancelled
        assertEquals(VisitStatus.CANCELLED, mockVisit.getStatus());
    }

    /**
     * Test the tutor schedule visit times endpoint
     * Checks if the correct number of suggestions are returned and that they are within working hours
     * Prints the suggestions to console, for next week and mocked calendar events
     */
    @Test
    void testSuggestedVisitTimes() {
        // Create mock visit suggestion request DTO for next week
        LocalDate nextMonday = LocalDate.now().plusWeeks(1).with(DayOfWeek.MONDAY);
        String nextWeek = nextMonday.format(DateTimeFormatter.ofPattern("YYYY-'W'ww")); // ISO week format
        int duration = 60;
        int travelDuration = 30;
        when(visitSuggestionReqDTO.getWeek()).thenReturn(nextWeek);
        when(visitSuggestionReqDTO.getDuration()).thenReturn(duration);
        when(visitSuggestionReqDTO.getTravelDuration()).thenReturn(travelDuration);
        when(visitSuggestionReqDTO.isOnline()).thenReturn(false);

        // Create dynamically calculated mock events for next week
        List<CalendarEventDTO> mockEvents = List.of(new CalendarEventDTO(1L, "Event 1", nextMonday.atTime(10, 0).toString(), nextMonday.atTime(11, 0).toString(), "Location 1"), // Monday 10am-11am
                new CalendarEventDTO(2L, "Event 2", nextMonday.atTime(12, 0).toString(), nextMonday.atTime(13, 0).toString(), "Location 2"), // Monday 12pm-1pm
                new CalendarEventDTO(3L, "Event 3", nextMonday.plusDays(1).atTime(10, 0).toString(), nextMonday.plusDays(1).atTime(12, 0).toString(), "Location 3"), // Tuesday 10am-12pm
                new CalendarEventDTO(4L, "Event 4", nextMonday.plusDays(1).atTime(14, 0).toString(), nextMonday.plusDays(1).atTime(15, 0).toString(), "Location 4"), // Tuesday 2pm-3pm
                new CalendarEventDTO(5L, "Event 5", nextMonday.plusDays(3).atTime(13, 0).toString(), nextMonday.plusDays(3).atTime(15, 0).toString(), "Location 5") // Thursday 1pm-3pm
        );

        // Mock getEventsInRange method to return the mock list of events
        doReturn(mockEvents).when(spyCalendarService).getEventsInRange(any(), any(), any());

        // Run the method to get suggested visit times
        List<VisitSuggestionDTO> suggestions = spyCalendarService.getSuggestedVisitTimes(session, visitSuggestionReqDTO);

        // Verify the correct number of suggestions are returned
        assertNotNull(suggestions);
        assertTrue(suggestions.size() <= 3, "Should return up to 3 suggestions");

        // Check that suggested times are within expected working hours
        for (VisitSuggestionDTO suggestion : suggestions) {
            LocalTime meetingTime = LocalTime.parse(suggestion.getMeetingTime());
            assertTrue(meetingTime.isAfter(LocalTime.of(8, 59)) && meetingTime.isBefore(LocalTime.of(17, 1)), "Suggested time should be within work hours (9 AM - 5 PM)");
        }

        // Print the suggestions to console
        System.out.println("\nSuggested Visit Times:");
        for (VisitSuggestionDTO suggestion : suggestions) {
            System.out.println("Date: " + suggestion.getDate() + ", Meeting Time: " + suggestion.getMeetingTime() + ", Travel Start: " + suggestion.getTravelStartTime());
        }
        System.out.println();
    }
}