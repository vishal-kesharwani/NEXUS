package com.knowledgenexus.service;

import com.knowledgenexus.dto.MentorSearchResponse;
import com.knowledgenexus.model.MentorAvailability;
import com.knowledgenexus.model.Skill;
import com.knowledgenexus.model.User;
import com.knowledgenexus.model.UserSkill;
import com.knowledgenexus.repository.MentorAvailabilityRepository;
import com.knowledgenexus.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MentorSearchService {

    private final UserSkillRepository userSkillRepository;
    private final MentorAvailabilityRepository availabilityRepository;

    public List<MentorSearchResponse> searchMentors(
            String skillName
    ) {

        List<UserSkill> userSkills =
                userSkillRepository.findMentorsBySkill(skillName);

        return userSkills.stream()
                .filter(us -> Boolean.TRUE.equals(us.getCanMentor()))
                .map(UserSkill::getUser)
                .distinct()
                .map(this::mapToResponse)
                .toList();
    }

    private MentorSearchResponse mapToResponse(
            User user
    ) {
        // Fetch all skills where user can mentor
        List<UserSkill> mentorSkills = userSkillRepository.findByUserId(user.getId())
                .stream()
                .filter(us -> Boolean.TRUE.equals(us.getCanMentor()))
                .collect(Collectors.toList());

        List<Skill> skills = mentorSkills.stream()
                .map(UserSkill::getSkill)
                .collect(Collectors.toList());

        // Fetch availability
        List<MentorAvailability> availability = availabilityRepository.findByMentor(user);

        return MentorSearchResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .headline(user.getHeadline())
                .company(user.getCompany())
                .skills(skills)
                .availability(availability)
                .build();
    }
}