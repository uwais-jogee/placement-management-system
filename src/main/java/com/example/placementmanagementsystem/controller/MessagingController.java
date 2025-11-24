package com.example.placementmanagementsystem.controller;

import com.example.placementmanagementsystem.dto.MessageDTO;
import com.example.placementmanagementsystem.model.Placement;
import com.example.placementmanagementsystem.repository.MessageRepo;
import com.example.placementmanagementsystem.repository.PlacementRepo;
import com.example.placementmanagementsystem.model.Message;
import com.example.placementmanagementsystem.repository.UserRepo;
import com.example.placementmanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for handling messaging between students and tutors
 */
@Controller
public class MessagingController {

    @Autowired
    private MessageRepo messageRepo;
    @Autowired
    private PlacementRepo placementRepo;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private UserService userService;

    /**
     * Websocket endpoint for sending messages
     *
     * @param messageDTO The message to send
     */
    @MessageMapping("/sendMessage") // Maps to /app/chat.sendMessage
    public void sendMessage(MessageDTO messageDTO) {
        // Save the message
        Message message = new Message();
        message.setContent(messageDTO.getContent());
        message.setDateTime(LocalDateTime.now());

        message.setSender(userRepo.findUserByUsername(messageDTO.getSenderUsername()));
        message.setReceiver(userRepo.findUserByUsername(messageDTO.getReceiverUsername()));
        message.setRead(false); // Set the read receipt of the new message to false
        messageRepo.save(message);

        // Get the placement
        Placement placement = placementRepo.findPlacementById(messageDTO.getPlacementId());
        // Add to placement messages for tracking
        placement.getMessageChat().add(message);
        placementRepo.save(placement);

        // Update the DTO with the generated ID
        messageDTO.setId(message.getId());

        // Broadcast to both sender and receiver's topic
        messagingTemplate.convertAndSendToUser(message.getReceiver().getUsername(), "/queue/messages", messageDTO);
        messagingTemplate.convertAndSendToUser(message.getSender().getUsername(), "/queue/messages", messageDTO);
    }

    /**
     * Websocket endpoint for marking a message as read
     *
     * @param payload The message ID to mark as read
     */
    @MessageMapping("/markAsRead")
    public void markMessageAsRead(Map<String, Long> payload) {
        Long messageId = payload.get("messageId");
        Optional<Message> messageOptional = messageRepo.findById(messageId);

        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();
            message.setRead(true);
            messageRepo.save(message);
        } else {
            throw new IllegalArgumentException("Message not found");
        }
    }

    /**
     * AJAX endpoint for fetching all messages for a placement
     *
     * @param id The placement ID
     * @return List of messages in JSON
     */
    @GetMapping("/tutor/messages/get")
    @ResponseBody
    public ResponseEntity<List<MessageDTO>> getMessagesByPlacementId(@RequestParam Long id) {
        // Get the placement entity
        Optional<Placement> placement = placementRepo.findById(id);
        if (placement.isEmpty()) {
            return ResponseEntity.notFound().build(); // 404 if placement not found
        }
        // Get the messages for the placement
        List<Message> messages = placement.get().getMessageChat();


        if (messages == null) {
            return ResponseEntity.notFound().build(); // 404 if placement not found
        }

        // Convert the messages to DTOs
        List<MessageDTO> messageDTOs = new ArrayList<>();
        for (Message message : messages) {
            // When fetching the messages, set the message as read if it is unread and the current user is the receiver
            if (!message.isRead() && message.getReceiver().getUsername().equals(userService.getCurrentLoggedInTutor().getUsername())) {
                message.setRead(true);
                messageRepo.save(message);
            }
            messageDTOs.add(new MessageDTO(message.getId(), message.getContent(), message.getSender().getUsername(), message.getSender().getFirstName(), message.getSender().getLastName(), message.getReceiver().getUsername(), message.getReceiver().getFirstName(), message.getReceiver().getLastName(), id, message.getDateTime()));
        }

        System.out.println(messageDTOs);
        return ResponseEntity.ok(messageDTOs); // 200 with messages in JSON
    }
}