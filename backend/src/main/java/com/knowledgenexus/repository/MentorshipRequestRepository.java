package com.knowledgenexus.repository;

import com.knowledgenexus.model.MentorshipRequest;
import com.knowledgenexus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MentorshipRequestRepository
        extends JpaRepository<MentorshipRequest, UUID> {

    List<MentorshipRequest> findByMentor(User mentor);

    List<MentorshipRequest> findByMentee(User mentee);

    long countByMenteeId(UUID menteeId);

    long countByMentorId(UUID mentorId);

    long countByMenteeIdAndStatus(UUID menteeId, String status);

}