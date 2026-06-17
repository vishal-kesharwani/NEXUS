package com.knowledgenexus.dto;

import lombok.Builder;
import lombok.Data;

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
}