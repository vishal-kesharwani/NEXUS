package com.knowledgenexus.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private UUID id;
    private UUID mentorId;
    private UUID reviewerId;
    private String reviewerName;
    private double rating;
    private String comment;
    private LocalDateTime createdAt;
}
