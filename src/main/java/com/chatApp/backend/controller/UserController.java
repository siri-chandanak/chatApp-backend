package com.chatApp.backend.controller;

import com.chatApp.backend.dto.UserSummaryResponse;
import com.chatApp.backend.model.User;
import com.chatApp.backend.repository.UserRepository;
import com.chatApp.backend.security.AuthUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/search")
    public List<UserSummaryResponse> search(@RequestParam("q") String q) {
        Long me = AuthUtils.requireUserId(); // ensure logged in

        String query = q == null ? "" : q.trim();
        if (query.length() < 2) return List.of(); // avoid spamming DB

        return userRepository
                .findTop10ByEmailContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(query, query)
                .stream()
                .filter(u -> !u.getId().equals(me)) // don’t show self
                .map(u -> new UserSummaryResponse(u.getId(), u.getEmail(), u.getDisplayName()))
                .toList();
    }
    @GetMapping("/me")
    public UserSummaryResponse me() {
        Long me = AuthUtils.requireUserId();

        User user = userRepository.findById(me)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return new UserSummaryResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName()
        );
    }
}
