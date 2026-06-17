package com.knowledgenexus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentorAvailabilityResponse {

    private UUID id;
    private UUID mentorId;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
}