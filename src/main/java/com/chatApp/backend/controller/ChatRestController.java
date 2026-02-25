package com.chatApp.backend.controller;

import com.chatApp.backend.dto.MessageResponse;
import com.chatApp.backend.model.Message;
import com.chatApp.backend.repository.MessageRepository;
import com.chatApp.backend.security.AuthUtils;
import com.chatApp.backend.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatRestController {

    private final MessageRepository messageRepository;
    private final ChatService chatService;

    public ChatRestController(MessageRepository messageRepository, ChatService chatService) {
        this.messageRepository = messageRepository;
        this.chatService = chatService;
    }

    @GetMapping("/{chatId}/messages")
    public List<MessageResponse> getMessages(@PathVariable Long chatId) {
        Long me = AuthUtils.requireUserId();
        chatService.requireMember(chatId, me);
        List<Message> messages =
                messageRepository.findTop50ByChatIdOrderByCreatedAtDesc(chatId);

        return messages.stream()
                .map(m -> new MessageResponse(
                        m.getId(),
                        m.getContent(),
                        m.getSender().getDisplayName(),
                        m.getCreatedAt(),
                        m.getStatus().name()
                ))
                .toList();
    }
}