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
