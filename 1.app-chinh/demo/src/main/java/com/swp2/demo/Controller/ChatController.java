package com.swp2.demo.Controller;

import com.swp2.demo.entity.ChatMessage;
import com.swp2.demo.entity.User;
import com.swp2.demo.service.ChatMessageService;
import com.swp2.demo.service.UserMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final UserMessageService userService;
    private final ChatMessageService chatMessageService;

    @GetMapping("/messenger")
    public String chatPage(Model model, Authentication authentication) {
        String currentUsername = authentication.getName();
        model.addAttribute("username", currentUsername);

        User currentUser = userService.findUserByUsername(currentUsername);
        model.addAttribute("currentUser", currentUser);

        List<User> connectedUsers = userService.findConnectedUsers();

        List<User> otherUsers = connectedUsers.stream()
                .filter(user -> !user.getUsername().equals(currentUsername))
                .collect(Collectors.toList());

        User activePartner = null;
        List<ChatMessage> messages = new ArrayList<>();

        if (!otherUsers.isEmpty()) {
            activePartner = otherUsers.get(0);
            messages = chatMessageService.findChatMessages(currentUsername, activePartner.getUsername());
        }

        // --- FIX IS HERE ---
        // Create a final or effectively final copy of activePartner
        final User finalActivePartner = activePartner; // This makes it effectively final for the lambda

        model.addAttribute("activeConversationPartner", finalActivePartner); // Use the final copy
        model.addAttribute("messages", messages);

        // Prepare conversation data for the sidebar
        List<ConversationDTO> conversations = otherUsers.stream()
                .map(user -> {
                    // Use finalActivePartner here
                    boolean isActive = (finalActivePartner != null && finalActivePartner.getUsername().equals(user.getUsername()));
                    return new ConversationDTO(user.getUsername(), user, isActive, "No recent messages", "N/A");
                })
                .collect(Collectors.toList());

        model.addAttribute("conversations", conversations);

        return "messenger";
    }

    // DTO (Data Transfer Object) for conversations
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

        // Getters
        public String getId() { return id; }
        public User getPartner() { return partner; }
        public boolean isActive() { return isActive; }
        public String getLastMessage() { return lastMessage; }
        public String getTime() { return time; }

        // Setters
        public void setId(String id) { this.id = id; }
        public void setPartner(User partner) { this.partner = partner; }
        public void setActive(boolean active) { isActive = active; }
        public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
        public void setTime(String time) { this.time = time; }
    }


    @GetMapping("/users")
    @ResponseBody
    public ResponseEntity<List<User>> findConnectedUsers() {
        return ResponseEntity.ok(userService.findConnectedUsers());
    }

    @GetMapping("/messages/{senderId}/{receiverId}")
    @ResponseBody
    public ResponseEntity<List<ChatMessage>> findChatMessages(@PathVariable String senderId,
                                                              @PathVariable String receiverId) {
        return ResponseEntity.ok(chatMessageService.findChatMessages(senderId, receiverId));
    }
}