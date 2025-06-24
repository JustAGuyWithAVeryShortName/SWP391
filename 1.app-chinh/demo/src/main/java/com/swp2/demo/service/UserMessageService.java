package com.swp2.demo.service;

import com.swp2.demo.entity.*;
import com.swp2.demo.Repository.ChatMessageRepository;
import com.swp2.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class UserMessageService {
    private static final Logger logger = LoggerFactory.getLogger(UserMessageService.class); // Add logger
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;

    public User findUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found with username: " + username);
        }
        return user;
    }

    public void setStatus(String username, Status status) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setStatus(status);
            userRepository.save(user);
        }
    }

    public List<User> findConnectedUsers() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername);

        if (currentUser == null) {
            logger.error("Current user not found for username: {}", currentUsername);
            throw new RuntimeException("Current user not found for username: " + currentUsername);
        }

        logger.info("User {} with role {} is requesting connected users.", currentUser.getUsername(), currentUser.getRole());

        if (currentUser.getRole() == Role.Member) {
            List<User> coaches = userRepository.findAllByRole(Role.Coach);
            logger.info("Member {} found {} coaches.", currentUser.getUsername(), coaches.size());
            coaches.forEach(coach -> logger.info("  Coach: {} (Status: {})", coach.getUsername(), coach.getStatus()));
            return coaches;
        } else if (currentUser.getRole() == Role.Coach) {
            // Assuming you want coaches to see ALL members as per our latest discussion:
            List<User> members = userRepository.findAllByRole(Role.Member);
            logger.info("Coach {} found {} members (all).", currentUser.getUsername(), members.size());
            members.forEach(member -> logger.info("  Member: {} (Status: {})", member.getUsername(), member.getStatus()));
            return members;

            // If you want coaches to see ONLY members they've chatted with, keep this original logic:
            /*
            List<ChatMessage> messagesWithCoach = chatMessageRepository.findBySenderIdOrReceiverId(currentUsername, currentUsername);
            Set<String> memberUsernames = messagesWithCoach.stream()
                    .map(msg -> msg.getSenderId().equals(currentUsername) ? msg.getReceiverId() : msg.getSenderId())
                    .filter(username -> {
                        User potentialMember = userRepository.findByUsername(username);
                        return potentialMember != null && potentialMember.getRole() == Role.Member;
                    })
                    .collect(Collectors.toSet());
            List<User> allMembers = userRepository.findAllByRole(Role.Member);
            List<User> chattedMembers = allMembers.stream()
                    .filter(member -> memberUsernames.contains(member.getUsername()))
                    .collect(Collectors.toList());
            logger.info("Coach {} found {} members (chatted only).", currentUser.getUsername(), chattedMembers.size());
            chattedMembers.forEach(member -> logger.info("  Chatted Member: {} (Status: {})", member.getUsername(), member.getStatus()));
            return chattedMembers;
            */
        }
        logger.warn("User {} with unsupported role {} attempted to find connected users.", currentUser.getUsername(), currentUser.getRole());
        return List.of();
    }
}