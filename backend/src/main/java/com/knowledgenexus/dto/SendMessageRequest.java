package com.knowledgenexus.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class SendMessageRequest {

    private UUID conversationId;

    private String content;
}