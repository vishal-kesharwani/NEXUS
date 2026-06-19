package com.knowledgenexus.service;

import com.knowledgenexus.model.User;
import com.knowledgenexus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Handles the Google OAuth2 "authorization code" flow: building the consent URL,
 * exchanging the returned code for tokens, and refreshing expired access tokens.
 */
@Service
@RequiredArgsConstructor
public class GoogleTokenService {

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String SCOPE = "https://www.googleapis.com/auth/calendar.events";

    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public String buildAuthUrl(String state) {
        return UriComponentsBuilder.fromHttpUrl("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", SCOPE)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    @SuppressWarnings("unchecked")
    public void exchangeCodeAndSave(String code, User user) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri);
        form.add("grant_type", "authorization_code");

        Map<String, Object> body = postForm(form);

        user.setGoogleAccessToken((String) body.get("access_token"));
        if (body.get("refresh_token") != null) {
            user.setGoogleRefreshToken((String) body.get("refresh_token"));
        }
        user.setGoogleTokenExpiry(LocalDateTime.now().plusSeconds(toLong(body.get("expires_in"))));
        userRepository.save(user);
    }

    /**
     * Returns a valid (non-expired) access token for this user, refreshing it
     * via the stored refresh token if needed. Throws if the user never connected Google.
     */
    public String getValidAccessToken(User user) {
        if (user.getGoogleRefreshToken() == null) {
            throw new IllegalStateException("GOOGLE_NOT_CONNECTED");
        }

        boolean stillValid = user.getGoogleTokenExpiry() != null
                && user.getGoogleTokenExpiry().isAfter(LocalDateTime.now().plusMinutes(1));

        if (stillValid) {
            return user.getGoogleAccessToken();
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", user.getGoogleRefreshToken());
        form.add("grant_type", "refresh_token");

        Map<String, Object> body = postForm(form);

        String accessToken = (String) body.get("access_token");
        user.setGoogleAccessToken(accessToken);
        user.setGoogleTokenExpiry(LocalDateTime.now().plusSeconds(toLong(body.get("expires_in"))));
        userRepository.save(user);

        return accessToken;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> postForm(MultiValueMap<String, String> form) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<Map> response = restTemplate.postForEntity(TOKEN_URL, new HttpEntity<>(form, headers), Map.class);
        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("Empty response from Google token endpoint");
        }
        return body;
    }

    private long toLong(Object value) {
        return value instanceof Integer i ? i.longValue() : (Long) value;
    }
}