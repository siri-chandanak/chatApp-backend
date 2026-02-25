package com.chatApp.backend.service;

import com.chatApp.backend.dto.ChatDetailsResponse;
import com.chatApp.backend.dto.ChatSummaryResponse;
import com.chatApp.backend.dto.UserSummaryResponse;
import com.chatApp.backend.model.*;
import com.chatApp.backend.repository.ChatMemberRepository;
import com.chatApp.backend.repository.ChatRepository;
import com.chatApp.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.springframework.http.HttpStatus.*;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final UserRepository userRepository;

    public ChatService(ChatRepository chatRepository,
                       ChatMemberRepository chatMemberRepository,
                       UserRepository userRepository) {
        this.chatRepository = chatRepository;
        this.chatMemberRepository = chatMemberRepository;
        this.userRepository = userRepository;
    }

    public void requireMember(Long chatId, Long userId) {
        if (!chatMemberRepository.existsByChatIdAndUserId(chatId, userId)) {
            throw new ResponseStatusException(FORBIDDEN, "You are not a member of this chat");
        }
    }

    @Transactional
    public Chat createDirectChat(Long me, Long otherUserId) {
        if (Objects.equals(me, otherUserId)) {
            throw new ResponseStatusException(BAD_REQUEST, "Cannot create direct chat with yourself");
        }

        User meUser = userRepository.findById(me)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        User other = userRepository.findById(otherUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Other user not found"));

        // Check existing
        Optional<Chat> existing =
                chatMemberRepository.findDirectChatBetween(me, otherUserId);

        if (existing.isPresent()) {
            return existing.get();
        }
        // SIMPLE MVP: always create new direct chat
        // (Later Step 6 we’ll prevent duplicates by searching existing chats)
        Chat chat = new Chat();
        chat.setType(ChatType.DIRECT);
        chat.setTitle(null);
        chat = chatRepository.save(chat);

        ChatMember m1 = new ChatMember();
        m1.setChat(chat); m1.setUser(meUser); m1.setRole(ChatMemberRole.MEMBER);

        ChatMember m2 = new ChatMember();
        m2.setChat(chat); m2.setUser(other); m2.setRole(ChatMemberRole.MEMBER);

        chatMemberRepository.saveAll(List.of(m1, m2));
        return chat;
    }

    @Transactional
    public Chat createGroupChat(Long me, String title, List<Long> memberIds) {
        User meUser = userRepository.findById(me)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        Chat chat = new Chat();
        chat.setType(ChatType.GROUP);
        chat.setTitle(title);
        chat = chatRepository.save(chat);

        Set<Long> unique = new HashSet<>(memberIds);
        unique.add(me);

        List<ChatMember> members = new ArrayList<>();
        for (Long uid : unique) {
            User u = userRepository.findById(uid)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found: " + uid));
            ChatMember cm = new ChatMember();
            cm.setChat(chat);
            cm.setUser(u);
            cm.setRole(uid.equals(me) ? ChatMemberRole.ADMIN : ChatMemberRole.MEMBER);
            members.add(cm);
        }

        chatMemberRepository.saveAll(members);
        return chat;
    }

    public List<ChatSummaryResponse> listMyChatsWithTitles(Long userId) {

        List<ChatMember> memberships = chatMemberRepository.findByUserId(userId);

        return memberships.stream().map(cm -> {
            Chat chat = cm.getChat();

            String displayTitle;

            if (chat.getType() == ChatType.DIRECT) {
                List<ChatMember> members = chatMemberRepository.findByChatId(chat.getId());

                displayTitle = members.stream()
                        .map(ChatMember::getUser)
                        .filter(u -> !u.getId().equals(userId))
                        .map(User::getDisplayName)
                        .findFirst()
                        .orElse("Direct Chat");
            } else {
                displayTitle = chat.getTitle();
            }

            return new ChatSummaryResponse(
                    chat.getId(),
                    chat.getType(),
                    chat.getTitle(),
                    displayTitle,
                    chat.getCreatedAt()
                    );

        }).toList();
    }


    @Transactional
    public ChatSummaryResponse createDirectChatWithTitle(Long me, Long otherUserId) {
        Chat chat = createDirectChat(me, otherUserId);
        return buildChatSummary(chat, me);
    }

    @Transactional
    public ChatSummaryResponse createGroupChatWithTitle(Long me, String title, List<Long> memberIds) {
        Chat chat = createGroupChat(me, title, memberIds);
        return buildChatSummary(chat, me);
    }

    private ChatSummaryResponse buildChatSummary(Chat chat, Long me) {

        String displayTitle;

        if (chat.getType() == ChatType.DIRECT) {
            List<ChatMember> members = chatMemberRepository.findByChatId(chat.getId());

            displayTitle = members.stream()
                    .map(ChatMember::getUser)
                    .filter(u -> !u.getId().equals(me))
                    .map(User::getDisplayName)
                    .findFirst()
                    .orElse("Direct Chat");
        } else {
            displayTitle = chat.getTitle();
        }

        return new ChatSummaryResponse(
                chat.getId(),
                chat.getType(),
                chat.getTitle(),
                displayTitle,
                chat.getCreatedAt()
        );
    }
    public ChatDetailsResponse getChatDetails(Long chatId, Long me) {

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        List<ChatMember> members = chatMemberRepository.findByChatId(chatId);

        List<UserSummaryResponse> memberDtos = members.stream()
                .map(m -> {
                    User u = m.getUser();
                    return new UserSummaryResponse(
                            u.getId(),
                            u.getEmail(),
                            u.getDisplayName()
                    );
                }).toList();

        String displayTitle;

        if (chat.getType() == ChatType.DIRECT) {
            displayTitle = members.stream()
                    .map(ChatMember::getUser)
                    .filter(u -> !u.getId().equals(me))
                    .map(User::getDisplayName)
                    .findFirst()
                    .orElse("Direct Chat");
        } else {
            displayTitle = chat.getTitle();
        }

        return new ChatDetailsResponse(
                chat.getId(),
                chat.getType().name(),
                chat.getTitle(),
                displayTitle,
                chat.getCreatedAt(),
                memberDtos
        );
    }
}
