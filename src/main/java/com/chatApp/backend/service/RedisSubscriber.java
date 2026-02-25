package com.chatApp.backend.service;


import com.chatApp.backend.dto.ChatMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisSubscriber {

    private final SimpMessagingTemplate messagingTemplate;

    public RedisSubscriber(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void handleMessage(String rawMessage) {

        try {
            // rawMessage format:
            // chatId|messageId|sender|timestamp|status|content

            String[] parts = rawMessage.split("\\|", 6);

            Long chatId = Long.parseLong(parts[0]);

            ChatMessage out = new ChatMessage();
            out.setId(Long.parseLong(parts[1]));
            out.setChatId(parts[0]);
            out.setSender(parts[2]);
            out.setTimestamp(parts[3]);
            out.setStatus(parts[4]);
            out.setContent(parts[5]);

            messagingTemplate.convertAndSend(
                    "/topic/chats/" + chatId,
                    out
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}