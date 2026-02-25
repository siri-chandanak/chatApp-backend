package com.chatApp.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "chat_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"chat_id", "user_id"}),
        indexes = {
                @Index(name = "idx_chat_members_user", columnList = "user_id"),
                @Index(name = "idx_chat_members_chat", columnList = "chat_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatMemberRole role = ChatMemberRole.MEMBER;

    @Column(nullable = false)
    private Instant joinedAt = Instant.now();
}