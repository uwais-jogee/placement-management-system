package com.example.placementmanagementsystem;

import com.example.placementmanagementsystem.controller.UserManagerController;
import com.example.placementmanagementsystem.dto.UserDTO;
import com.example.placementmanagementsystem.model.User;
import com.example.placementmanagementsystem.repository.EmailTokenRepo;
import com.example.placementmanagementsystem.repository.UserRepo;
import com.example.placementmanagementsystem.service.EmailService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * JUnit tests for user management related endpoints
 */
@ExtendWith(MockitoExtension.class)
public class UserManagementTests {

    private MockMvc mockMvc;
    @InjectMocks
    private UserManagerController userManagerController;
    @Mock
    private UserRepo userRepo;
    @Mock
    private EmailTokenRepo emailTokenRepo;
    @Mock
    private EmailService emailService;
    @Mock
    private UserService userService;

    /**
     * Set up the mock MVC by creating a standalone setup of the user manager controller
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userManagerController).build();
    }

    /**
     * Test the endpoint for creating a new user successfully
     * Checks if the user was created and the email was sent
     *
     * @throws Exception If an error occurs during the test
     */
    @Test
    void testCreateUser() throws Exception {
        // Create mock user DTO
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("mockUser");
        userDTO.setFirstName("Mock");
        userDTO.setLastName("User");
        userDTO.setEmail("mockuser@pms.test");
        userDTO.setRole("ROLE_STUDENT");

        // Create mock user
        User user = new User();

        // Mock the repository methods
        when(userRepo.findUserByUsername(userDTO.getUsername())).thenReturn(null);
        when(userRepo.findUserByEmail(userDTO.getEmail())).thenReturn(null);
        doAnswer(invocation -> {
            // After userService.createNewUser(userDTO) is called, update the mock to return the new user
            when(userRepo.findUserByEmail(userDTO.getEmail())).thenReturn(user);
            return null;
        }).when(userService).createNewUser(any(UserDTO.class));

        doNothing().when(emailService).sendAccountActivationEmail(any(), any(), any(), any(), any());

        // Perform the POST request
        mockMvc.perform(post("/user-manager/user/new/submit").contentType(MediaType.APPLICATION_JSON).param("username", userDTO.getUsername()).param("firstName", userDTO.getFirstName()).param("lastName", userDTO.getLastName()).param("email", userDTO.getEmail()).param("role", userDTO.getRole()).contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/user-manager/user-management/all"));

        // Check if the user was created and the email was sent
        verify(userService, times(1)).createNewUser(any(UserDTO.class));
        verify(emailService, times(1)).sendAccountActivationEmail(any(), any(), any(), any(), any());
    }

    /**
     * Test the endpoint for enabling a user account
     * Checks if the user was enabled
     *
     * @throws Exception If an error occurs during the test
     */
    @Test
    void testEnableUser() throws Exception {
        // Create mock user
        User user = new User();
        user.setUsername("mockUser");
        user.setEnabled(false);

        // Mock the repository methods
        when(userRepo.findUserByUsername("mockUser")).thenReturn(user);

        // Perform the POST request
        mockMvc.perform(post("/user-manager/user/view/enable").contentType(MediaType.APPLICATION_JSON).param("username", user.getUsername()).contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/user-manager/user/view?username=" + user.getUsername()));

        // Check if the user was enabled
        assertTrue(user.isEnabled());
    }

    /**
     * Test the endpoint for disabling a user account
     * Checks if the user was disabled
     *
     * @throws Exception If an error occurs during the test
     */
    @Test
    void testDisableUser() throws Exception {
        // Create mock user
        User user = new User();
        user.setUsername("mockUser");
        user.setEnabled(true);

        // Mock the repository methods
        when(userRepo.findUserByUsername("mockUser")).thenReturn(user);

        // Perform the POST request
        mockMvc.perform(post("/user-manager/user/view/disable").contentType(MediaType.APPLICATION_JSON).param("username", user.getUsername()).contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/user-manager/user/view?username=" + user.getUsername()));

        // Check if the user was disabled
        assertFalse(user.isEnabled());
    }

    /**
     * Test the endpoint for deleting a user account
     * Checks if the user was deleted
     *
     * @throws Exception If an error occurs during the test
     */
    @Test
    void testDeleteUser() throws Exception {
        // Create mock user
        User user = new User();
        user.setUsername("mockUser");

        // Mock the repository methods
        when(userRepo.findUserByUsername("mockUser")).thenReturn(user);

        // Perform the POST request
        mockMvc.perform(post("/user-manager/user/view/delete").contentType(MediaType.APPLICATION_JSON).param("username", user.getUsername()).contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/user-manager/user-management/all"));

        // Check if the user was deleted
        verify(userRepo, times(1)).delete(user);
    }

    /**
     * Test the endpoint for editing a user account
     * Checks if the user attributes were updated correctly
     *
     * @throws Exception If an error occurs during the test
     */
    @Test
    void testEditUser() throws Exception {
        // Create mock user
        User user = new User();
        user.setUsername("mockUser");

        // Mock repository methods
        when(userRepo.findUserByUsername("mockUser")).thenReturn(user);

        // Perform the POST request
        mockMvc.perform(post("/user-manager/user/view/edit").contentType(MediaType.APPLICATION_JSON).param("username", user.getUsername()).param("firstName", "newFirstName").param("lastName", "newLastName").param("email", "newEmail@pms.test").contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/user-manager/user/view?username=" + user.getUsername()));

        // Check if the user attributes were updated
        assertEquals("newFirstName", user.getFirstName());
        assertEquals("newLastName", user.getLastName());
        assertEquals("newEmail@pms.test", user.getEmail());
    }
}