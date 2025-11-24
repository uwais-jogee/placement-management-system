package com.example.placementmanagementsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Scheduler for the application, to run tasks to update database records
 */
@Service
public class ApplicationScheduler {

    @Autowired
    private PlacementService placementService;
    @Autowired
    private UserService userService;

    /**
     * Scheduled job to update the statuses of placements in the database.
     * The job is run at the specified cron expression in the application.properties file, and also when the application is started.
     */
    @Scheduled(cron = "${scheduler.update-placements.cron}")
    @EventListener(ApplicationReadyEvent.class)
    public void updatePlacementStatuses() {
        placementService.updateAllPlacementStatuses();
        System.out.println("Scheduled job triggered: updatePlacementStatuses() @ " + LocalDateTime.now());
    }

    /**
     * Scheduled job to update the statuses of visits in the database.
     * The job is run at the specified cron expression in the application.properties file, and also when the application is started.
     */
    @Scheduled(cron = "${scheduler.update-visits.cron}")
    @EventListener(ApplicationReadyEvent.class)
    public void updateVisitStatuses() {
        placementService.updateVisitStatuses();
        System.out.println("Scheduled job triggered: updateVisitStatuses() @ " + LocalDateTime.now());
    }

    /**
     * Scheduled job to send email reminders to users with unread messages.
     * The job is run at the specified cron expression in the application.properties file, and also when the application is started.
     */
    @Scheduled(cron = "${scheduler.send-unread-messages-email.cron}")
    @EventListener(ApplicationReadyEvent.class)
    public void sendUnreadMessagesEmail() {
        userService.sendUnreadMessagesEmailReminders();
        System.out.println("Scheduled job triggered: sendUnreadMessagesEmail() @ " + LocalDateTime.now());
    }
}