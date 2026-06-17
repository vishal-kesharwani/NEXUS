package com.knowledgenexus.service;

import com.knowledgenexus.dto.AddUserSkillRequest;
import com.knowledgenexus.dto.SkillDto;
import com.knowledgenexus.dto.UserSkillResponse;
import com.knowledgenexus.model.Skill;
import com.knowledgenexus.model.User;
import com.knowledgenexus.model.UserSkill;
import com.knowledgenexus.repository.SkillRepository;
import com.knowledgenexus.repository.UserRepository;
import com.knowledgenexus.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserSkillService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;

    private UserSkillResponse mapToResponse(UserSkill userSkill) {

        Skill skill = userSkill.getSkill();

        SkillDto skillDto = SkillDto.builder()
                .id(skill.getId())
                .name(skill.getName())
                .category(skill.getCategory())
                .build();

        return UserSkillResponse.builder()
                .id(userSkill.getId())
                .userId(userSkill.getUser().getId())
                .skillId(skill.getId())
                .yearsOfExperience(userSkill.getYearsExp())
                .canMentor(userSkill.getCanMentor())
                .skill(skillDto)
                .build();
    }

    public UserSkillResponse addSkillToUser(
            String email,
            AddUserSkillRequest request
    ) {

        if (request.getSkillId() == null) {
            throw new RuntimeException("Skill is required");
        }

        User user = currentUserService.resolve(email);

        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() ->
                        new RuntimeException("Skill not found"));

        int yearsOfExperience = request.getYearsOfExperience() == null
                ? 0
                : Math.max(0, request.getYearsOfExperience());

        boolean canMentor =
                Boolean.TRUE.equals(request.getCanMentor());

        return userSkillRepository
                .findByUserIdAndSkillId(
                        user.getId(),
                        skill.getId()
                )
                .map(existing -> {

                    existing.setYearsExp(yearsOfExperience);
                    existing.setCanMentor(canMentor);

                    UserSkill saved =
                            userSkillRepository.save(existing);

                    return mapToResponse(saved);
                })
                .orElseGet(() -> {

                    UserSkill userSkill =
                            UserSkill.builder()
                                    .user(user)
                                    .skill(skill)
                                    .yearsExp(yearsOfExperience)
                                    .canMentor(canMentor)
                                    .build();

                    UserSkill saved =
                            userSkillRepository.save(userSkill);

                    return mapToResponse(saved);
                });
    }

    public List<UserSkillResponse> getUserSkills(
            String email
    ) {

        User user = currentUserService.resolve(email);

        return userSkillRepository
                .findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteSkill(
            String email,
            UUID skillId
    ) {

        User user = currentUserService.resolve(email);

        UserSkill userSkill =
                userSkillRepository.findById(skillId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Skill not found"
                                ));

        if (!userSkill.getUser()
                .getId()
                .equals(user.getId())) {

            throw new RuntimeException(
                    "Unauthorized"
            );
        }

        userSkillRepository.delete(userSkill);
    }
}