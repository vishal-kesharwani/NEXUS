package com.knowledgenexus.service;

import com.knowledgenexus.dto.ConversationResponse;
import com.knowledgenexus.dto.MessageResponse;
import com.knowledgenexus.dto.SendMessageRequest;
import com.knowledgenexus.model.Conversation;
import com.knowledgenexus.model.Message;
import com.knowledgenexus.model.User;
import com.knowledgenexus.repository.ConversationRepository;
import com.knowledgenexus.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public List<ConversationResponse> getConversations(
            String email
    ) {

        User user =
                currentUserService.resolve(email);

        return conversationRepository
                .findByMentorIdOrMenteeId(
                        user.getId(),
                        user.getId()
                )
                .stream()
                .map(c -> mapConversation(c, user))
                .toList();
    }

    public List<MessageResponse> getMessages(
            String email,
            UUID conversationId
    ) {
        User user = currentUserService.resolve(email);
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow();

        if (!conversation.getMentor().getId().equals(user.getId()) &&
                !conversation.getMentee().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not a participant in this conversation");
        }

        return messageRepository
                .findByConversationIdOrderBySentAtAsc(
                        conversationId
                )
                .stream()
                .map(this::mapMessage)
                .toList();
    }

    public MessageResponse sendMessage(
            String email,
            SendMessageRequest request
    ) {

        User sender =
                currentUserService.resolve(email);

        Conversation conversation =
                conversationRepository.findById(
                        request.getConversationId()
                ).orElseThrow();

        if (!conversation.getMentor().getId().equals(sender.getId()) &&
                !conversation.getMentee().getId().equals(sender.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not a participant in this conversation");
        }
        if ("CLOSED".equals(conversation.getStatus())) {
            throw new IllegalStateException("This mentorship chat is closed");
        }

        Message message =
                Message.builder()
                        .conversation(conversation)
                        .sender(sender)
                        .content(request.getContent())
                        .sentAt(LocalDateTime.now())
                        .build();

        Message saved = messageRepository.save(message);

        User recipient = conversation.getMentor().getId().equals(sender.getId())
                ? conversation.getMentee()
                : conversation.getMentor();

        notificationService.createNotification(
                recipient,
                "New Message",
                sender.getFirstName() + " " + sender.getLastName() + ": " + message.getContent()
        );

        return mapMessage(saved);
    }


    private ConversationResponse mapConversation(
            Conversation conversation,
            User currentUser
    ) {

        String mentorName =
                conversation.getMentor().getFirstName()
                        + " "
                        + conversation.getMentor().getLastName();

        String menteeName =
                conversation.getMentee().getFirstName()
                        + " "
                        + conversation.getMentee().getLastName();

        String displayName;

        if (conversation.getMentor()
                .getId()
                .equals(currentUser.getId())) {

            displayName = menteeName;

        } else {

            displayName = mentorName;
        }

        return ConversationResponse.builder()
                .id(conversation.getId())
                .mentorId(conversation.getMentor().getId())
                .menteeId(conversation.getMentee().getId())
                .mentorName(mentorName)
                .menteeName(menteeName)
                .displayName(displayName)
                .mentorshipRequestId(conversation.getMentorshipRequest() == null ? null : conversation.getMentorshipRequest().getId())
                .skillId(conversation.getSkill() == null ? null : conversation.getSkill().getId())
                .skillName(conversation.getSkill() == null ? null : conversation.getSkill().getName())
                .status(conversation.getStatus())
                .closedAt(conversation.getClosedAt())
                .build();
    }

    public ConversationResponse closeConversation(String email, UUID conversationId) {
        User user = currentUserService.resolve(email);
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow();

        if (!conversation.getMentor().getId().equals(user.getId()) &&
                !conversation.getMentee().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not a participant in this conversation");
        }

        conversation.setStatus("CLOSED");
        conversation.setClosedAt(LocalDateTime.now());
        return mapConversation(conversationRepository.save(conversation), user);
    }

    private MessageResponse mapMessage(
            Message message
    ) {

        return MessageResponse.builder()
                .id(message.getId())
                .senderId(
                        message.getSender().getId()
                )
                .senderName(
                        message.getSender()
                                .getFirstName()
                                + " "
                                + message.getSender()
                                .getLastName()
                )
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .build();
    }
}
