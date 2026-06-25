package com.knowledgenexus.dto;

import lombok.*;
import com.knowledgenexus.model.SkillLevel;
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
    private UUID skillId;
    private UUID mentorshipRequestId;
    private double rating;
    private SkillLevel skillLevelRating;
    private String comment;
    private LocalDateTime createdAt;
}
