package com.example.placementmanagementsystem.controller;

import com.example.placementmanagementsystem.dto.UserDTO;
import com.example.placementmanagementsystem.enumeration.Role;
import com.example.placementmanagementsystem.model.EmailToken;
import com.example.placementmanagementsystem.model.User;
import com.example.placementmanagementsystem.repository.EmailTokenRepo;
import com.example.placementmanagementsystem.repository.UserRepo;
import com.example.placementmanagementsystem.service.EmailService;
import com.example.placementmanagementsystem.service.UserService;
import com.mailersend.sdk.exceptions.MailerSendException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller class for handling user-manager specific requests
 */
@Controller
public class UserManagerController {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private EmailTokenRepo emailTokenRepo;
    @Value("${HOST}")
    private String host;
    @Autowired
    private EmailService emailService;

    /**
     * User management dashboard endpoint
     *
     * @param model The model to add the user list and new user DTO to
     * @return The user management dashboard view
     */
    @GetMapping("/user-manager/user-management/{role}")
    public String userManagement(@PathVariable String role, Model model) {
        // Add a new user DTO to the model
        model.addAttribute("newUserDTO", new UserDTO());

        // Check the role in URL to filter the users
        switch (role.toLowerCase()) {
            case "student":
                model.addAttribute("users", userRepo.findUsersByRole(Role.ROLE_STUDENT));
                model.addAttribute("tab", "Student");
                break;
            case "tutor":
                model.addAttribute("users", userRepo.findUsersByRole(Role.ROLE_TUTOR));
                model.addAttribute("tab", "Tutor");
                break;
            case "admin":
                model.addAttribute("users", userRepo.findUsersByRole(Role.ROLE_ADMIN));
                model.addAttribute("tab", "Admin");
                break;
            case "all":
                // Add all the users to the model - not including the ROLE_USER_MANAGER
                model.addAttribute("users", userRepo.findUsersByRoleIn(List.of(Role.ROLE_STUDENT, Role.ROLE_TUTOR, Role.ROLE_ADMIN)));
                model.addAttribute("tab", "All");
                break;
            default:
                // If the role is invalid, return a Bad Request response
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to view user management - Invalid role");
        }
        return "userManager/userManagement";
    }

    /**
     * User-manager profile endpoint
     *
     * @return The user-manager profile view
     */
    @GetMapping("/user-manager/profile")
    public String profile() {
        return "userManager/profile";
    }

    /**
     * User manager submit change password endpoint
     *
     * @param currentPassword    The current password provided by the user
     * @param newPassword        The new password provided by the user
     * @param repeatNewPassword  The repeated new password provided by the user
     * @param redirectAttributes The redirect attributes to add flash attributes to
     * @return A redirect to the user manager profile page with a success message if the password was changed successfully, or an error message if the current password was incorrect
     */
    @PostMapping("/user-manager/profile/change-password")
    public String changePassword(@RequestParam String currentPassword, @RequestParam String newPassword, @RequestParam String repeatNewPassword, RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentLoggedInUser();
        // Check the new password and repeat new password match
        if (!newPassword.equals(repeatNewPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to change password. New password and repeat new password do not match.");
            return "redirect:/user-manager/profile";
        }
        if (userService.changeUserPassword(currentUser, currentPassword, newPassword)) {
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to change password. Incorrect current password.");
        }
        return "redirect:/user-manager/profile";
    }

    /**
     * View specific user details endpoint
     *
     * @param username The username of the user to view
     * @param model    The model to add the user details to
     * @return The view user view
     */
    @GetMapping("/user-manager/user/view")
    public String viewUser(@RequestParam String username, Model model) {
        // Check if the username exists in the database
        if (userRepo.findUserByUsername(username) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        // Add the user to the model
        model.addAttribute("user", userRepo.findUserByUsername(username));
        return "userManager/viewUser";
    }


    /**
     * AJAX request endpoint to validate the given username, to check if it is already in use.
     *
     * @param username The username to validate
     * @return ResponseEntity with the response JSON object, containing available: true/false
     */
    @ResponseBody
    @PostMapping("/user-manager/user/new/validate-username")
    public ResponseEntity<Map<String, Boolean>> validateUsername(@RequestParam String username) {
        Map<String, Boolean> response = new HashMap<>();
        // Check if the username is already in use
        boolean available = userRepo.findUserByUsername(username) == null;
        if (available) {
            response.put("available", true);
        } else {
            response.put("available", false);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * AJAX request endpoint to validate the given email, to check if it is already in use.
     *
     * @param email        The email to validate
     * @param currentEmail (Optional) The current email of the user, to exclude from the check
     * @return ResponseEntity with the response JSON object, containing available: true/false
     */
    @ResponseBody
    @PostMapping("/user-manager/user/new/validate-email")
    public ResponseEntity<Map<String, Boolean>> validateEmail(@RequestParam String email, @RequestParam(required = false) String currentEmail) {
        Map<String, Boolean> response = new HashMap<>();
        // Fetch the user by email
        User existingUser = userRepo.findUserByEmail(email);
        // Check if the email is in use AND it is not the current user's email
        boolean available = (existingUser == null || (currentEmail != null && currentEmail.equals(existingUser.getEmail())));
        response.put("available", available);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to create a new user
     * Validates the user DTO and creates a new user account.
     * Then sends the user an email to activate their account and set their password, with a tokenised link.
     *
     * @param userDTO            The user DTO containing the details to create the new user
     * @param redirectAttributes The redirect attributes to add a success message to
     * @return Redirect to the user management dashboard
     */
    @PostMapping("/user-manager/user/new/submit")
    public String createUser(@ModelAttribute UserDTO userDTO, RedirectAttributes redirectAttributes) {
        // Validate the user DTO
        // Check if any of the fields are empty
        if (userDTO.getUsername().isEmpty() || userDTO.getFirstName().isEmpty() || userDTO.getLastName().isEmpty() || userDTO.getEmail().isEmpty() || userDTO.getRole().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error creating new user - Some fields are empty");
        }
        // Check the username is not in use
        if (userRepo.findUserByUsername(userDTO.getUsername()) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error creating new user - Username already in use");
        }
        // Check the email is not in use
        if (userRepo.findUserByEmail(userDTO.getEmail()) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error creating new user - Email already in use");
        }

        try {
            // Create the new user account
            userService.createNewUser(userDTO);

            // Get the new user from the database
            User user = userRepo.findUserByEmail(userDTO.getEmail());

            // Create a new account activation token for the email link and assign it to the user
            EmailToken emailToken = new EmailToken(2880); // Token expires in 48 hours
            emailTokenRepo.save(emailToken);
            user.setAccountActivationEmailToken(emailToken);
            userRepo.save(user);

            // Define the email fields
            String toEmail = user.getEmail();
            String toName = user.getFirstName() + " " + user.getLastName();
            String link = host + "/activate-account?username=" + user.getUsername() + "&token=" + user.getAccountActivationEmailToken().getToken();
            String expiresIn = "48 hours";

            // Send the email
            emailService.sendAccountActivationEmail(toEmail, toName, user.getUsername(), link, expiresIn);

            // Add a success message as a flash attribute
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully. Account activation email sent.");
        } catch (MailerSendException mse) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email, error from MailerSend API: " + mse.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error creating new user - " + e.getMessage());
        }

        return "redirect:/user-manager/user-management/all";
    }

    /**
     * Endpoint to enable a user
     *
     * @param username           The username of the user to enable
     * @param redirectAttributes The redirect attributes to add a success message to
     * @return Redirect to the view user page
     */
    @PostMapping("/user-manager/user/view/enable")
    public String enableUser(@RequestParam String username, RedirectAttributes redirectAttributes) {
        User user = userRepo.findUserByUsername(username);
        // Check if the username exists in the database
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        // Enable the user
        user.setEnabled(true);
        userRepo.save(user);

        // Add a success message as a flash attribute
        redirectAttributes.addFlashAttribute("successMessage", "User enabled successfully.");

        return "redirect:/user-manager/user/view?username=" + username;
    }

    /**
     * Endpoint to disable a user
     *
     * @param username           The username of the user to disable
     * @param redirectAttributes The redirect attributes to add a success message to
     * @return Redirect to the view user page
     */
    @PostMapping("/user-manager/user/view/disable")
    public String disableUser(@RequestParam String username, RedirectAttributes redirectAttributes) {
        User user = userRepo.findUserByUsername(username);
        // Check if the username exists in the database
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        // Disable the user
        user.setEnabled(false);
        userRepo.save(user);

        // Add a success message as a flash attribute
        redirectAttributes.addFlashAttribute("successMessage", "User disabled successfully.");

        return "redirect:/user-manager/user/view?username=" + username;
    }

    /**
     * Endpoint to delete a user
     *
     * @param username           The username of the user to delete
     * @param redirectAttributes The redirect attributes to add a success message to
     * @return Redirect to the user management dashboard
     */
    @PostMapping("/user-manager/user/view/delete")
    public String deleteUser(@RequestParam String username, RedirectAttributes redirectAttributes) {
        User user = userRepo.findUserByUsername(username);
        // Check if the username exists in the database
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        // Delete the user
        userRepo.delete(user);

        // Add a success message as a flash attribute
        redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");

        return "redirect:/user-manager/user-management/all";
    }

    /**
     * Endpoint to edit a user's details
     * Checks if the username exists in the database, and if the email is already in use.
     * Then updates the user's details and redirects to the view user page.
     *
     * @param username           The username of the user to edit
     * @param firstName          The new first name
     * @param lastName           The new last name
     * @param email              The new email
     * @param redirectAttributes The redirect attributes to add a success message to
     * @return Redirect to the view user page
     */
    @PostMapping("/user-manager/user/view/edit")
    public String editUser(@RequestParam String username, @RequestParam String firstName, @RequestParam String lastName, @RequestParam String email, RedirectAttributes redirectAttributes) {
        User user = userRepo.findUserByUsername(username);
        // Check if the username exists in the database
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error editing user - User not found");
        }
        // Check if the email is already in use
        if (userRepo.findUserByEmail(email) != null && !user.getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error editing user - Email already in use");
        }

        // Update the user
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        userRepo.save(user);

        // Add a success message as a flash attribute
        redirectAttributes.addFlashAttribute("successMessage", "User updated successfully.");

        return "redirect:/user-manager/user/view?username=" + username;
    }

    @PostMapping("/user-manager/user/view/resend-activation-email")
    public String resendActivationEmail(@RequestParam String username, RedirectAttributes redirectAttributes) {
        User user = userRepo.findUserByUsername(username);
        // Check if the username exists in the database
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        // Check if the user account is locked/inactive
        if (user.isAccountNonLocked()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User account is already active");
        }
        try {
            // Delete the old account activation email token
            emailTokenRepo.delete(user.getAccountActivationEmailToken());

            // Create a new account activation token for the email link and assign it to the user
            EmailToken emailToken = new EmailToken(2880); // Token expires in 48 hours
            emailTokenRepo.save(emailToken);
            user.setAccountActivationEmailToken(emailToken);
            userRepo.save(user);

            // Define the email fields
            String toEmail = user.getEmail();
            String toName = user.getFirstName() + " " + user.getLastName();
            String link = host + "/activate-account?username=" + user.getUsername() + "&token=" + user.getAccountActivationEmailToken().getToken();
            String expiresIn = "48 hours";

            // Send the email
            emailService.sendAccountActivationEmail(toEmail, toName, user.getUsername(), link, expiresIn);
        } catch (MailerSendException mse) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email, error from MailerSend API: " + mse.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error resending activation email - " + e.getMessage());
        }

        // Add a success message as a flash attribute
        redirectAttributes.addFlashAttribute("successMessage", "Account activation email sent.");

        return "redirect:/user-manager/user/view?username=" + username;
    }
}