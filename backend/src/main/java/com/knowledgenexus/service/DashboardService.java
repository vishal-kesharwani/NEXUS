package com.knowledgenexus.service;

import com.knowledgenexus.dto.DashboardResponse;
import com.knowledgenexus.model.User;
import com.knowledgenexus.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CurrentUserService currentUserService;
    private final UserSkillRepository userSkillRepository;
    private final MentorshipRequestRepository mentorshipRequestRepository;
    private final RecommendationService recommendationService;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MeetingRepository meetingRepository;
    private final ReviewService reviewService;

    public DashboardResponse getDashboard(String email) {

        User user = currentUserService.resolve(email);

        long totalSkills = userSkillRepository.countByUserId(user.getId());
        long mentoringSkills = userSkillRepository.countByUserIdAndCanMentorTrue(user.getId());
        long skillsLearned = totalSkills - mentoringSkills;

        long totalMentees = conversationRepository.countByMentorId(user.getId());

        long activeChats = conversationRepository.findByMentorIdOrMenteeId(user.getId(), user.getId())
                .stream()
                .filter(c -> messageRepository.countByConversationId(c.getId()) > 0)
                .count();

        long unreadChats = conversationRepository.findByMentorIdOrMenteeId(user.getId(), user.getId())
                .stream()
                .filter(c -> hasUnreadMessages(c, user))
                .count();

        long sessionsConducted = conversationRepository.findByMentorIdOrMenteeId(user.getId(), user.getId())
                .stream()
                .flatMap(c -> meetingRepository.findByConversationId(c.getId()).stream())
                .filter(m -> m.getScheduledAt().isBefore(LocalDateTime.now()) || "COMPLETED".equals(m.getStatus()))
                .count();

        double averageRating = reviewService.getAverageRating(user.getId());

        long acceptedRequests = mentorshipRequestRepository.countByMenteeIdAndStatus(user.getId(), "ACCEPTED");
        long pendingReceivedRequests = mentorshipRequestRepository.countByMentorIdAndStatus(user.getId(), "PENDING");

        long pendingMeetings = conversationRepository.findByMentorIdOrMenteeId(user.getId(), user.getId())
                .stream()
                .flatMap(c -> meetingRepository.findByConversationId(c.getId()).stream())
                .filter(m -> "PENDING".equals(m.getStatus()))
                .count();

        long upcomingSessions = conversationRepository.findByMentorIdOrMenteeId(user.getId(), user.getId())
                .stream()
                .flatMap(c -> meetingRepository.findByConversationId(c.getId()).stream())
                .filter(m -> m.getScheduledAt().isAfter(LocalDateTime.now()) && !"CANCELLED".equals(m.getStatus()))
                .count();

        return DashboardResponse.builder()
                .totalSkills(totalSkills)
                .mentoringSkills(mentoringSkills)
                .sentRequests(mentorshipRequestRepository.countByMenteeId(user.getId()))
                .receivedRequests(mentorshipRequestRepository.countByMentorId(user.getId()))
                .recommendations(recommendationService.recommend(email).size())
                .pendingReceivedRequests(pendingReceivedRequests)
                .pendingMeetings(pendingMeetings)
                .unreadChats(unreadChats)
                .totalMentees(totalMentees)
                .activeChats(activeChats)
                .sessionsConducted(sessionsConducted)
                .averageRating(averageRating)
                .acceptedRequests(acceptedRequests)
                .skillsLearned(skillsLearned)
                .upcomingSessions(upcomingSessions)
                .build();
    }

    private boolean hasUnreadMessages(com.knowledgenexus.model.Conversation conversation, User currentUser) {
        var latestMessage = messageRepository.findTopByConversationIdOrderBySentAtDesc(conversation.getId()).orElse(null);
        if (latestMessage == null || latestMessage.getSender().getId().equals(currentUser.getId())) {
            return false;
        }

        LocalDateTime lastReadAt = conversation.getMentor().getId().equals(currentUser.getId())
                ? conversation.getMentorLastReadAt()
                : conversation.getMenteeLastReadAt();
        return lastReadAt == null || latestMessage.getSentAt().isAfter(lastReadAt);
    }
}
