package com.knowledgenexus.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateMentorshipRequest {

    private UUID mentorId;

    private UUID skillId;

    private String message;
}