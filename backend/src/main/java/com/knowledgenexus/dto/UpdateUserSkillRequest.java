package com.knowledgenexus.dto;

import com.knowledgenexus.model.SkillLevel;
import lombok.Data;

@Data
public class UpdateUserSkillRequest {
    private Integer yearsOfExperience;
    private Boolean canMentor;
    private SkillLevel selfLevel;
}
