package com.knowledgenexus.controller;

import com.knowledgenexus.dto.ConversationResponse;
import com.knowledgenexus.dto.MessageResponse;
import com.knowledgenexus.dto.SendMessageRequest;
import com.knowledgenexus.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/conversations")
    public List<ConversationResponse> conversations(
            @AuthenticationPrincipal
            UserDetails userDetails
    ) {

        return chatService.getConversations(
                userDetails.getUsername()
        );
    }

    @GetMapping("/{conversationId}")
    public List<MessageResponse> messages(
            @AuthenticationPrincipal
            UserDetails userDetails,
            @PathVariable UUID conversationId
    ) {

        return chatService.getMessages(
                userDetails.getUsername(),
                conversationId
        );
    }

    @PostMapping("/send")
    public MessageResponse send(
            @AuthenticationPrincipal
            UserDetails userDetails,

            @RequestBody
            SendMessageRequest request
    ) {

        return chatService.sendMessage(
                userDetails.getUsername(),
                request
        );
    }

    @PostMapping("/conversations/{conversationId}/close")
    public ConversationResponse closeConversation(
            @AuthenticationPrincipal
            UserDetails userDetails,
            @PathVariable UUID conversationId
    ) {
        return chatService.closeConversation(userDetails.getUsername(), conversationId);
    }
}
