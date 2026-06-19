package com.knowledgenexus.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingResponse {
    private UUID id;
    private UUID conversationId;
    private UUID creatorId;
    private String creatorName;
    private UUID recipientId;
    private LocalDateTime scheduledAt;
    private String meetLink;
    private String status;
}