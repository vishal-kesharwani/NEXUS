package com.knowledgenexus.repository;

import com.knowledgenexus.model.MentorAvailability;
import com.knowledgenexus.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MentorAvailabilityRepository
        extends JpaRepository<MentorAvailability, UUID> {

    List<MentorAvailability> findByMentor(User mentor);
}