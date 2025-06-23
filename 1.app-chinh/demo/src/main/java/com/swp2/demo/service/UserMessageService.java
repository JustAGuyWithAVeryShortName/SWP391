package com.swp2.demo.service;

import com.swp2.demo.entity.*;
import com.swp2.demo.Repository.ChatMessageRepository;
import com.swp2.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserMessageService {
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;

    public User findUserByUsername(String username) {
        User user = userRepository.findByUsername(username); // Call directly
        if (user == null) {
            // If it returns null, throw your exception
            throw new RuntimeException("User not found with username: " + username);
        }
        return user; // Return the user if found
    }
    public void setStatus(String username, Status status) {
        User user = userRepository.findByUsername(username);
        if (user != null) { // Check if user was found
            user.setStatus(status);
            userRepository.save(user);
        }
        // Optionally, you might want to log a warning or throw an exception if the user is not found
        // else {
        //     System.out.println("Warning: User with username " + username + " not found.");
        // }
    }

    public List<User> findConnectedUsers() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername); // This now returns User or null

        if (currentUser == null) {
            throw new RuntimeException("Current user not found for username: " + currentUsername);
        }

        if (currentUser.getRole() == Role.Member) {
            // Members can see all online coaches
            return userRepository.findAllByRoleAndStatus(Role.Coach, Status.ONLINE);
        } else if (currentUser.getRole() == Role.Coach) {
            // Coaches can only see members who have texted them
            // 1. Get all messages involving this coach
            List<ChatMessage> messagesWithCoach = chatMessageRepository.findBySenderIdOrReceiverId(currentUsername, currentUsername);

            // 2. Extract the usernames of the members from these messages
            Set<String> memberUsernames = messagesWithCoach.stream()
                    .map(msg -> msg.getSenderId().equals(currentUsername) ? msg.getReceiverId() : msg.getSenderId())
                    .collect(Collectors.toSet());

            // 3. Find all online members and filter by those who have chatted with the coach
            List<User> allOnlineMembers = userRepository.findAllByRoleAndStatus(Role.Member, Status.ONLINE);
            return allOnlineMembers.stream()
                    .filter(member -> memberUsernames.contains(member.getUsername()))
                    .collect(Collectors.toList());
        }
        return List.of(); // Return empty list for other roles or if something goes wrong
    }
}
