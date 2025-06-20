package com.swp2.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "chatbox")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Enumerated(EnumType.STRING)
    private MessageType type;
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String content;
    private String sender;
    private java.time.LocalDateTime timestamp;

}
