package com.knowledgenexus.controller;

import com.knowledgenexus.dto.MessageResponse;
import com.knowledgenexus.dto.SendMessageRequest;
import com.knowledgenexus.service.ChatService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload SendMessageRequest request, Principal principal) {
        MessageResponse response = chatService.sendMessage(principal.getName(), request);
        messagingTemplate.convertAndSend(
                "/topic/messages/" + request.getConversationId(),
                response
        );
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + request.getConversationId(),
                response
        );
    }

    @MessageMapping("/chat.typing")
    public void broadcastTyping(@Payload TypingStatus typingStatus) {
        messagingTemplate.convertAndSend(
                "/topic/typing/" + typingStatus.getConversationId(),
                typingStatus
        );
    }

    @MessageMapping("/chat.presence")
    public void broadcastPresence(@Payload PresenceStatus presenceStatus) {
        messagingTemplate.convertAndSend(
                "/topic/presence",
                presenceStatus
        );
    }

    @Data
    public static class TypingStatus {
        private UUID conversationId;
        private UUID userId;
        private String username;
        private boolean isTyping;
    }

    @Data
    public static class PresenceStatus {
        private UUID userId;
        private String status;
    }
}
