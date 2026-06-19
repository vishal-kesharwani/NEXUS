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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final ConversationRepository conversationRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final GoogleCalendarService googleCalendarService;

    /** Step 1: creator proposes a session. No link yet — status PENDING until the other side accepts. */
    public MeetingResponse scheduleMeeting(String email, CreateMeetingRequest request) {
        User creator = currentUserService.resolve(email);
        Conversation conversation = conversationRepository.findById(request.getConversationId()).orElseThrow();

        if (!conversation.getMentor().getId().equals(creator.getId()) &&
                !conversation.getMentee().getId().equals(creator.getId())) {
            throw new AccessDeniedException("Not a participant in this conversation");
        }

        User recipient = otherParticipant(conversation, creator);

        Meeting meeting = Meeting.builder()
                .conversation(conversation)
                .creator(creator)
                .scheduledAt(request.getScheduledAt())
                .status("PENDING")
                .build();

        Meeting saved = meetingRepository.save(meeting);

        notificationService.createNotification(
                recipient,
                "New Session Request",
                creator.getFirstName() + " wants to schedule a mentorship session on "
                        + request.getScheduledAt() + ". Accept to confirm and get the Meet link."
        );

        MeetingResponse response = map(saved);
        broadcast(conversation.getId(), response);
        return response;
    }

    /** Step 2: recipient accepts -> creates a real Google Calendar event with a Meet link, on the creator's calendar. */
    public MeetingResponse acceptMeeting(String email, UUID meetingId) {
        User user = currentUserService.resolve(email);
        Meeting meeting = meetingRepository.findById(meetingId).orElseThrow();
        Conversation conversation = meeting.getConversation();
        User organizer = meeting.getCreator();
        User recipient = otherParticipant(conversation, organizer);

        if (!recipient.getId().equals(user.getId())) {
            throw new AccessDeniedException("Only the invited participant can accept this session");
        }
        if (!"PENDING".equals(meeting.getStatus())) {
            throw new IllegalStateException("This session is no longer pending");
        }

        GoogleCalendarService.CreatedEvent event = googleCalendarService.createMeetEvent(
                organizer,
                recipient.getEmail(),
                meeting.getScheduledAt(),
                "Mentorship Session: " + organizer.getFirstName() + " & " + recipient.getFirstName()
        );

        meeting.setMeetLink(event.meetLink());
        meeting.setGoogleEventId(event.eventId());
        meeting.setStatus("ACCEPTED");
        Meeting saved = meetingRepository.save(meeting);

        notificationService.createNotification(
                organizer,
                "Session Confirmed",
                recipient.getFirstName() + " accepted your session. Your Google Meet link is ready."
        );

        MeetingResponse response = map(saved);
        broadcast(conversation.getId(), response);
        return response;
    }

    /** Recipient declines -> no link is ever generated. */
    public MeetingResponse declineMeeting(String email, UUID meetingId) {
        User user = currentUserService.resolve(email);
        Meeting meeting = meetingRepository.findById(meetingId).orElseThrow();
        Conversation conversation = meeting.getConversation();
        User organizer = meeting.getCreator();
        User recipient = otherParticipant(conversation, organizer);

        if (!recipient.getId().equals(user.getId())) {
            throw new AccessDeniedException("Only the invited participant can decline this session");
        }

        meeting.setStatus("DECLINED");
        Meeting saved = meetingRepository.save(meeting);

        notificationService.createNotification(
                organizer,
                "Session Declined",
                recipient.getFirstName() + " declined the proposed session."
        );

        MeetingResponse response = map(saved);
        broadcast(conversation.getId(), response);
        return response;
    }

    public List<MeetingResponse> getMeetings(String email, UUID conversationId) {
        User user = currentUserService.resolve(email);
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow();

        if (!conversation.getMentor().getId().equals(user.getId()) &&
                !conversation.getMentee().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not a participant in this conversation");
        }

        return meetingRepository.findByConversationId(conversationId)
                .stream()
                .map(this::map)
                .toList();
    }

    /** All sessions where the user is either the organizer or the invited participant. */
    public List<MeetingResponse> getMyMeetings(String email) {
        User user = currentUserService.resolve(email);
        return meetingRepository.findAllForUser(user.getId())
                .stream()
                .map(this::map)
                .toList();
    }

    private User otherParticipant(Conversation conversation, User user) {
        return conversation.getMentor().getId().equals(user.getId())
                ? conversation.getMentee()
                : conversation.getMentor();
    }

    private void broadcast(UUID conversationId, MeetingResponse response) {
        messagingTemplate.convertAndSend("/topic/meetings/" + conversationId, response);
    }

    private MeetingResponse map(Meeting meeting) {
        User creator = meeting.getCreator();
        Conversation conversation = meeting.getConversation();
        User recipient = otherParticipant(conversation, creator);

        return MeetingResponse.builder()
                .id(meeting.getId())
                .conversationId(conversation.getId())
                .creatorId(creator.getId())
                .creatorName(creator.getFirstName() + " " + creator.getLastName())
                .recipientId(recipient.getId())
                .scheduledAt(meeting.getScheduledAt())
                .meetLink(meeting.getMeetLink())
                .status(meeting.getStatus())
                .organizerGoogleConnected(creator.getGoogleRefreshToken() != null)
                .build();
    }
}