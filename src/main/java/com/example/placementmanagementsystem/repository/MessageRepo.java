package com.example.placementmanagementsystem.repository;

import com.example.placementmanagementsystem.model.Message;
import com.example.placementmanagementsystem.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CRUD repository for Message entity
 */
@Repository
public interface MessageRepo extends CrudRepository<Message, Long> {

    /**
     * Query to find all messages by the given receiver and read status
     *
     * @param receiver The receiver of the message
     * @param isRead      The read status of the message (true for read, false for unread)
     * @return The list of messages that match the given criteria
     */
    List<Message> findMessagesByReceiverAndIsRead(User receiver, boolean isRead);
}