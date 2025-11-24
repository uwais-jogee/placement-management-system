package com.example.placementmanagementsystem;

import com.example.placementmanagementsystem.controller.AdminController;
import com.example.placementmanagementsystem.controller.StudentController;
import com.example.placementmanagementsystem.enumeration.PlacementStatus;
import com.example.placementmanagementsystem.model.Company;
import com.example.placementmanagementsystem.model.Placement;
import com.example.placementmanagementsystem.model.Student;
import com.example.placementmanagementsystem.model.Tutor;
import com.example.placementmanagementsystem.repository.PlacementRepo;
import com.example.placementmanagementsystem.repository.TutorRepo;
import com.example.placementmanagementsystem.service.NotificationService;
import com.example.placementmanagementsystem.service.PlacementService;
import com.example.placementmanagementsystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * JUnit tests for Placement related endpoints
 */
@ExtendWith(MockitoExtension.class)
public class PlacementTests {

    private MockMvc mockMvc;
    @InjectMocks
    private AdminController adminController;
    @InjectMocks
    private StudentController studentController;
    @Mock
    private NotificationService notificationService;
    @Mock
    private PlacementRepo placementRepo;
    @Mock
    private TutorRepo tutorRepo;
    @Mock
    private PlacementService placementService;
    @Mock
    private UserService userService;

    /**
     * Set up the mock MVC by creating a standalone setup of the required controllers
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController, studentController).build();
    }

    /**
     * Test the admin change placement tutor endpoint
     * Checks the tutor is updated correctly and the old tutor is removed from the placement
     *
     * @throws Exception If an error occurs during the test
     */
    @Test
    void testAdminChangePlacementTutor() throws Exception {
        // Create mock placement
        Placement mockPlacement = new Placement();
        mockPlacement.setId(1L);

        // Create mock company
        Company mockCompany = new Company();
        mockCompany.setCompanyName("Mock Company");
        mockPlacement.setCompany(mockCompany);

        // Create mock student
        Student mockStudent = new Student();
        mockStudent.setUsername("mockStudent");
        mockStudent.setFirstName("Mock");
        mockStudent.setLastName("Student");
        mockPlacement.setStudent(mockStudent);
        mockStudent.getPlacements().add(mockPlacement);

        // Create mock old tutor
        Tutor mockOldTutor = new Tutor();
        mockOldTutor.setUsername("mockOldTutor");
        mockOldTutor.setFirstName("Mock");
        mockOldTutor.setLastName("OldTutor");
        mockPlacement.setTutor(mockOldTutor);
        mockOldTutor.getPlacements().add(mockPlacement);

        // Create mock new tutor
        Tutor mockNewTutor = new Tutor();
        mockNewTutor.setUsername("mockNewTutor");
        mockNewTutor.setFirstName("Mock");
        mockNewTutor.setLastName("NewTutor");

        // Mock repository methods
        when(placementRepo.findById(1L)).thenReturn(Optional.of(mockPlacement));
        when(tutorRepo.findTutorByUsername("mockNewTutor")).thenReturn(mockNewTutor);

        // Perform the POST request
        mockMvc.perform(post("/admin/placement/change-tutor").param("placementId", "1").param("newTutorUsername", "mockNewTutor").contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/placement/view?id=1"));

        // Verify the placement tutor has been updated
        assertEquals(mockNewTutor, mockPlacement.getTutor());
        assertEquals(mockPlacement, mockNewTutor.getPlacements().get(0));
        assertEquals(0, mockOldTutor.getPlacements().size());
    }

    /**
     * Test the student update placement dates endpoint
     * Checks the placement dates are updated correctly
     *
     * @throws Exception If an error occurs during the test
     */
    @Test
    void testStudentUpdatePlacementDates() throws Exception {
        // Test changing the end date while the placement is in progress
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate oldEndDate = LocalDate.now().plusMonths(1);
        LocalDate newEndDate = LocalDate.now().plusMonths(2);

        // Create mock placement
        Placement mockPlacement = new Placement();
        mockPlacement.setId(1L);
        mockPlacement.setStartDate(startDate);
        mockPlacement.setEndDate(oldEndDate);
        mockPlacement.setStatus(PlacementStatus.IN_PROGRESS);

        // Create mock student
        Student mockStudent = new Student();
        mockStudent.setUsername("mockStudent");
        mockStudent.setFirstName("Mock");
        mockStudent.setLastName("Student");
        mockStudent.getPlacements().add(mockPlacement);
        mockPlacement.setStudent(mockStudent);

        // Mock repository methods
        when(userService.getCurrentLoggedInStudent()).thenReturn(mockStudent);
        when(placementService.getStudentCurrentPlacement(mockStudent)).thenReturn(mockPlacement);

        // Perform the POST request to update the end date
        mockMvc.perform(post("/student/placement/update-start-end-date/submit").param("newStartDate", startDate.toString()).param("newEndDate", newEndDate.toString()).contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/student/placement"));

        // Verify the placement dates have been updated
        assertEquals(startDate, mockPlacement.getStartDate());
        assertEquals(newEndDate, mockPlacement.getEndDate());
    }
}