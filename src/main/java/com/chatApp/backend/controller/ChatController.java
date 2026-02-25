package com.chatApp.backend.controller;

import com.chatApp.backend.dto.ChatDetailsResponse;
import com.chatApp.backend.dto.ChatSummaryResponse;
import com.chatApp.backend.dto.CreateDirectChatRequest;
import com.chatApp.backend.dto.CreateGroupChatRequest;
import com.chatApp.backend.model.Chat;
import com.chatApp.backend.security.AuthUtils;
import com.chatApp.backend.service.ChatService;
import com.chatApp.backend.service.PresenceService;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;
    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatService chatService, PresenceService ps, SimpMessagingTemplate mt) {
        this.chatService = chatService;
        this.presenceService=ps;
        this.messagingTemplate=mt;
    }

    @PostMapping("/direct")
    public ChatSummaryResponse createDirect(@Valid @RequestBody CreateDirectChatRequest req) {
        Long me = AuthUtils.requireUserId();
        return chatService.createDirectChatWithTitle(me, req.getOtherUserId());
    }

    @PostMapping("/group")
    public ChatSummaryResponse createGroup(@Valid @RequestBody CreateGroupChatRequest req) {
        Long me = AuthUtils.requireUserId();
        return chatService.createGroupChatWithTitle(me, req.getTitle(), req.getMemberUserIds());
    }

    @GetMapping
    public List<ChatSummaryResponse> myChats() {
        Long me = AuthUtils.requireUserId();
        return chatService.listMyChatsWithTitles(me);
    }

    @GetMapping("/presence/{userId}")
    public boolean isOnline(@PathVariable Long userId) {
        return presenceService.isOnline(userId);
    }

    @MessageMapping("/chats/{chatId}/typing")
    public void typing(@DestinationVariable Long chatId,
                       SimpMessageHeaderAccessor headers) {

        Object uidObj = headers.getSessionAttributes().get("userId");
        if (uidObj == null) return;

        messagingTemplate.convertAndSend(
                "/topic/chats/" + chatId + "/typing",
                uidObj
        );
    }
    @GetMapping("/{chatId}")
    public ChatDetailsResponse getChatDetails(@PathVariable Long chatId) {
        Long me = AuthUtils.requireUserId();
        return chatService.getChatDetails(chatId, me);
    }
}