package com.example.placementmanagementsystem.service;

import com.example.placementmanagementsystem.model.Notification;
import com.example.placementmanagementsystem.model.Student;
import com.example.placementmanagementsystem.model.Tutor;
import com.example.placementmanagementsystem.model.User;
import com.example.placementmanagementsystem.repository.NotificationRepo;
import com.example.placementmanagementsystem.repository.StudentRepo;
import com.example.placementmanagementsystem.repository.TutorRepo;
import com.mailersend.sdk.exceptions.MailerSendException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service class for notification related operations
 */
@Service
public class NotificationService {

    @Autowired
    private NotificationRepo notificationRepo;
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private TutorRepo tutorRepo;
    @Autowired
    private EmailService emailService;

    /**
     * Create a new notification for a student and save it to the database
     * Sends an email to the student to notify them of the new notification
     *
     * @param student  The student to create the notification for
     * @param title    The title of the notification
     * @param message  The message of the notification
     * @param link     The link from the notification to the relevant page
     * @param linkText The text to display on the link
     */
    public void createStudentNotification(Student student, String title, String message, String link, String linkText) {
        Notification newNotification = new Notification(title, message, link, linkText);
        notificationRepo.save(newNotification);
        student.getNotifications().add(newNotification);
        studentRepo.save(student);
        try {
            emailService.sendNewNotificationEmail(title, message, link, linkText, student.getEmail(), student.getFirstName() + " " + student.getLastName());
        } catch (MailerSendException e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }

    /**
     * Create a new notification for a tutor and save it to the database
     * Sends an email to the tutor to notify them of the new notification
     *
     * @param tutor    The tutor to create the notification for
     * @param title    The title of the notification
     * @param message  The message of the notification
     * @param link     The link from the notification to the relevant page
     * @param linkText The text to display on the link
     */
    public void createTutorNotification(Tutor tutor, String title, String message, String link, String linkText) {
        Notification newNotification = new Notification(title, message, link, linkText);
        notificationRepo.save(newNotification);
        tutor.getNotifications().add(newNotification);
        tutorRepo.save(tutor);
        try {
            emailService.sendNewNotificationEmail(title, message, link, linkText, tutor.getEmail(), tutor.getFirstName() + " " + tutor.getLastName());
        } catch (MailerSendException e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }

    /**
     * Endpoint for when a user acknowledges a notification
     *
     * @param notificationId The ID of the notification to acknowledge
     * @return True if the notification was acknowledged successfully, false if the notification was not found
     */
    public boolean acknowledgeNotification(User user, Long notificationId) {
        Optional<Notification> notificationOptional = notificationRepo.findById(notificationId);
        if (notificationOptional.isPresent()) {
            Notification notification = notificationOptional.get();
            user.getNotifications().remove(notification);
            notificationRepo.delete(notification);
            return true;
        }
        return false;
    }
}