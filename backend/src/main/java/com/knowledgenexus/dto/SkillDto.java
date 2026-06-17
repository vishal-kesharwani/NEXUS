package com.knowledgenexus.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SkillDto {

    private UUID id;
    private String name;
    private String category;
}