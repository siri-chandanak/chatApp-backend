package com.chatApp.backend.controller;

import com.chatApp.backend.dto.ChatMessage;
import com.chatApp.backend.model.*;
import com.chatApp.backend.repository.ChatRepository;
import com.chatApp.backend.repository.MessageRepository;
import com.chatApp.backend.repository.UserRepository;
import com.chatApp.backend.service.ChatService;

import com.chatApp.backend.service.RedisPublisher;
import com.chatApp.backend.service.RedisSubscriber;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@Controller
public class ChatWsController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatService chatService;
    private final RedisPublisher redisPublisher;

    public ChatWsController(
            SimpMessagingTemplate messagingTemplate,
            MessageRepository messageRepository,
            ChatRepository chatRepository,
            UserRepository userRepository,
            ChatService chatService,
            RedisPublisher redisPublisher
    ) {
        this.messagingTemplate = messagingTemplate;
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.chatService = chatService;
        this.redisPublisher = redisPublisher;
    }

    // ================================
    // SEND MESSAGE
    // ================================
    @MessageMapping("/chats/{chatId}/send")
    public void send(@DestinationVariable Long chatId,
                     @Payload ChatMessage msg,
                     SimpMessageHeaderAccessor headers) {

        Long userId = extractUserId(headers);

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Chat not found"));

        chatService.requireMember(chatId, userId);

        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(msg.getContent());
        message.setCreatedAt(Instant.now());
        message.setStatus(MessageStatus.SENT);

        message = messageRepository.save(message);

        String redisPayload =
                chatId + "|" +
                        message.getId() + "|" +
                        sender.getDisplayName() + "|" +
                        message.getCreatedAt() + "|" +
                        message.getStatus().name() + "|" +
                        message.getContent();

        redisPublisher.publish(redisPayload);
    }

    // ================================
    // MESSAGE DELIVERED
    // ================================
    @MessageMapping("/chats/{chatId}/delivered")
    public void delivered(@DestinationVariable Long chatId,
                          @Payload Long messageId,
                          SimpMessageHeaderAccessor headers) {

        Long userId = extractUserId(headers);

        chatService.requireMember(chatId, userId);

        Message msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Message not found"));

        validateMessageBelongsToChat(msg, chatId);

        if (msg.getStatus() == MessageStatus.SENT) {
            msg.setStatus(MessageStatus.DELIVERED);
            messageRepository.save(msg);

            broadcastStatus(chatId, messageId, MessageStatus.DELIVERED);
        }
    }

    // ================================
    // MESSAGE READ
    // ================================
    @MessageMapping("/chats/{chatId}/read")
    public void read(@DestinationVariable Long chatId,
                     @Payload Long messageId,
                     SimpMessageHeaderAccessor headers) {

        Long userId = extractUserId(headers);

        chatService.requireMember(chatId, userId);

        Message msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Message not found"));

        validateMessageBelongsToChat(msg, chatId);

        if (msg.getStatus() != MessageStatus.READ) {
            msg.setStatus(MessageStatus.READ);
            messageRepository.save(msg);

            broadcastStatus(chatId, messageId, MessageStatus.READ);
        }
    }

    // ================================
    // HELPERS
    // ================================
    private Long extractUserId(SimpMessageHeaderAccessor headers) {
        if (headers.getSessionAttributes() == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "No WebSocket session");
        }

        Object uidObj = headers.getSessionAttributes().get("userId");
        if (uidObj == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Unauthorized WebSocket user");
        }

        return (Long) uidObj;
    }

    private void validateMessageBelongsToChat(Message msg, Long chatId) {
        if (!msg.getChat().getId().equals(chatId)) {
            throw new ResponseStatusException(BAD_REQUEST, "Message does not belong to this chat");
        }
    }

    private void broadcastStatus(Long chatId, Long messageId, MessageStatus status) {
        messagingTemplate.convertAndSend(
                "/topic/chats/" + chatId + "/status",
                (Object) Map.of(
                        "messageId", messageId,
                        "status", status.name()
                )
        );
    }
}