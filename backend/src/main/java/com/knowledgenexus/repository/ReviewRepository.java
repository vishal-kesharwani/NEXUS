package com.knowledgenexus.repository;

import com.knowledgenexus.model.MentorReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<MentorReview, UUID> {
    List<MentorReview> findByMentorIdOrderByCreatedAtDesc(UUID mentorId);
    
    boolean existsByMentorIdAndReviewerId(UUID mentorId, UUID reviewerId);

    boolean existsByMentorshipRequestId(UUID mentorshipRequestId);
    
    @Query("SELECT AVG(r.rating) FROM MentorReview r WHERE r.mentor.id = :mentorId")
    Double getAverageRating(@Param("mentorId") UUID mentorId);
}
