package com.knowledgenexus.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ConversationResponse {

    private UUID id;

    private UUID mentorId;

    private UUID menteeId;

    private String mentorName;

    private String menteeName;

    private String displayName;

    private UUID mentorshipRequestId;

    private UUID skillId;

    private String skillName;

    private String status;

    private LocalDateTime closedAt;
}
