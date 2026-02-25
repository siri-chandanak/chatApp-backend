package com.chatApp.backend.repository;

import com.chatApp.backend.model.Chat;
import com.chatApp.backend.model.ChatMember;
import com.chatApp.backend.model.ChatMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
    boolean existsByChatIdAndUserId(Long chatId, Long userId);
    Optional<ChatMember> findByChatIdAndUserId(Long chatId, Long userId);
    List<ChatMember> findByUserId(Long userId);
    List<ChatMember> findByChatId(Long chatId);
    List<ChatMember> findByChatIdAndRole(Long chatId, ChatMemberRole role);
    @Query("""
    SELECT c FROM Chat c
    JOIN ChatMember m1 ON m1.chat = c
    JOIN ChatMember m2 ON m2.chat = c
    WHERE c.type = 'DIRECT'
      AND m1.user.id = :user1
      AND m2.user.id = :user2
""")
    Optional<Chat> findDirectChatBetween(@Param("user1") Long user1,
                                         @Param("user2") Long user2);
}