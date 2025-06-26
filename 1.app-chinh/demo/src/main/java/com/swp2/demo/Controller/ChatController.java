package com.swp2.demo.Controller;

// In ChatController.java

// In ChatController.java

import com.swp2.demo.Repository.ChatMessageRepository;
import com.swp2.demo.entity.ChatMessage;
import com.swp2.demo.entity.User;
import com.swp2.demo.entity.dto.ChatMessageDTO;
import com.swp2.demo.service.ChatMessageService;
import com.swp2.demo.service.UserMessageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final UserMessageService userService;
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;

    @GetMapping("/messenger")
    public String chatPage(Model model, Authentication authentication) {
        String currentPrincipalName;

        if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            currentPrincipalName = oAuth2User.getAttribute("email");
            logger.info("OAuth2User detected, principal email: {}", currentPrincipalName);
        } else {
            currentPrincipalName = authentication.getName();
            logger.info("Standard user detected, principal name: {}", currentPrincipalName);
        }

        model.addAttribute("username", currentPrincipalName);

        User currentUser = userService.findUserByUsername(currentPrincipalName);
        if (currentUser == null) {
            logger.error("Authenticated user not found in database for principal: {}", currentPrincipalName);
            throw new RuntimeException("Authenticated user not found in database for principal: " + currentPrincipalName);
        }

        logger.info("Found currentUser: ID={}, Username={}, Email={}, Role={}",
                currentUser.getId(), currentUser.getUsername(), currentUser.getEmail(), currentUser.getRole());

        model.addAttribute("currentUser", currentUser);
        // ADD THIS NEW LINE: Pass the role as a simple string
        model.addAttribute("currentUserRoleString", currentUser.getRole().name());


        List<User> connectedUsers = userService.findConnectedUsers();

        // Separate other users from the current user
        List<User> otherUsers = connectedUsers.stream()
                .filter(u -> !u.getUsername().equals(currentUser.getUsername()))
                .collect(Collectors.toList());

        // Set the first other user as the default active partner
        User activePartner = otherUsers.isEmpty() ? null : otherUsers.get(0);
        List<ChatMessage> messages = (activePartner != null)
                ? chatMessageService.findChatMessages(currentUser.getUsername(), activePartner.getUsername())
                : new ArrayList<>();

        model.addAttribute("activeConversationPartner", activePartner);
        model.addAttribute("messages", messages);

        // Prepare conversation list DTOs
        User finalActivePartner = activePartner; // must be effectively final for lambda
        List<ConversationDTO> conversations = otherUsers.stream()
                .map(user -> {
                    boolean isActive = finalActivePartner != null &&
                            finalActivePartner.getUsername().equals(user.getUsername());
                    // Placeholder for last message and time - these would typically be fetched
                    // from a service or directly from the ChatMessageRepository
                    return new ConversationDTO(user.getUsername(), user, isActive, "No recent messages", "N/A");
                })
                .collect(Collectors.toList());

        model.addAttribute("conversations", conversations);

        return "messenger";
    }

    @GetMapping("/users")
    @ResponseBody
    public ResponseEntity<List<User>> findConnectedUsers() {
        return ResponseEntity.ok(userService.findConnectedUsers());
    }

    @GetMapping("/messages/{senderId}/{receiverId}")
    @ResponseBody
    public ResponseEntity<List<ChatMessage>> findChatMessages(
            @PathVariable String senderId,
            @PathVariable String receiverId) {
        return ResponseEntity.ok(chatMessageService.findChatMessages(senderId, receiverId));
    }

    // DTO for conversation display
    public static class ConversationDTO {
        private String id;
        private User partner;
        private boolean isActive;
        private String lastMessage;
        private String time;

        public ConversationDTO(String id, User partner, boolean isActive, String lastMessage, String time) {
            this.id = id;
            this.partner = partner;
            this.isActive = isActive;
            this.lastMessage = lastMessage;
            this.time = time;
        }

        public String getId() { return id; }
        public User getPartner() { return partner; }
        public boolean isActive() { return isActive; }
        public String getLastMessage() { return lastMessage; }
        public String getTime() { return time; }

        public void setId(String id) { this.id = id; }
        public void setPartner(User partner) { this.partner = partner; }
        public void setActive(boolean active) { isActive = active; }
        public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
        public void setTime(String time) { this.time = time; }
    }

    @MessageMapping("/chat.sendMessage")
    public void processMessage(@Payload ChatMessageDTO chatMessageDTO) {
        logger.info("Received chat message: senderId='{}', receiverId='{}', content='{}'",
                chatMessageDTO.getSenderId(), chatMessageDTO.getReceiverId(), chatMessageDTO.getContent());

        // Validate senderId and receiverId are not null or empty
        if (chatMessageDTO.getSenderId() == null || chatMessageDTO.getSenderId().trim().isEmpty()) {
            logger.error("Sender ID is null or empty for message: {}", chatMessageDTO);
            return; // Stop processing if senderId is invalid
        }
        if (chatMessageDTO.getReceiverId() == null || chatMessageDTO.getReceiverId().trim().isEmpty()) {
            logger.error("Receiver ID is null or empty for message: {}", chatMessageDTO);
            return; // Stop processing if receiverId is invalid
        }

        // Check if sender and receiver exist in the database (crucial for consistency)
        User senderUser = userService.findUserByUsername(chatMessageDTO.getSenderId());
        User receiverUser = userService.findUserByUsername(chatMessageDTO.getReceiverId());

        if (senderUser == null) {
            logger.error("Sender user not found for ID: '{}'. Message will not be saved.", chatMessageDTO.getSenderId());
            return; // Do not proceed if sender does not exist
        }
        if (receiverUser == null) {
            logger.error("Receiver user not found for ID: '{}'. Message will not be saved.", chatMessageDTO.getReceiverId());
            return; // Do not proceed if receiver does not exist
        }

        // Build and save the chat message entity
        ChatMessage message = ChatMessage.builder()
                .senderId(chatMessageDTO.getSenderId())
                .receiverId(chatMessageDTO.getReceiverId())
                .content(chatMessageDTO.getContent())
                .timestamp(LocalDateTime.now())
                .build();

        try {
            chatMessageRepository.save(message);
            logger.info("Message saved successfully from '{}' to '{}'. Content: '{}'",
                    chatMessageDTO.getSenderId(), chatMessageDTO.getReceiverId(), chatMessageDTO.getContent());
        } catch (Exception e) {
            logger.error("Failed to save chat message: '{}' from '{}' to '{}'. Error: {}",
                    chatMessageDTO.getContent(), chatMessageDTO.getSenderId(), chatMessageDTO.getReceiverId(), e.getMessage(), e);
            // Re-throw the exception to indicate failure to the caller/framework, or handle as needed
            throw new RuntimeException("Failed to save chat message", e);
        }

        // Send the message to the receiver via WebSocket
        messagingTemplate.convertAndSendToUser(
                chatMessageDTO.getReceiverId(), // This is the user's destination prefix
                "/queue/messages",             // The specific queue for messages
                chatMessageDTO                 // The payload to send
        );
        logger.info("Message sent via WebSocket to receiver: '{}'", chatMessageDTO.getReceiverId());
    }
}