package com.example.placementmanagementsystem.service;

import com.example.placementmanagementsystem.enumeration.Role;
import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.MailerSendResponse;
import com.mailersend.sdk.Recipient;
import com.mailersend.sdk.emails.Email;
import com.mailersend.sdk.exceptions.MailerSendException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service class to send emails using MailerSend SDK
 */
@Service
public class EmailService {

    // Inject the API values from application.properties
    @Value("${mailersend.from-email}")
    private String fromEmail;
    @Value("${mailersend.api-key}")
    private String apiKey;
    @Value("${mailersend.template.company-approval}")
    private String companyApprovalTemplateId;
    @Value("${mailersend.template.reset-password}")
    private String resetPasswordTemplateId;
    @Value("${mailersend.template.account-activation}")
    private String accountActivationTemplateId;
    @Value("${HOST}")
    private String host;

    /**
     * Send an email for the company to authorise a placement, using MailerSend SDK
     *
     * @param toEmail     The email address of the recipient company contact
     * @param toName      The name of the recipient company contact
     * @param link        The link to authorise the placement
     * @param validTill   The date until which the link is valid, in format "dd MMMM yyyy at HH:mm"
     * @param companyName The name of the company
     * @param studentName The student's full name
     * @throws MailerSendException If there is an error sending the email, MailerSend SDK throws a custom exception
     */
    public void sendEmailToCompany(String toEmail, String toName, String link, String validTill, String companyName, String studentName) throws MailerSendException {
        // Create the email object and set the from, to, and subject fields
        Email email = new Email();
        email.setFrom("Placement Management System", fromEmail);
        Recipient recipient = new Recipient(toName, toEmail);
        email.recipients.add(recipient);
        email.setSubject("Placement Approval Request - " + studentName);

        // Select the template created in MailerSend, and set the dynamic variables
        email.setTemplateId(companyApprovalTemplateId);
        email.addPersonalization(recipient, "link", link);
        email.addPersonalization(recipient, "toName", toName);
        email.addPersonalization(recipient, "validTill", validTill);
        email.addPersonalization(recipient, "companyName", companyName);
        email.addPersonalization(recipient, "studentName", studentName);

        // Send the email
        System.out.println("Email sending...: " + email);
        MailerSend ms = new MailerSend();
        ms.setToken(apiKey);
        MailerSendResponse response = ms.emails().send(email);
        System.out.println("Email sent successfully - Response: " + response.responseStatusCode);
    }

    /**
     * Send a password reset email using MailerSend SDK
     *
     * @param toEmail   The email address of the recipient
     * @param toName    The name of the recipient
     * @param link      The link to reset the password
     * @param expiresIn The time until the link expires as a string, e.g. "5 minutes", to be displayed in the email
     * @throws MailerSendException If there is an error sending the email, MailerSend SDK throws a custom exception
     */
    public void sendPasswordResetEmail(String toEmail, String toName, String link, String expiresIn) throws MailerSendException {
        // Create the email object and set the from, to, and subject fields
        Email email = new Email();
        email.setFrom("Placement Management System", fromEmail);
        Recipient recipient = new Recipient(toName, toEmail);
        email.recipients.add(recipient);
        email.setSubject("Password Reset Request");

        // Select the template created in MailerSend, and set the dynamic variables
        email.setTemplateId(resetPasswordTemplateId);
        email.addPersonalization(recipient, "link", link);
        email.addPersonalization(recipient, "toName", toName);
        email.addPersonalization(recipient, "expiresIn", expiresIn);

        // Send the email
        System.out.println("Email sending...: " + email);
        MailerSend ms = new MailerSend();
        ms.setToken(apiKey);
        MailerSendResponse response = ms.emails().send(email);
        System.out.println("Email sent successfully - Response: " + response.responseStatusCode);
    }

    /**
     * Send an account activation email when the user manager creates a new account, using MailerSend SDK
     *
     * @param toEmail   The email address of the recipient
     * @param toName    The name of the recipient
     * @param username  The username of the new account
     * @param link      The link to activate the account
     * @param expiresIn The time until the link expires as a string, e.g. "48 hours", to be displayed in the email
     * @throws MailerSendException If there is an error sending the email, MailerSend SDK throws a custom exception
     */
    public void sendAccountActivationEmail(String toEmail, String toName, String username, String link, String expiresIn) throws MailerSendException {
        // Create the email object and set the from, to, and subject fields
        Email email = new Email();
        email.setFrom("Placement Management System", fromEmail);
        Recipient recipient = new Recipient(toName, toEmail);
        email.recipients.add(recipient);
        email.setSubject("Account Activation");

        // Select the template created in MailerSend, and set the dynamic variables
        email.setTemplateId(accountActivationTemplateId);
        email.addPersonalization(recipient, "link", link);
        email.addPersonalization(recipient, "toName", toName);
        email.addPersonalization(recipient, "username", username);
        email.addPersonalization(recipient, "expiresIn", expiresIn);

        // Send the email
        System.out.println("Email sending...: " + email);
        MailerSend ms = new MailerSend();
        ms.setToken(apiKey);
        MailerSendResponse response = ms.emails().send(email);
        System.out.println("Email sent successfully - Response: " + response.responseStatusCode);
    }

    /**
     * Send a new notification email using MailerSend SDK
     *
     * @param title    The title of the notification
     * @param message  The message of the notification
     * @param link     The link from the notification to the relevant page
     * @param linkText The text to display on the link
     * @param toEmail  The email address of the recipient
     * @param toName   The name of the recipient
     * @throws MailerSendException If there is an error sending the email, MailerSend SDK throws a custom exception
     */
    public void sendNewNotificationEmail(String title, String message, String link, String linkText, String toEmail, String toName) throws MailerSendException {
        // Create the email object and set the from, to, and subject fields
        Email email = new Email();
        email.setFrom("Placement Management System", fromEmail);
        Recipient recipient = new Recipient(toName, toEmail);
        email.recipients.add(recipient);
        email.setSubject("New Notification");

        // Set the email body
        link = host + link;
        String emailHtml = String.format("<!DOCTYPE html>" + "<html>" + "<head>" + "<meta charset=\"UTF-8\">" + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" + "<title>New Notification</title>" + "</head>" + "<body style=\"font-family: Arial, sans-serif; margin: 20px;\">" + "<table width=\"100%%\" cellpadding=\"10\" cellspacing=\"0\" border=\"0\">" + "<tr><td>" + "<p><strong>Hi %s,</strong></p><br/>" + "<p>You have received a new notification:</p>" + "<h3 align=\"center\">%s</h3>" + "<p align=\"center\">%s</p>" + "<p align=\"center\">" + "<a href=\"%s\">%s</a>" + "</p><br/>" + "<p><strong>Kind regards,</strong></p>" + "<p>Placement Management System</p>" + "</td></tr>" + "<tr><td align=\"center\" style=\"color: #888;\">" + "<br/><br/>" + "<small>This is an automated email from Placement Management System.</small>" + "</td></tr>" + "</table></body></html>", toName, title, message, link, linkText);
        email.setHtml(emailHtml);

        // Send the email
        System.out.println("Email sending...: " + email);
        MailerSend ms = new MailerSend();
        ms.setToken(apiKey);
        MailerSendResponse response = ms.emails().send(email);
        System.out.println("Email sent successfully - Response: " + response.responseStatusCode);
    }

    /**
     * Send unread messages email reminder to the user using MailerSend SDK
     *
     * @param unreadCount The number of unread messages the user has
     * @param toName      The name of the recipient
     * @param toEmail     The email address of the recipient
     * @param role        The role of the recipient
     * @throws MailerSendException If there is an error sending the email, MailerSend SDK throws a custom exception
     */
    public void sendUnreadMessagesEmail(int unreadCount, String toName, String toEmail, Role role) throws MailerSendException {
        // Determine the link to the messages page based on the user's role
        String link = host;
        switch (role) {
            case ROLE_STUDENT:
                link += "/student/tutor";
                break;
            case ROLE_TUTOR:
                link += "/tutor/messages";
                break;
            default:
                // Other roles do not receive messages
                return;
        }

        // Create the email object and set the from, to, and subject fields
        Email email = new Email();
        email.setFrom("Placement Management System", fromEmail);
        Recipient recipient = new Recipient(toName, toEmail);
        email.recipients.add(recipient);
        email.setSubject("Unread Messages");

        // Set the email body
        String emailHtml = String.format("<!DOCTYPE html>" + "<html>" + "<head>" + "<meta charset=\"UTF-8\">" + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" + "<title>Unread Messages</title>" + "</head>" + "<body style=\"font-family: Arial, sans-serif; margin: 20px;\">" + "<table width=\"100%%\" cellpadding=\"10\" cellspacing=\"0\" border=\"0\">" + "<tr><td>" + "<p><strong>Hi %s,</strong></p><br/>" + "<p align=\"center\">You have %s unread messages.</p>" + "<p align=\"center\">" + "<a href=\"%s\">View Messages</a>" + "</p><br/>" + "<p><strong>Kind regards,</strong></p>" + "<p>Placement Management System</p>" + "</td></tr>" + "<tr><td align=\"center\" style=\"color: #888;\">" + "<br/><br/>" + "<small>This is an automated email from Placement Management System.</small>" + "</td></tr>" + "</table></body></html>", toName, unreadCount, link);
        email.setHtml(emailHtml);

        // Send the email
        System.out.println("Email sending...: " + email);
        MailerSend ms = new MailerSend();
        ms.setToken(apiKey);
        MailerSendResponse response = ms.emails().send(email);
        System.out.println("Email sent successfully - Response: " + response.responseStatusCode);
    }
}