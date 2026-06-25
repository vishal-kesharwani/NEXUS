package com.knowledgenexus.dto;

import lombok.Data;
import com.knowledgenexus.model.SkillLevel;
import java.util.UUID;

@Data
public class CreateReviewRequest {
    private UUID mentorId;
    private UUID skillId;
    private UUID mentorshipRequestId;
    private double rating;
    private SkillLevel skillLevelRating;
    private String comment;
}
