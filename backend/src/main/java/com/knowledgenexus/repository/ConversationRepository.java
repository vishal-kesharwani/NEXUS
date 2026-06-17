package com.knowledgenexus.repository;

import com.knowledgenexus.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConversationRepository
        extends JpaRepository<Conversation, UUID> {

    List<Conversation> findByMentorIdOrMenteeId(
            UUID mentorId,
            UUID menteeId
    );

    boolean existsByMentorIdAndMenteeId(
            UUID mentorId,
            UUID menteeId
    );

    long countByMentorId(UUID mentorId);
}