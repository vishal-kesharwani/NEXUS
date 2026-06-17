package com.knowledgenexus.dto;

import com.knowledgenexus.model.MentorAvailability;
import com.knowledgenexus.model.Skill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentorSearchResponse {

    private UUID id;

    private String firstName;

    private String lastName;

    private String headline;

    private String company;

    private List<Skill> skills;

    private List<MentorAvailability> availability;
}