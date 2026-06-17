package com.knowledgenexus.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private long totalSkills;

    private long mentoringSkills;

    private long sentRequests;

    private long receivedRequests;

    private long recommendations;

    // Analytics metrics
    private long totalMentees;
    private long activeChats;
    private long sessionsConducted;
    private double averageRating;

    private long acceptedRequests;
    private long skillsLearned;
    private long upcomingSessions;
}