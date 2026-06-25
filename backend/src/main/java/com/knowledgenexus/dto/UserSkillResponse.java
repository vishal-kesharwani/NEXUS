package com.knowledgenexus.dto;

import lombok.Builder;
import lombok.Data;
import com.knowledgenexus.model.SkillLevel;

import java.util.UUID;

@Data
@Builder
public class UserSkillResponse {

    private UUID id;

    private UUID userId;

    private UUID skillId;

    private Integer yearsOfExperience;

    private Boolean canMentor;

    private SkillLevel selfLevel;

    private Integer selfScore;

    private Double adjustedScore;

    private SkillLevel adjustedLevel;

    private SkillDto skill;
}
