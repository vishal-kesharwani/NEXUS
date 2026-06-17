package com.knowledgenexus.repository;

import com.knowledgenexus.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MeetingRepository
        extends JpaRepository<Meeting, UUID> {

    List<Meeting> findByConversationId(
            UUID conversationId
    );

    long countByConversationIdAndStatus(
            UUID conversationId,
            String status
    );

    List<Meeting> findByCreatorId(
            UUID userId
    );
}