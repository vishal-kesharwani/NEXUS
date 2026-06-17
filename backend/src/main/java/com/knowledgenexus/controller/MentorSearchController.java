package com.knowledgenexus.controller;

import com.knowledgenexus.dto.MentorSearchResponse;
import com.knowledgenexus.service.MentorSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
public class MentorSearchController {

    private final MentorSearchService mentorSearchService;

    @GetMapping("/search")
    public List<MentorSearchResponse> searchMentors(
            @RequestParam String skill
    ) {

        return mentorSearchService.searchMentors(skill);
    }
}