package com.swp2.demo.service;

import com.swp2.demo.entity.ChatMessage;
import com.swp2.demo.Repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository repository;

    public ChatMessage save(ChatMessage chatMessage) {
        return repository.save(chatMessage);
    }

    public List<ChatMessage> findChatMessages(String senderId, String receiverId) {
        return repository.findChatHistory(senderId, receiverId);
    }
}
