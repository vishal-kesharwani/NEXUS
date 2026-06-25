package com.knowledgenexus.controller;

import com.knowledgenexus.dto.CreateMeetingRequest;
import com.knowledgenexus.dto.MeetingResponse;
import com.knowledgenexus.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;



    @PostMapping
    public MeetingResponse scheduleMeeting(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateMeetingRequest request
    ) {
        return meetingService.scheduleMeeting(userDetails.getUsername(), request);
    }

    @GetMapping("/conversation/{conversationId}")
    public List<MeetingResponse> getMeetings(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID conversationId
    ) {
        return meetingService.getMeetings(userDetails.getUsername(), conversationId);
    }

    @PostMapping("/{id}/accept")
    public MeetingResponse accept(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id
    ) {
        try {
            return meetingService.acceptMeeting(userDetails.getUsername(), id);
        } catch (IllegalStateException ex) {
            if ("GOOGLE_NOT_CONNECTED".equals(ex.getMessage())) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.PRECONDITION_REQUIRED,
                        "The session organizer needs to connect their Google account before this can be accepted."
                );
            }
            throw ex;
        }
    }

    @PostMapping("/{id}/decline")
    public MeetingResponse decline(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id
    ) {
        return meetingService.declineMeeting(userDetails.getUsername(), id);
    }

    @DeleteMapping("/{id}/link")
    public MeetingResponse removeMeetLink(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id
    ) {
        return meetingService.removeMeetLink(userDetails.getUsername(), id);
    }

    @GetMapping
    public List<MeetingResponse> myMeetings(
            @AuthenticationPrincipal
            UserDetails userDetails
    ) {
        return meetingService.getMyMeetings(
                userDetails.getUsername()
        );
    }
}
