package com.chatApp.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name="messages",indexes={@Index(name="idx_chat_created",columnList="chat_id,createdAt DESC")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chat_id",nullable = false)
    private Chat chat;

    @ManyToOne
    @JoinColumn(name="sender_id",nullable = false)
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Instant createdAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status = MessageStatus.SENT;
}
