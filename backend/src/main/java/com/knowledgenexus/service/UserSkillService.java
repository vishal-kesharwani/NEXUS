package com.knowledgenexus.service;

import com.knowledgenexus.dto.AddUserSkillRequest;
import com.knowledgenexus.dto.SkillDto;
import com.knowledgenexus.dto.UpdateUserSkillRequest;
import com.knowledgenexus.dto.UserSkillResponse;
import com.knowledgenexus.model.Skill;
import com.knowledgenexus.model.SkillLevel;
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
                .selfLevel(userSkill.getSelfLevel())
                .selfScore(userSkill.getSelfScore())
                .adjustedScore(userSkill.getAdjustedScore())
                .adjustedLevel(userSkill.getAdjustedLevel())
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
        SkillLevel selfLevel = request.getSelfLevel() == null
                ? SkillLevel.BEGINNER
                : request.getSelfLevel();
        int selfScore = selfLevel.score();

        return userSkillRepository
                .findByUserIdAndSkillId(
                        user.getId(),
                        skill.getId()
                )
                .map(existing -> {

                    existing.setYearsExp(yearsOfExperience);
                    existing.setCanMentor(canMentor);
                    existing.setSelfLevel(selfLevel);
                    existing.setSelfScore(selfScore);
                    existing.setAdjustedScore((double) selfScore);
                    existing.setAdjustedLevel(selfLevel);

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
                                    .selfLevel(selfLevel)
                                    .selfScore(selfScore)
                                    .adjustedScore((double) selfScore)
                                    .adjustedLevel(selfLevel)
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

    public UserSkillResponse updateSkill(
            String email,
            UUID id,
            UpdateUserSkillRequest request
    ) {
        User user = currentUserService.resolve(email);

        UserSkill userSkill = userSkillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        if (!userSkill.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        if (request.getYearsOfExperience() != null) {
            userSkill.setYearsExp(Math.max(0, request.getYearsOfExperience()));
        }
        if (request.getCanMentor() != null) {
            userSkill.setCanMentor(request.getCanMentor());
        }
        if (request.getSelfLevel() != null) {
            int previousSelfScore = userSkill.getSelfScore() == null ? 0 : userSkill.getSelfScore();
            double reviewInfluence = userSkill.getAdjustedScore() == null
                    ? previousSelfScore
                    : userSkill.getAdjustedScore();
            double reviewDelta = previousSelfScore == 0 ? 0 : reviewInfluence - previousSelfScore;
            int selfScore = request.getSelfLevel().score();

            userSkill.setSelfLevel(request.getSelfLevel());
            userSkill.setSelfScore(selfScore);
            userSkill.setAdjustedScore(Math.max(0, Math.min(100, selfScore + reviewDelta)));
            userSkill.setAdjustedLevel(SkillLevel.fromScore(userSkill.getAdjustedScore()));
        }

        return mapToResponse(userSkillRepository.save(userSkill));
    }

    public void applyMentorshipRating(User mentor, Skill skill, SkillLevel menteeRating) {
        UserSkill userSkill = userSkillRepository.findByUserIdAndSkillId(mentor.getId(), skill.getId())
                .orElseThrow(() -> new RuntimeException("Mentor does not have this skill"));

        int selfScore = userSkill.getSelfScore() == null
                ? (userSkill.getSelfLevel() == null ? SkillLevel.BEGINNER.score() : userSkill.getSelfLevel().score())
                : userSkill.getSelfScore();
        double adjustedScore = (selfScore + menteeRating.score()) / 2.0;

        userSkill.setAdjustedScore(adjustedScore);
        userSkill.setAdjustedLevel(SkillLevel.fromScore(adjustedScore));
        userSkillRepository.save(userSkill);
    }
}
