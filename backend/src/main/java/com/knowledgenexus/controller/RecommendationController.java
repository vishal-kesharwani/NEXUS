package com.knowledgenexus.controller;

import com.knowledgenexus.dto.MentorRecommendationResponse;
import com.knowledgenexus.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public List<MentorRecommendationResponse> getRecommendations(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return recommendationService.recommend(
                userDetails.getUsername()
        );
    }
}