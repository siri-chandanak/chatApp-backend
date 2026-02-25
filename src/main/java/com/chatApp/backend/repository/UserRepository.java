package com.chatApp.backend.repository;

import com.chatApp.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    // simple search (case-insensitive)
    List<User> findTop10ByEmailContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(String email, String displayName);

}
