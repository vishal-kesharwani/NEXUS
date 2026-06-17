package com.knowledgenexus.controller;

import com.knowledgenexus.dto.MentorAvailabilityRequest;
import com.knowledgenexus.dto.MentorAvailabilityResponse;
import com.knowledgenexus.service.MentorAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class MentorAvailabilityController {

    private final MentorAvailabilityService mentorAvailabilityService;

    @PostMapping
    public MentorAvailabilityResponse create(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody MentorAvailabilityRequest request
    ) {

        return mentorAvailabilityService.create(
                userDetails.getUsername(),
                request
        );
    }

    @GetMapping("/mentor/{mentorId}")
    public List<MentorAvailabilityResponse> getMentorAvailability(
            @PathVariable String mentorId
    ) {
        return mentorAvailabilityService.getMentorAvailability(mentorId);
    }

    @DeleteMapping("/{id}")
    public void deleteAvailability(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id
    ) {
        mentorAvailabilityService.deleteAvailability(userDetails.getUsername(), id);
    }
}