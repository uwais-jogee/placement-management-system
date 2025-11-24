package com.example.placementmanagementsystem.repository;

import com.example.placementmanagementsystem.model.Notification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * CRUD repository for Notification entity
 */
@Repository
public interface NotificationRepo extends CrudRepository<Notification, Long> {
}