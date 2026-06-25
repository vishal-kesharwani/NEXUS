package com.knowledgenexus.service;

import com.knowledgenexus.dto.CreateMentorshipRequest;
import com.knowledgenexus.dto.MentorshipRequestResponse;
import com.knowledgenexus.model.Conversation;
import com.knowledgenexus.model.MentorshipRequest;
import com.knowledgenexus.model.Skill;
import com.knowledgenexus.model.User;
import com.knowledgenexus.repository.MentorshipRequestRepository;
import com.knowledgenexus.repository.ConversationRepository;
import com.knowledgenexus.repository.SkillRepository;
import com.knowledgenexus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MentorshipRequestService {

    private final MentorshipRequestRepository requestRepository;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final SkillRepository skillRepository;
    private final NotificationService notificationService;

    public MentorshipRequestResponse create(
            String email,
            CreateMentorshipRequest request
    ) {

        User mentee = currentUserService.resolve(email);

        User mentor = userRepository.findById(request.getMentorId())
                .orElseThrow();

        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        MentorshipRequest mentorshipRequest =
                MentorshipRequest.builder()
                        .mentor(mentor)
                        .mentee(mentee)
                        .skill(skill)
                        .status("PENDING")
                        .message(request.getMessage())
                        .createdAt(LocalDateTime.now())
                        .build();

        requestRepository.save(mentorshipRequest);

        notificationService.createNotification(
                mentor,
                "New Mentorship Request",
                mentee.getFirstName() + " " + mentee.getLastName() + " requested mentorship for " + skill.getName()
        );

        return map(mentorshipRequest);
    }

    public List<MentorshipRequestResponse> received(String email) {

        User mentor = currentUserService.resolve(email);

        return requestRepository.findByMentor(mentor)
                .stream()
                .map(this::map)
                .toList();
    }

    public List<MentorshipRequestResponse> sent(String email) {

        User mentee = currentUserService.resolve(email);

        return requestRepository.findByMentee(mentee)
                .stream()
                .map(this::map)
                .toList();
    }

    public MentorshipRequestResponse accept(UUID id) {

        MentorshipRequest request =
                requestRepository.findById(id)
                        .orElseThrow();

        request.setStatus("ACCEPTED");

        requestRepository.save(request);

        notificationService.createNotification(
                request.getMentee(),
                "Mentorship Request Accepted",
                request.getMentor().getFirstName() + " " + request.getMentor().getLastName() + " accepted your mentorship request for " + request.getSkill().getName()
        );

        Conversation conversation =
                Conversation.builder()
                        .mentor(request.getMentor())
                        .mentee(request.getMentee())
                        .mentorshipRequest(request)
                        .skill(request.getSkill())
                        .status("ACTIVE")
                        .createdAt(LocalDateTime.now())
                        .build();

        conversationRepository.save(conversation);

        return map(request);
    }

    public MentorshipRequestResponse reject(UUID id) {

        MentorshipRequest request =
                requestRepository.findById(id)
                        .orElseThrow();

        request.setStatus("REJECTED");

        requestRepository.save(request);

        notificationService.createNotification(
                request.getMentee(),
                "Mentorship Request Rejected",
                request.getMentor().getFirstName() + " " + request.getMentor().getLastName() + " declined your mentorship request for " + request.getSkill().getName()
        );

        return map(request);
    }

    private MentorshipRequestResponse map(
            MentorshipRequest request
    ) {

        return MentorshipRequestResponse.builder()
                .id(request.getId())
                .mentorId(request.getMentor().getId())
                .menteId(request.getMentee().getId())
                .skillId(request.getSkill().getId())
                .status(request.getStatus())
                .message(request.getMessage())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
