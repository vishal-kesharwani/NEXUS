package com.knowledgenexus.service;

import com.knowledgenexus.dto.MentorAvailabilityRequest;
import com.knowledgenexus.dto.MentorAvailabilityResponse;
import com.knowledgenexus.model.MentorAvailability;
import com.knowledgenexus.model.User;
import com.knowledgenexus.repository.MentorAvailabilityRepository;
import com.knowledgenexus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MentorAvailabilityService {

    private final MentorAvailabilityRepository availabilityRepository;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    public MentorAvailabilityResponse create(
            String email,
            MentorAvailabilityRequest request
    ) {

        User mentor = currentUserService.resolve(email);

        MentorAvailability availability =
                MentorAvailability.builder()
                        .mentor(mentor)
                        .dayOfWeek(request.getDayOfWeek())
                        .startTime(request.getStartTime())
                        .endTime(request.getEndTime())
                        .createdAt(LocalDateTime.now())
                        .build();

        availabilityRepository.save(availability);

        return mapToResponse(availability);
    }

    public List<MentorAvailabilityResponse> getMentorAvailability(String mentorId) {
        try {
            UUID mentorUUID = UUID.fromString(mentorId);
            User mentor = userRepository.findById(mentorUUID)
                    .orElseThrow(() -> new RuntimeException("Mentor not found"));
            return availabilityRepository.findByMentor(mentor)
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid mentor ID format");
        }
    }

    public void deleteAvailability(String email, String availabilityId) {
        try {
            UUID availUUID = UUID.fromString(availabilityId);
            User user = currentUserService.resolve(email);

            MentorAvailability availability = availabilityRepository.findById(availUUID)
                    .orElseThrow(() -> new RuntimeException("Availability not found"));

            if (!availability.getMentor().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized: Cannot delete others' availability");
            }

            availabilityRepository.delete(availability);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid availability ID format");
        }
    }

    public List<MentorAvailabilityResponse> getMyAvailability(
            String email
    ) {

        User mentor = currentUserService.resolve(email);

        return availabilityRepository.findByMentor(mentor)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private MentorAvailabilityResponse mapToResponse(
            MentorAvailability availability
    ) {
        return MentorAvailabilityResponse.builder()
                .id(availability.getId())
                .mentorId(availability.getMentor().getId())
                .dayOfWeek(availability.getDayOfWeek())
                .startTime(availability.getStartTime())
                .endTime(availability.getEndTime())
                .build();
    }
}
