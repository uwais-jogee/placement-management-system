package com.example.placementmanagementsystem;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class that runs the Spring Boot application.
 * Implements CommandLineRunner to run any methods on startup
 */
@SpringBootApplication
@EnableScheduling
public class PlacementManagementSystemApplication implements CommandLineRunner {

    /**
     * Main method that is called when the application is run
     * Calls .run method to run any methods on startup
     *
     * @param args List of command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(PlacementManagementSystemApplication.class, args);
    }

    /**
     * Run method that is called on application startup
     *
     * @param args List of command line arguments
     */
    @Override
    public void run(String... args) {
        System.out.println("Application has started running");
    }
}