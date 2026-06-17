package com.knowledgenexus.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MessageResponse {

    private UUID id;

    private UUID senderId;

    private String senderName;

    private String content;

    private LocalDateTime sentAt;
}