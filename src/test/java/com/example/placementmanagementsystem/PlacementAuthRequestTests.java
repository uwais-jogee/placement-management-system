package com.example.placementmanagementsystem;

import com.example.placementmanagementsystem.controller.AdminController;
import com.example.placementmanagementsystem.controller.CompanyController;
import com.example.placementmanagementsystem.enumeration.PlacementAuthRequestStatus;
import com.example.placementmanagementsystem.model.*;
import com.example.placementmanagementsystem.repository.EmailTokenRepo;
import com.example.placementmanagementsystem.repository.PlacementAuthRequestRepo;
import com.example.placementmanagementsystem.repository.PlacementRepo;
import com.example.placementmanagementsystem.repository.TutorRepo;
import com.example.placementmanagementsystem.service.EmailService;
import com.example.placementmanagementsystem.service.NotificationService;
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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * JUnit tests for PlacementAuthRequest related endpoints
 */
@ExtendWith(MockitoExtension.class)
public class PlacementAuthRequestTests {

    private MockMvc mockMvc;
    @Mock
    private PlacementAuthRequestRepo placementAuthRequestRepo;
    @Mock
    private EmailTokenRepo emailTokenRepo;
    @Mock
    private NotificationService notificationService;
    @Mock
    private EmailService emailService;
    @InjectMocks
    private AdminController adminController;
    @InjectMocks
    private CompanyController companyController;
    @Mock
    private TutorRepo tutorRepo;
    @Mock
    private PlacementRepo placementRepo;

    /**
     * Set up the mock MVC by creating a standalone setup of the required controllers
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController, companyController).build();
    }

    /**
     * Test the admin reject auth request endpoint
     * Checks that the status is updated correctly
     *
     * @throws Exception If an error occurs during the test
     */
    @Test
    void testAdminReject() throws Exception {
        // Create a mock placement request
        PlacementAuthRequest mockRequest = new PlacementAuthRequest();
        mockRequest.setId(1L);
        mockRequest.setStatus(PlacementAuthRequestStatus.PENDING_INITIAL_ADMIN_APPROVAL);

        // Mock the repository method
        when(placementAuthRequestRepo.findById(1L)).thenReturn(Optional.of(mockRequest));

        // Perform a POST request to reject the placement request
        mockMvc.perform(post("/admin/auth-request/reject").param("id", "1").param("rejectionReason", "Insufficient details").contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/auth-request/view?id=1"));

        // Assert that the status has been updated correctly
        assertEquals(PlacementAuthRequestStatus.REJECTED_INITIAL_BY_ADMIN, mockRequest.getStatus());
    }

    /**
     * Test the admin initial approval endpoint
     * Checks that the status is updated and the email is sent
     *
     * @throws Exception If an error occurs during the test
     */
    @Test
    void testAdminInitialApproval() throws Exception {
        // Create a mock placement auth request
        PlacementAuthRequest mockRequest = new PlacementAuthRequest();
        mockRequest.setId(1L);
        mockRequest.setStatus(PlacementAuthRequestStatus.PENDING_INITIAL_ADMIN_APPROVAL);
        mockRequest.setCompanyNameOther("Mock Company");
        mockRequest.setCompanyContactEmail("placementmanagementsys@gmail.com");
        mockRequest.setCompanyContactName("Mock Company Contact Name");

        // Create a mock student
        Student mockStudent = new Student();
        mockStudent.setUsername("mockStudent");
        mockStudent.setFirstName("Mock");
        mockStudent.setLastName("Student");
        mockRequest.setStudent(mockStudent);

        // Mock repository methods
        when(placementAuthRequestRepo.findById(1L)).thenReturn(Optional.of(mockRequest));

        // Mock the email service (so the email isn't actually sent)
        doNothing().when(emailService).sendEmailToCompany(any(), any(), any(), any(), any(), any());

        // Perform the POST request
        mockMvc.perform(post("/admin/auth-request/initial-approval").param("id", "1").contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/auth-request/view?id=1"));

        // Assert that the status has been updated correctly
        assertEquals(PlacementAuthRequestStatus.PENDING_COMPANY_APPROVAL, mockRequest.getStatus());

        // Verify that the email sending method was called (but not actually sending the email)
        verify(emailService, times(1)).sendEmailToCompany(any(), any(), any(), any(), any(), any());
    }

    /**
     * Test the admin final approval endpoint
     * Checks the status is updated correctly and that the associations are correct
     *
     * @throws Exception If an error occurs during the test
     */
    @Test
    void testAdminFinalApproval() throws Exception {
        // Create a mock placement auth request
        PlacementAuthRequest mockRequest = new PlacementAuthRequest();
        mockRequest.setId(1L);
        mockRequest.setStatus(PlacementAuthRequestStatus.PENDING_FINAL_ADMIN_APPROVAL);
        mockRequest.setPlacementStartDate(LocalDate.now());
        mockRequest.setPlacementEndDate(LocalDate.now().plusDays(365));

        // Create mock student
        Student mockStudent = new Student();
        mockStudent.setUsername("mockStudent");
        mockStudent.setFirstName("Mock");
        mockStudent.setLastName("Student");
        mockRequest.setStudent(mockStudent);

        // Create mock tutor
        Tutor mockTutor = new Tutor();
        mockTutor.setUsername("mockTutor");
        mockTutor.setFirstName("Mock");
        mockTutor.setLastName("Tutor");

        // Create mock company
        Company mockCompany = new Company();
        mockCompany.setCompanyName("Mock Company");
        mockRequest.setCompany(mockCompany);

        // Mock repository methods
        when(placementAuthRequestRepo.findById(1L)).thenReturn(Optional.of(mockRequest));
        when(tutorRepo.findTutorByUsername("mockTutor")).thenReturn(mockTutor);

        // Perform the POST request
        mockMvc.perform(post("/admin/auth-request/final-approval").param("id", "1").param("tutorUsername", "mockTutor").contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/auth-request/view?id=1"));

        // Verify the placement status and associations
        assertEquals(PlacementAuthRequestStatus.APPROVED, mockRequest.getStatus());
        assertFalse(mockStudent.getPlacements().isEmpty());
        assertFalse(mockTutor.getPlacements().isEmpty());
        assertFalse(mockCompany.getPlacements().isEmpty());
    }

    /**
     * Test the company approve auth request endpoint
     * Checks that the status is updated correctly and the token is removed
     *
     * @throws Exception If an error occurs during the test
     */
    @Test
    void testCompanyApproval() throws Exception {
        // Create a mock placement auth request
        PlacementAuthRequest mockRequest = new PlacementAuthRequest();
        mockRequest.setId(1L);
        mockRequest.setStatus(PlacementAuthRequestStatus.PENDING_COMPANY_APPROVAL);

        // Create mock email token
        EmailToken mockToken = new EmailToken();
        mockToken.setToken("mock-token-value");
        mockToken.setExpiresAt(LocalDateTime.now().plusDays(1));
        mockRequest.setEmailToken(mockToken);

        // Mock repository methods
        when(placementAuthRequestRepo.findById(1L)).thenReturn(Optional.of(mockRequest));
        when(emailTokenRepo.findByToken("mock-token-value")).thenReturn(Optional.of(mockToken));

        // Perform the POST request
        mockMvc.perform(post("/company/auth-request/company-approve").param("id", "1").param("token", "mock-token-value").contentType(MediaType.APPLICATION_FORM_URLENCODED));

        // Verify the status has been updated and the token has been removed
        assertEquals(PlacementAuthRequestStatus.PENDING_FINAL_ADMIN_APPROVAL, mockRequest.getStatus());
        assertNull(mockRequest.getEmailToken());
    }

    /**
     * Test the company reject auth request endpoint
     * Checks that the status is updated correctly and the token is removed
     *
     * @throws Exception If an error occurs during the test
     */
    @Test
    void testCompanyReject() throws Exception {
        // Create a mock placement auth request
        PlacementAuthRequest mockRequest = new PlacementAuthRequest();
        mockRequest.setId(1L);
        mockRequest.setStatus(PlacementAuthRequestStatus.PENDING_COMPANY_APPROVAL);

        // Create mock email token
        EmailToken mockToken = new EmailToken();
        mockToken.setToken("mock-token-value");
        mockToken.setExpiresAt(LocalDateTime.now().plusDays(1));
        mockRequest.setEmailToken(mockToken);

        // Mock repository methods
        when(placementAuthRequestRepo.findById(1L)).thenReturn(Optional.of(mockRequest));
        when(emailTokenRepo.findByToken("mock-token-value")).thenReturn(Optional.of(mockToken));

        // Perform the POST request
        String rejectionReason = "Mock rejection reason";
        mockMvc.perform(post("/company/auth-request/company-reject").param("id", "1").param("token", "mock-token-value").param("rejectionReason", rejectionReason).contentType(MediaType.APPLICATION_FORM_URLENCODED));

        // Verify the status has been updated and the token has been removed
        assertEquals(PlacementAuthRequestStatus.REJECTED_BY_COMPANY, mockRequest.getStatus());
        assertEquals(rejectionReason, mockRequest.getRejectionReason());
        assertNull(mockRequest.getEmailToken());
    }
}