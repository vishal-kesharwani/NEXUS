package com.knowledgenexus.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "google_sub", unique = true)
    private String googleSub;

    private String firstName;

    private String lastName;

    private String headline;

// ---- Add these fields inside your existing User entity class (User.java) ----
// Place them anywhere among the other @Column fields. Lombok's @Getter/@Setter
// (or @Data) on the class will auto-generate the getters/setters used by
// GoogleTokenService and GoogleAuthController.
 
    @Column(length = 2048)
    private String googleAccessToken;
 
    @Column(length = 2048)
    private String googleRefreshToken;
 
    private java.time.LocalDateTime googleTokenExpiry;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @JsonProperty("experience")
    @Column(name = "experience_years")
    private Integer experienceYears;

    @JsonProperty("role")
    @Column(name = "role_title")
    private String currentRole;

    private String company;

    private String location;

    @JsonProperty("linkedIn")
    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @JsonProperty("github")
    @Column(name = "github_url")
    private String githubUrl;

    @PrePersist
    public void prePersist() {
        id = UUID.randomUUID();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }


}
