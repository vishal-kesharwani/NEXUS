package com.knowledgenexus.dto;

import com.knowledgenexus.model.User;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentorProfileResponse {
    private User user;
    private double averageRating;
    private List<ReviewResponse> reviews;
}
