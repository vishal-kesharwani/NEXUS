package com.knowledgenexus.service;

import com.knowledgenexus.dto.MentorRecommendationResponse;
import com.knowledgenexus.dto.SkillDto;
import com.knowledgenexus.model.User;
import com.knowledgenexus.model.UserSkill;
import com.knowledgenexus.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final CurrentUserService currentUserService;
    private final UserSkillRepository userSkillRepository;

    public List<MentorRecommendationResponse> recommend(
            String email
    ) {

        User currentUser =
                currentUserService.resolve(email);

        List<UserSkill> currentSkills =
                userSkillRepository.findByUserId(
                        currentUser.getId()
                );

        Set<UUID> mySkillIds =
                currentSkills.stream()
                        .map(us -> us.getSkill().getId())
                        .collect(Collectors.toSet());

        List<UserSkill> mentorSkills =
                userSkillRepository.findByCanMentorTrue();

        Map<User, Integer> scores =
                new HashMap<>();

        for (UserSkill us : mentorSkills) {

            User mentor = us.getUser();

            if (mentor.getId().equals(currentUser.getId())) {
                continue;
            }

            int score =
                    scores.getOrDefault(mentor, 0);

            if (mySkillIds.contains(
                    us.getSkill().getId())) {
                score += 10;
            }

            if (mentor.getExperienceYears() != null) {
                score += mentor.getExperienceYears() * 2;
            }

            if (currentUser.getCompany() != null
                    && mentor.getCompany() != null
                    && currentUser.getCompany()
                    .equalsIgnoreCase(
                            mentor.getCompany())) {

                score += 5;
            }

            if (currentUser.getCurrentRole() != null
                    && mentor.getCurrentRole() != null
                    && currentUser.getCurrentRole()
                    .equalsIgnoreCase(
                            mentor.getCurrentRole())) {

                score += 5;
            }

            scores.put(mentor, score);
        }

        return scores.entrySet()
                .stream()
                .sorted(
                        Map.Entry
                                .<User, Integer>comparingByValue()
                                .reversed()
                )
                .limit(10)
                .map(entry ->
                        mapToResponse(
                                entry.getKey(),
                                entry.getValue()
                        )
                )
                .toList();
    }

    private MentorRecommendationResponse mapToResponse(
            User mentor,
            Integer score
    ) {

        List<SkillDto> skills =
                userSkillRepository.findByUserId(
                                mentor.getId()
                        )
                        .stream()
                        .filter(us ->
                                Boolean.TRUE.equals(
                                        us.getCanMentor()
                                )
                        )
                        .map(UserSkill::getSkill)
                        .map(skill ->
                                SkillDto.builder()
                                        .id(skill.getId())
                                        .name(skill.getName())
                                        .category(skill.getCategory())
                                        .build()
                        )
                        .toList();

        return MentorRecommendationResponse.builder()
                .id(mentor.getId())
                .firstName(mentor.getFirstName())
                .lastName(mentor.getLastName())
                .headline(mentor.getHeadline())
                .company(mentor.getCompany())
                .experience(
                        mentor.getExperienceYears() != null
                                ? mentor.getExperienceYears() + " years"
                                : null
                )
                .skills(skills)
                .matchScore(score.doubleValue())
                .reason(generateReason(score))
                .build();
    }

    private String generateReason(Integer score) {

        if (score >= 25) {
            return "Excellent match based on skills and experience";
        }

        if (score >= 15) {
            return "Good match with relevant skills";
        }

        return "Potential mentor with relevant experience";
    }
}