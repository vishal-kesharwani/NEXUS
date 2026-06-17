package com.knowledgenexus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentorshipRequestResponse {

    private UUID id;

    private UUID mentorId;

    private UUID menteId;

    private UUID skillId;

    private String status;

    private String message;

    private LocalDateTime createdAt;
}