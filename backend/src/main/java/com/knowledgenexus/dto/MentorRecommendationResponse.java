package com.knowledgenexus.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class MentorRecommendationResponse {

    private UUID id;

    private String firstName;

    private String lastName;

    private String headline;

    private String company;

    private String experience;

    private List<SkillDto> skills;

    private Double matchScore;

    private String reason;
}