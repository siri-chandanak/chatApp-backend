package com.chatApp.backend.repository;

import com.chatApp.backend.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Long>{
}
