package com.knowledgenexus.service;

import com.knowledgenexus.dto.MentorRecommendationResponse;
import com.knowledgenexus.dto.SkillDto;
import com.knowledgenexus.model.User;
import com.knowledgenexus.model.UserSkill;
import com.knowledgenexus.repository.UserSkillRepository;
import com.knowledgenexus.service.CurrentUserService;
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

    // ---- Scoring weights (sum of max contributions = 100) ----
    private static final int SKILL_MATCH_POINTS = 10;     // per shared skill
    private static final int MAX_SKILL_POINTS   = 40;     // cap: up to 4 shared skills counted
    private static final int EXPERIENCE_POINTS_PER_YEAR = 2;
    private static final int MAX_EXPERIENCE_POINTS = 30;  // cap: experience contributes at most 30
    private static final int SAME_COMPANY_POINTS = 15;
    private static final int SAME_ROLE_POINTS    = 15;
    private static final int MAX_POSSIBLE_SCORE =
            MAX_SKILL_POINTS + MAX_EXPERIENCE_POINTS + SAME_COMPANY_POINTS + SAME_ROLE_POINTS; // 100

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

        // Track raw (uncapped) sub-components per mentor so we can combine
        // and cap them correctly before converting to a percentage.
        Map<User, Integer> sharedSkillCount = new HashMap<>();
        Set<User> mentorsSeen = new LinkedHashSet<>();

        for (UserSkill us : mentorSkills) {

            User mentor = us.getUser();

            if (mentor.getId().equals(currentUser.getId())) {
                continue;
            }

            mentorsSeen.add(mentor);

            if (mySkillIds.contains(us.getSkill().getId())) {
                sharedSkillCount.merge(mentor, 1, Integer::sum);
            }
        }

        Map<User, Double> scores = new HashMap<>();

        for (User mentor : mentorsSeen) {

            int skillPoints = Math.min(
                    sharedSkillCount.getOrDefault(mentor, 0) * SKILL_MATCH_POINTS,
                    MAX_SKILL_POINTS
            );

            int experiencePoints = 0;
            if (mentor.getExperienceYears() != null) {
                experiencePoints = Math.min(
                        mentor.getExperienceYears() * EXPERIENCE_POINTS_PER_YEAR,
                        MAX_EXPERIENCE_POINTS
                );
            }

            int companyPoints = 0;
            if (currentUser.getCompany() != null
                    && mentor.getCompany() != null
                    && currentUser.getCompany()
                    .equalsIgnoreCase(mentor.getCompany())) {
                companyPoints = SAME_COMPANY_POINTS;
            }

            int rolePoints = 0;
            if (currentUser.getCurrentRole() != null
                    && mentor.getCurrentRole() != null
                    && currentUser.getCurrentRole()
                    .equalsIgnoreCase(mentor.getCurrentRole())) {
                rolePoints = SAME_ROLE_POINTS;
            }

            int rawTotal = skillPoints + experiencePoints + companyPoints + rolePoints;

            // Normalize to a true 0-100 percentage and clamp defensively.
            double matchPercentage = Math.min(
                    100.0,
                    Math.max(0.0, (rawTotal / (double) MAX_POSSIBLE_SCORE) * 100.0)
            );

            scores.put(mentor, matchPercentage);
        }

        return scores.entrySet()
                .stream()
                .sorted(
                        Map.Entry
                                .<User, Double>comparingByValue()
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
            Double matchPercentage
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
                .matchScore(Math.round(matchPercentage * 10.0) / 10.0) // round to 1 decimal
                .reason(generateReason(matchPercentage))
                .build();
    }

    private String generateReason(Double matchPercentage) {

        if (matchPercentage >= 70) {
            return "Excellent match based on skills and experience";
        }

        if (matchPercentage >= 40) {
            return "Good match with relevant skills";
        }

        return "Potential mentor with relevant experience";
    }
}