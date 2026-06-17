package com.knowledgenexus.service;

import com.knowledgenexus.dto.CreateMeetingRequest;
import com.knowledgenexus.dto.MeetingResponse;
import com.knowledgenexus.model.Conversation;
import com.knowledgenexus.model.Meeting;
import com.knowledgenexus.model.User;
import com.knowledgenexus.repository.ConversationRepository;
import com.knowledgenexus.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final ConversationRepository conversationRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    public MeetingResponse scheduleMeeting(String email, CreateMeetingRequest request) {
        User creator = currentUserService.resolve(email);
        Conversation conversation = conversationRepository.findById(request.getConversationId()).orElseThrow();

        // Verify participant
        if (!conversation.getMentor().getId().equals(creator.getId()) &&
                !conversation.getMentee().getId().equals(creator.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not a participant in this conversation");
        }

        String meetLink = generateMeetLink();

        Meeting meeting = Meeting.builder()
                .conversation(conversation)
                .creator(creator)
                .scheduledAt(request.getScheduledAt())
                .meetLink(meetLink)
                .status("SCHEDULED")
                .build();

        Meeting saved = meetingRepository.save(meeting);

        // Notify other participant
        User recipient = conversation.getMentor().getId().equals(creator.getId())
                ? conversation.getMentee()
                : conversation.getMentor();

        notificationService.createNotification(
                recipient,
                "New Session Scheduled",
                creator.getFirstName() + " scheduled a mentorship session on " + request.getScheduledAt()
        );

        MeetingResponse response = map(saved);

        // Notify conversation websocket
        messagingTemplate.convertAndSend(
                "/topic/meetings/" + conversation.getId(),
                response
        );

        return response;
    }

    public List<MeetingResponse> getMeetings(String email, UUID conversationId) {
        User user = currentUserService.resolve(email);
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow();

        if (!conversation.getMentor().getId().equals(user.getId()) &&
                !conversation.getMentee().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not a participant in this conversation");
        }

        return meetingRepository.findByConversationId(conversationId)
                .stream()
                .map(this::map)
                .toList();
    }


    private String generateMeetLink() {
        String chars = "abcdefghijklmnopqrstuvwxyz";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder("https://meet.google.com/");
        for (int i = 0; i < 3; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        sb.append("-");
        for (int i = 0; i < 4; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        sb.append("-");
        for (int i = 0; i < 3; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private MeetingResponse map(Meeting meeting) {
        return MeetingResponse.builder()
                .id(meeting.getId())
                .conversationId(meeting.getConversation().getId())
                .creatorId(meeting.getCreator().getId())
                .creatorName(meeting.getCreator().getFirstName() + " " + meeting.getCreator().getLastName())
                .scheduledAt(meeting.getScheduledAt())
                .meetLink(meeting.getMeetLink())
                .status(meeting.getStatus())
                .build();
    }

    public List<MeetingResponse> getMyMeetings(
            String email
    ) {

        User user =
                currentUserService.resolve(email);

        return meetingRepository
                .findByCreatorId(user.getId())
                .stream()
                .map(this::map)
                .toList();
    }

    private MeetingResponse mapToResponse(Meeting meeting) {

        return MeetingResponse.builder()
                .id(meeting.getId())
                .conversationId(meeting.getConversation().getId())
                .creatorId(meeting.getCreator().getId())
                .creatorName(
                        meeting.getCreator().getFirstName()
                                + " "
                                + meeting.getCreator().getLastName()
                )
                .scheduledAt(meeting.getScheduledAt())
                .meetLink(meeting.getMeetLink())
                .status(meeting.getStatus())
                .build();
    }


    
}
