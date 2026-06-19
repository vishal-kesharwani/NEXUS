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

    @org.springframework.data.jpa.repository.Query(
            "select m from Meeting m where m.conversation.mentor.id = :userId or m.conversation.mentee.id = :userId"
    )
    List<Meeting> findAllForUser(
            @org.springframework.data.repository.query.Param("userId") UUID userId
    );
}