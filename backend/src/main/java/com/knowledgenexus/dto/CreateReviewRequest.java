package com.knowledgenexus.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class CreateReviewRequest {
    private UUID mentorId;
    private double rating;
    private String comment;
}
