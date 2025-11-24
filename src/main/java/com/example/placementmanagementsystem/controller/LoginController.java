package com.example.placementmanagementsystem.controller;

import com.example.placementmanagementsystem.model.EmailToken;
import com.example.placementmanagementsystem.model.User;
import com.example.placementmanagementsystem.repository.EmailTokenRepo;
import com.example.placementmanagementsystem.repository.UserRepo;
import com.example.placementmanagementsystem.service.EmailService;
import com.mailersend.sdk.exceptions.MailerSendException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Controller for handling login and redirection to correct dashboard
 */
@Controller
public class LoginController {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private EmailService emailService;
    @Autowired
    private EmailTokenRepo emailTokenRepo;
    @Value("${HOST}")
    private String host;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Redirects the application root to the correct dashboard based on the user's role, if authenticated.
     * If the user is not authenticated, redirects to the login page.
     *
     * @return Redirect to the correct dashboard or login page
     */
    @GetMapping("/")
    public String rootDashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {
            return "redirect:/student/dashboard";
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TUTOR"))) {
            return "redirect:/tutor/dashboard";
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER_MANAGER"))) {
            return "redirect:/user-manager/user-management/all";
        }
        return "redirect:/login";
    }

    /**
     * Login endpoint
     *
     * @return Login page view
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Forgot password endpoint
     *
     * @return Forgot password page view
     */
    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgotPassword";
    }

    /**
     * Submit forgot password form
     * Sends a tokenised reset link to the email address provided, if it exists in the database
     *
     * @param email Email address to send reset link to
     * @return Redirect to login page with reset parameter
     */
    @PostMapping("/forgot-password/request-reset-link")
    public String requestResetLink(@RequestParam String email) {
        User user = userRepo.findUserByEmail(email);
        if (user != null) {
            // If the user already has a password reset token that is valid, do not send another email
            if (user.getPasswordResetEmailToken() != null && user.getPasswordResetEmailToken().getExpiresAt().isAfter(LocalDateTime.now())) {
                return "redirect:/login?reset=requested-previously";
            }
            // If the user has not activated their account, and it is locked, do not send another email
            if (!user.isAccountNonLocked()) {
                return "redirect:/login?reset=account-inactive";
            }
            try {
                // Create a new password reset token for the email link and assign it to the user
                EmailToken emailToken = new EmailToken(5);
                emailTokenRepo.save(emailToken);
                user.setPasswordResetEmailToken(emailToken);
                userRepo.save(user);

                // Define the email fields
                String toEmail = user.getEmail();
                String toName = user.getFirstName() + " " + user.getLastName();
                String link = host + "/reset-password?username=" + user.getUsername() + "&token=" + user.getPasswordResetEmailToken().getToken();
                String expiresIn = "5 minutes";

                // Send the email
                emailService.sendPasswordResetEmail(toEmail, toName, link, expiresIn);
            } catch (MailerSendException mse) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email, error from MailerSend API: " + mse.getMessage());
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email: " + e.getMessage());
            }
        }
        return "redirect:/login?reset=requested";
    }

    /**
     * Reset password endpoint
     * Checks if the token is valid and if the username matches the token
     * Shows the reset password form if the token and username are valid
     *
     * @param token    The token from the URL
     * @param username The username from the URL
     * @param model    The model to pass the token and username to the view
     * @return The reset password form view
     */
    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam String token, @RequestParam String username, Model model) {
        Optional<EmailToken> emailToken = emailTokenRepo.findByToken(token);
        if (emailToken.isPresent()) {
            // Check if the token is valid
            if (emailToken.get().getExpiresAt().isAfter(LocalDateTime.now())) {
                // Check if the given username from the URL is the same as the one in the user object related to the token
                User user = userRepo.findUserByPasswordResetEmailToken(emailToken.get());
                if (user == null) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not found");
                }
                if (!user.getUsername().equals(username)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Username does not match token");
                }
                // Show the reset password form, passing the token and username back to the model
                model.addAttribute("token", token);
                model.addAttribute("username", username);
                return "resetPassword";
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token has expired");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token not found");
        }
    }

    /**
     * Endpoint for submitting the reset password form
     *
     * @param token          The token from the URL
     * @param password       The new password from the form
     * @param repeatPassword The repeated new password from the form
     * @return Redirect to login page with reset success parameter
     */
    @PostMapping("/reset-password/submit")
    public String submitResetPassword(@RequestParam String token, @RequestParam String password, @RequestParam String repeatPassword) {
        Optional<EmailToken> emailToken = emailTokenRepo.findByToken(token);
        if (emailToken.isPresent()) {
            EmailToken emailTokenObj = emailToken.get();
            // Check if the token is valid
            if (emailTokenObj.getExpiresAt().isAfter(LocalDateTime.now())) {
                User user = userRepo.findUserByPasswordResetEmailToken(emailTokenObj);
                // Check if the user exists
                if (user == null) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not found");
                }
                // Check if the passwords match
                if (!password.equals(repeatPassword)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Passwords do not match");
                }

                // Update the user's password
                user.setPassword(passwordEncoder.encode(password));
                userRepo.save(user);

                // Delete the token
                user.setPasswordResetEmailToken(null);
                userRepo.save(user);
                emailTokenRepo.delete(emailTokenObj);
                return "redirect:/login?reset=success";
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token has expired");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token not found");
        }
    }

    /**
     * Account activation endpoint
     * Checks if the token is valid and if the username matches the token
     * Shows the account activation form if the token and username are valid
     *
     * @param token    The token from the URL
     * @param username The username from the URL
     * @param model    The model to pass the token and username to the view
     * @return The account activation form view
     */
    @GetMapping("/activate-account")
    public String activateAccount(@RequestParam String token, @RequestParam String username, Model model) {
        Optional<EmailToken> emailToken = emailTokenRepo.findByToken(token);
        if (emailToken.isPresent()) {
            // Check if the token is valid
            if (emailToken.get().getExpiresAt().isAfter(LocalDateTime.now())) {
                // Check if the given username from the URL is the same as the one in the user object related to the token
                User user = userRepo.findUserByAccountActivationEmailToken(emailToken.get());
                if (user == null) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not found");
                }
                if (!user.getUsername().equals(username)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Username does not match token");
                }
                // Show the account activation form, passing the token and username back to the model
                model.addAttribute("token", token);
                model.addAttribute("username", username);
                return "accountActivation";
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token has expired");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token not found");
        }
    }

    /**
     * Endpoint for submitting the account activation form
     *
     * @param token          The token from the URL
     * @param password       The new password from the form
     * @param repeatPassword The repeated new password from the form
     * @return Redirect to login page with activation success parameter
     */
    @PostMapping("/activate-account/submit")
    public String submitActivateAccount(@RequestParam String token, @RequestParam String password, @RequestParam String repeatPassword) {
        Optional<EmailToken> emailToken = emailTokenRepo.findByToken(token);
        if (emailToken.isPresent()) {
            EmailToken emailTokenObj = emailToken.get();
            // Check if the token is valid
            if (emailTokenObj.getExpiresAt().isAfter(LocalDateTime.now())) {
                User user = userRepo.findUserByAccountActivationEmailToken(emailTokenObj);
                // Check if the user exists
                if (user == null) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not found");
                }
                // Check if the passwords match
                if (!password.equals(repeatPassword)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Passwords do not match");
                }

                // Update the user's password and unlock the account
                user.setPassword(passwordEncoder.encode(password));
                user.setAccountNonLocked(true);
                userRepo.save(user);

                // Delete the token
                user.setAccountActivationEmailToken(null);
                userRepo.save(user);
                emailTokenRepo.delete(emailTokenObj);
                return "redirect:/login?activation=success";
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token has expired");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token not found");
        }
    }
}