package com.chatApp.backend.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRedisMessage {
    private Long chatId;
    private Object payload;
}
