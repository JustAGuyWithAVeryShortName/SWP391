package com.swp2.demo.Controller;

import com.swp2.demo.Repository.MessageHomeRepository;
import com.swp2.demo.Repository.UserRepository;
import com.swp2.demo.entity.MessageHome;
import com.swp2.demo.entity.User;
import com.swp2.demo.entity.dto.MessageHomeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
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
    public List<MessageHomeDTO> getChatHistory() {
        return messageRepo.findAllByOrderBySentAtAsc()
                .stream()
                .map(MessageHomeDTO::new)
                .toList();
    }


    @MessageMapping("/chat.send")
    @SendTo("/topic/public")
    public MessageHomeDTO send(MessageHome message, Principal principal) {
        String username = "anonymous";

        if (principal instanceof OAuth2AuthenticationToken oauthToken) {
            Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();
            username = (String) attributes.get("email");
        } else if (principal instanceof Authentication auth) {
            username = auth.getName();
        } else if (principal != null) {
            username = principal.getName();
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found: " + username);
        }

        message.setUser(user);
        message.setSentAt(LocalDateTime.now());

        messageRepo.save(message);
        return new MessageHomeDTO(message); // ✅ đúng kiểu trả về
    }
}
