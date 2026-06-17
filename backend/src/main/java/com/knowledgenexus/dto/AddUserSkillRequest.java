package com.knowledgenexus.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AddUserSkillRequest {

    private UUID skillId;

    private Integer yearsOfExperience;

    private Boolean canMentor;
}