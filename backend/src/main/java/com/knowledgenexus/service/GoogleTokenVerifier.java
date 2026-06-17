package com.knowledgenexus.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class GoogleTokenVerifier {

    @Value("${app.google.client-id:}")
    private String googleClientId;

    private JwtDecoder decoder;

    @PostConstruct
    public void init() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                .withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .build();

        OAuth2TokenValidator<Jwt> issuerValidator = token -> {
            String issuer = token.getIssuer() != null ? token.getIssuer().toString() : "";
            if ("https://accounts.google.com".equals(issuer) || "accounts.google.com".equals(issuer)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Invalid Google issuer", null)
            );
        };

        OAuth2TokenValidator<Jwt> audienceValidator = token ->
                token.getAudience().contains(googleClientId)
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(
                                new OAuth2Error("invalid_token", "Invalid Google audience", null)
                        );

        jwtDecoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator)
        );
        this.decoder = jwtDecoder;
    }

    public GoogleProfile verify(String credential) {
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new IllegalStateException("Google client id is not configured");
        }

        Jwt jwt = decoder.decode(credential);

        return new GoogleProfile(
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified")),
                jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("family_name"),
                jwt.getClaimAsString("name"),
                jwt.getClaimAsString("picture"),
                jwt.getClaimAsString("hd")
        );
    }

    public record GoogleProfile(
            String sub,
            String email,
            boolean emailVerified,
            String givenName,
            String familyName,
            String fullName,
            String picture,
            String hostedDomain
    ) {}
}
