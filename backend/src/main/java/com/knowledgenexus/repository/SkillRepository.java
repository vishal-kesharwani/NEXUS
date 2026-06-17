package com.knowledgenexus.repository;

import com.knowledgenexus.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SkillRepository extends JpaRepository<Skill, UUID> {

    Optional<Skill> findByName(String name);

    boolean existsByName(String name);

    Optional<Skill> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<Skill> findTop20ByNameContainingIgnoreCaseOrderByNameAsc(String name);

    List<Skill> findTop20ByOrderByNameAsc();
}
