package com.example.placementmanagementsystem.service;

import com.example.placementmanagementsystem.dto.UserDTO;
import com.example.placementmanagementsystem.enumeration.Role;
import com.example.placementmanagementsystem.model.*;
import com.example.placementmanagementsystem.repository.*;
import com.mailersend.sdk.exceptions.MailerSendException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for handling User entity related operations
 */
@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private TutorRepo tutorRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AdminRepo adminRepo;
    @Autowired
    private MessageRepo messageRepo;
    @Autowired
    private EmailService emailService;

    /**
     * Get the current logged in user
     *
     * @return User object of the current logged in user
     */
    public User getCurrentLoggedInUser() {
        return userRepo.findUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    /**
     * Get the current logged in student
     *
     * @return Student object of the current logged in student
     */
    public Student getCurrentLoggedInStudent() {
        return studentRepo.findStudentByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    /**
     * Get the current logged in tutor
     *
     * @return Tutor object of the current logged in tutor
     */
    public Tutor getCurrentLoggedInTutor() {
        return tutorRepo.findTutorByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    /**
     * Create a new user from the given UserDTO object.
     * Called when a User Manager creates a new user.
     *
     * @param userDTO UserDTO object containing the new user's details
     * @throws Exception If the role is invalid
     */
    public void createNewUser(UserDTO userDTO) throws Exception {
        // Create the specific user object depending on the role
        // Set the account to locked initially until the user activates the account
        switch (userDTO.getRole()) {
            case "ROLE_STUDENT":
                Student student = new Student();
                student.setUsername(userDTO.getUsername());
                student.setFirstName(userDTO.getFirstName());
                student.setLastName(userDTO.getLastName());
                student.setEmail(userDTO.getEmail());
                student.setAccountNonLocked(false);
                studentRepo.save(student);
                break;
            case "ROLE_TUTOR":
                Tutor tutor = new Tutor();
                tutor.setUsername(userDTO.getUsername());
                tutor.setFirstName(userDTO.getFirstName());
                tutor.setLastName(userDTO.getLastName());
                tutor.setEmail(userDTO.getEmail());
                tutor.setAccountNonLocked(false);
                tutorRepo.save(tutor);
                break;
            case "ROLE_ADMIN":
                Admin admin = new Admin();
                admin.setUsername(userDTO.getUsername());
                admin.setFirstName(userDTO.getFirstName());
                admin.setLastName(userDTO.getLastName());
                admin.setEmail(userDTO.getEmail());
                admin.setAccountNonLocked(false);
                adminRepo.save(admin);
                break;
            default:
                throw new Exception("Invalid role");
        }
    }

    /**
     * Change the password of the given user.
     * Checks if the old password is correct before changing the password.
     *
     * @param user        User object whose password is to be changed
     * @param oldPassword Old password of the user
     * @param newPassword New password of the user
     * @return true if the password is changed successfully, false otherwise
     */
    public boolean changeUserPassword(User user, String oldPassword, String newPassword) {
        if (passwordEncoder.matches(oldPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepo.save(user);
            return true;
        }
        return false;
    }

    /**
     * Scheduled job to send email reminders to users with unread messages.
     * The job is run at the specified cron expression in the application.properties file, and also when the application is started.
     * Calls the emailService method to send each email.
     */
    public void sendUnreadMessagesEmailReminders() {
        // Get all student and tutor users
        List<User> users = userRepo.findUsersByRoleIn(List.of(Role.ROLE_STUDENT, Role.ROLE_TUTOR));

        // For each user, check if they have unread messages
        for (User user : users) {
            List<Message> unreadMessages = messageRepo.findMessagesByReceiverAndIsRead(user, false);
            if (!unreadMessages.isEmpty()) {
                // Send an email reminder
                try {
                    emailService.sendUnreadMessagesEmail(unreadMessages.size(), user.getFirstName() + " " + user.getLastName(), user.getEmail(), user.getRole());
                } catch (MailerSendException e) {
                    System.err.println("Error sending email: " + user.getUsername());
                }
            }
        }

    }
}