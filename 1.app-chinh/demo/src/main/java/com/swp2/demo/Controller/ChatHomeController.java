package com.swp2.demo.Controller;

import com.swp2.demo.Repository.MessageHomeRepository;
import com.swp2.demo.Repository.UserRepository;
import com.swp2.demo.entity.MessageHome;
import com.swp2.demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
public class ChatHomeController {
    @Autowired
    private MessageHomeRepository messageRepo;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/api/chat/history")
    @ResponseBody
    public List<MessageHome> getChatHistory() {
        return messageRepo.findAllByOrderBySentAtAsc();
    }


    @MessageMapping("/chat.send")
    @SendTo("/topic/public")
    public MessageHome send(MessageHome message, Authentication authentication) {
        String username = "anonymous";

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();
            username = (String) attributes.get("email"); // ✅ Lấy đúng email
        } else {
            username = authentication.getName(); // với login thường
        }


        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found: " + username);
        }

        message.setUser(user);
        message.setSentAt(LocalDateTime.now());

        messageRepo.save(message);
        return message;
    }
}
