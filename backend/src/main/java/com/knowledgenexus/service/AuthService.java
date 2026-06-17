package com.knowledgenexus.service;

import com.knowledgenexus.dto.AuthResponse;
import com.knowledgenexus.dto.GoogleAuthRequest;
import com.knowledgenexus.dto.LoginRequest;
import com.knowledgenexus.dto.RegisterRequest;
import com.knowledgenexus.model.User;
import com.knowledgenexus.repository.UserRepository;
import com.knowledgenexus.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleTokenVerifier googleTokenVerifier;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Generate username from email (first part before @)
        String username = request.getEmail().split("@")[0];
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = request.getEmail().split("@")[0] + counter;
            counter++;
        }

        User user = User.builder()
                .username(username)
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(savedUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .user(savedUser)
                .build();
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash())) {

            throw new RuntimeException("Invalid credentials");
        }

        String token =
                jwtTokenProvider.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .user(user)
                .build();
    }

    public AuthResponse googleLogin(GoogleAuthRequest request) {
        GoogleTokenVerifier.GoogleProfile profile = googleTokenVerifier.verify(request.getCredential());

        if (profile.email() == null || profile.email().isBlank()) {
            throw new RuntimeException("Google account does not provide an email");
        }
        if (!profile.emailVerified()) {
            throw new RuntimeException("Google account email is not verified");
        }
        if (profile.sub() == null || profile.sub().isBlank()) {
            throw new RuntimeException("Google account subject is missing");
        }

        User user = userRepository.findByGoogleSub(profile.sub())
                .orElseGet(() -> userRepository.findByEmail(profile.email())
                        .map(existing -> linkGoogleAccount(existing, profile))
                        .orElseGet(() -> createGoogleUser(profile)));

        if (profile.givenName() != null && user.getFirstName() == null) {
            user.setFirstName(profile.givenName());
        }
        if (profile.familyName() != null && user.getLastName() == null) {
            user.setLastName(profile.familyName());
        }
        if (user.getGoogleSub() == null) {
            user.setGoogleSub(profile.sub());
        }

        user = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .user(user)
                .build();
    }

    private User linkGoogleAccount(User user, GoogleTokenVerifier.GoogleProfile profile) {
        user.setGoogleSub(profile.sub());
        if (user.getFirstName() == null && profile.givenName() != null) {
            user.setFirstName(profile.givenName());
        }
        if (user.getLastName() == null && profile.familyName() != null) {
            user.setLastName(profile.familyName());
        }
        return user;
    }

    private User createGoogleUser(GoogleTokenVerifier.GoogleProfile profile) {
        String usernameBase = profile.email().split("@")[0];
        String username = usernameBase;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = usernameBase + counter;
            counter++;
        }

        return User.builder()
                .username(username)
                .email(profile.email())
                .passwordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                .googleSub(profile.sub())
                .firstName(profile.givenName() != null ? profile.givenName() : profile.fullName())
                .lastName(profile.familyName())
                .build();
    }
}
