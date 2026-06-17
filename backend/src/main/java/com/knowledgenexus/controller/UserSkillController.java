package com.knowledgenexus.controller;

import com.knowledgenexus.dto.AddUserSkillRequest;
import com.knowledgenexus.dto.UserSkillResponse;
import com.knowledgenexus.service.UserSkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user-skills")
@RequiredArgsConstructor
public class UserSkillController {

    private final UserSkillService userSkillService;

    @PostMapping
    public UserSkillResponse addSkill(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AddUserSkillRequest request
    ) {

        return userSkillService.addSkillToUser(
                userDetails.getUsername(),
                request
        );
    }

    @GetMapping("/me")
    public List<UserSkillResponse> getSkills(
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        return userSkillService.getUserSkills(
                userDetails.getUsername()
        );
    }

    @DeleteMapping("/{id}")
    public void deleteSkill(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id
    ) {
        userSkillService.deleteSkill(userDetails.getUsername(), id);
    }
}