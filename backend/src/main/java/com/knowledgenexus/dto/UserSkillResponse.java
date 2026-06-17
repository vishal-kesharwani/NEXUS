package com.knowledgenexus.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserSkillResponse {

    private UUID id;

    private UUID userId;

    private UUID skillId;

    private Integer yearsOfExperience;

    private Boolean canMentor;

    private SkillDto skill;
}