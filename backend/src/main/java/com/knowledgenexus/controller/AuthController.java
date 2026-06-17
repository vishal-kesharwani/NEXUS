package com.knowledgenexus.controller;

import com.knowledgenexus.dto.AuthResponse;
import com.knowledgenexus.dto.GoogleAuthRequest;
import com.knowledgenexus.dto.LoginRequest;
import com.knowledgenexus.dto.RegisterRequest;
import com.knowledgenexus.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(
                authService.register(request)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(
            @Valid @RequestBody GoogleAuthRequest request
    ) {
        return ResponseEntity.ok(
                authService.googleLogin(request)
        );
    }
}
