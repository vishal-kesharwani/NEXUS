package com.knowledgenexus.dto;

import lombok.Data;

@Data
public class MentorAvailabilityRequest {

    private String dayOfWeek;
    private String startTime;
    private String endTime;
}