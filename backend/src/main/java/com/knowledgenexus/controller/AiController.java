package com.knowledgenexus.controller;

import com.knowledgenexus.model.User;
import com.knowledgenexus.service.AiService;
import com.knowledgenexus.service.CurrentUserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final CurrentUserService currentUserService;

    @GetMapping("/match/{mentorId}")
    public MatchReasonResponse getMatchReason(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID mentorId
    ) {
        User mentee = currentUserService.resolve(userDetails.getUsername());
        String reason = aiService.getMatchReason(mentorId, mentee.getId());
        return new MatchReasonResponse(reason);
    }

    @PostMapping("/chat")
    public ChatAssistantResponse getChatAssistant(@RequestBody ChatAssistantRequest request) {
        String response = aiService.askAiAssistant(request.getConversationId(), request.getPrompt());
        return new ChatAssistantResponse(response);
    }

    @Data
    public static class MatchReasonResponse {
        private final String reason;
    }

    @Data
    public static class ChatAssistantRequest {
        private UUID conversationId;
        private String prompt;
    }

    @Data
    public static class ChatAssistantResponse {
        private final String response;
    }
}
