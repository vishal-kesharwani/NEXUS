package com.knowledgenexus.controller;

import com.knowledgenexus.dto.UpdateProfileRequest;
import com.knowledgenexus.model.User;
import com.knowledgenexus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public User me(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return userService.getCurrentUser(userDetails.getUsername());
    }

    @PutMapping("/me")
    public User updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateProfileRequest userData
    ) {
        return userService.updateProfile(userDetails.getUsername(), userData);
    }

    @GetMapping("/mentor/{id}")
    public com.knowledgenexus.dto.MentorProfileResponse getMentorProfile(
            @PathVariable UUID id
    ) {
        return userService.getMentorProfile(id);
    }
}
