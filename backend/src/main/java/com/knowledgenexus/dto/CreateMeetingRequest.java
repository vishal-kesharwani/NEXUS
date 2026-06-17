package com.knowledgenexus.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateMeetingRequest {
    private UUID conversationId;
    private LocalDateTime scheduledAt;
}
