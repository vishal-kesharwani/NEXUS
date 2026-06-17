package com.knowledgenexus.controller;

import com.knowledgenexus.dto.CreateSkillRequest;
import com.knowledgenexus.model.Skill;
import com.knowledgenexus.repository.SkillRepository;
import com.knowledgenexus.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;
    private final SkillRepository skillRepository;

    @PostMapping
    public Skill create(
            @Valid @RequestBody CreateSkillRequest request
    ) {
        return skillService.create(request);
    }

    @GetMapping
    public List<Skill> getAll() {
        return skillRepository.findAll();
    }

    @GetMapping("/catalog")
    public List<Skill> getCatalog(
            @RequestParam(required = false, defaultValue = "") String query
    ) {
        return skillService.getCatalog(query);
    }
}
