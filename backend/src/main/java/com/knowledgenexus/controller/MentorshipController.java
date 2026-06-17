package com.knowledgenexus.controller;

import com.knowledgenexus.dto.CreateMentorshipRequest;
import com.knowledgenexus.dto.MentorshipRequestResponse;
import com.knowledgenexus.service.MentorshipRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mentorship")
@RequiredArgsConstructor
public class MentorshipController {

    private final MentorshipRequestService service;

    @PostMapping("/request")
    public MentorshipRequestResponse create(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateMentorshipRequest request
    ) {
        return service.create(
                userDetails.getUsername(),
                request
        );
    }

    @GetMapping("/received")
    public List<MentorshipRequestResponse> received(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return service.received(
                userDetails.getUsername()
        );
    }

    @GetMapping("/sent")
    public List<MentorshipRequestResponse> sent(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return service.sent(
                userDetails.getUsername()
        );
    }

    @PutMapping("/{id}/accept")
    public MentorshipRequestResponse accept(
            @PathVariable UUID id
    ) {
        return service.accept(id);
    }

    @PutMapping("/{id}/reject")
    public MentorshipRequestResponse reject(
            @PathVariable UUID id
    ) {
        return service.reject(id);
    }
}