package com.knowledgenexus.controller;

import com.knowledgenexus.model.User;
import com.knowledgenexus.security.JwtTokenProvider;
import com.knowledgenexus.service.CurrentUserService;
import com.knowledgenexus.service.GoogleTokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/google")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final GoogleTokenService googleTokenService;
    private final CurrentUserService currentUserService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Frontend navigates the browser here (full page redirect, not an axios call) to start
     * the Google consent flow. Browser navigations can't carry an Authorization header, so
     * the JWT is passed as a query param instead and validated manually here.
     */
    @GetMapping("/oauth/connect")
    public void connect(@RequestParam String token, HttpServletResponse response) throws IOException {
        if (!jwtTokenProvider.validateToken(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        String email = jwtTokenProvider.getEmailFromToken(token);
        String state = Base64.getUrlEncoder().encodeToString(email.getBytes(StandardCharsets.UTF_8));
        response.sendRedirect(googleTokenService.buildAuthUrl(state));
    }

    /** Google redirects here after the user grants/denies consent. */
    @GetMapping("/oauth/callback")
    public void callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam String state,
            HttpServletResponse response
    ) throws IOException {
        String email = new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);

        if (error != null || code == null) {
            response.sendRedirect("/meetings?googleConnected=false");
            return;
        }

        User user = currentUserService.resolve(email);
        googleTokenService.exchangeCodeAndSave(code, user);
        response.sendRedirect("/meetings?googleConnected=true");
    }

    /** Frontend polls this to know whether to show "Connect Google Calendar". */
    @GetMapping("/status")
    public Map<String, Boolean> status(@AuthenticationPrincipal UserDetails userDetails) {
        User user = currentUserService.resolve(userDetails.getUsername());
        return Map.of("connected", user.getGoogleRefreshToken() != null);
    }
}