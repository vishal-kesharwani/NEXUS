package com.knowledgenexus.service;

import com.knowledgenexus.model.User;
import com.knowledgenexus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User resolve(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new RuntimeException("User not found");
        }

        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
