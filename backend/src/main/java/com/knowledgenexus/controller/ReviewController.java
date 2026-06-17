package com.knowledgenexus.controller;

import com.knowledgenexus.dto.CreateReviewRequest;
import com.knowledgenexus.dto.ReviewResponse;
import com.knowledgenexus.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ReviewResponse submitReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateReviewRequest request
    ) {
        return reviewService.submitReview(userDetails.getUsername(), request);
    }

    @GetMapping("/mentor/{mentorId}")
    public List<ReviewResponse> getReviews(
            @PathVariable UUID mentorId
    ) {
        return reviewService.getReviewsForMentor(mentorId);
    }

    @GetMapping("/mentor/{mentorId}/average")
    public Double getAverageRating(
            @PathVariable UUID mentorId
    ) {
        return reviewService.getAverageRating(mentorId);
    }
}
