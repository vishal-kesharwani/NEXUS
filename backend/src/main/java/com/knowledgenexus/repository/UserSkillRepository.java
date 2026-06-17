package com.knowledgenexus.repository;

import com.knowledgenexus.model.User;
import com.knowledgenexus.model.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserSkillRepository
        extends JpaRepository<UserSkill, UUID> {

    List<UserSkill> findByUser(User user);

    java.util.Optional<UserSkill> findByUserIdAndSkillId(UUID userId, UUID skillId);

    @Query("""
        SELECT us
        FROM UserSkill us
        JOIN FETCH us.user
        JOIN FETCH us.skill
        WHERE LOWER(us.skill.name) = LOWER(:skillName)
    """)
    List<UserSkill> findMentorsBySkill(
            @Param("skillName") String skillName
    );

    List<UserSkill> findByUserId(UUID userId);

    List<UserSkill> findByCanMentorTrue();

    long countByUserId(UUID userId);

    long countByUserIdAndCanMentorTrue(UUID userId);
}
