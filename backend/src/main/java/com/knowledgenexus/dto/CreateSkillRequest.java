package com.knowledgenexus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSkillRequest {

    @NotBlank
    private String name;
}