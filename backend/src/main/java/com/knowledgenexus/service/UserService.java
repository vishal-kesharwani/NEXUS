package com.knowledgenexus.service;

import com.knowledgenexus.dto.ReviewResponse;
import com.knowledgenexus.dto.UpdateProfileRequest;
import com.knowledgenexus.model.User;
import com.knowledgenexus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final ReviewService reviewService;

    public User getCurrentUser(String email) {
        return currentUserService.resolve(email);
    }

    public User updateProfile(String email, UpdateProfileRequest userData) {
        User user = currentUserService.resolve(email);

        if (userData.getFirstName() != null) {
            user.setFirstName(userData.getFirstName());
        }
        if (userData.getLastName() != null) {
            user.setLastName(userData.getLastName());
        }
        if (userData.getHeadline() != null) {
            user.setHeadline(userData.getHeadline());
        }
        if (userData.getBio() != null) {
            user.setBio(userData.getBio());
        }
        if (userData.getExperience() != null) {
            user.setExperienceYears(userData.getExperience());
        }
        if (userData.getRole() != null) {
            user.setCurrentRole(userData.getRole());
        }
        if (userData.getCompany() != null) {
            user.setCompany(userData.getCompany());
        }
        if (userData.getLocation() != null) {
            user.setLocation(userData.getLocation());
        }
        if (userData.getLinkedIn() != null) {
            user.setLinkedinUrl(userData.getLinkedIn());
        }
        if (userData.getGithub() != null) {
            user.setGithubUrl(userData.getGithub());
        }

        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public com.knowledgenexus.dto.MentorProfileResponse getMentorProfile(java.util.UUID id) {
        User user = userRepository.findById(id).orElseThrow();
        double avgRating = reviewService.getAverageRating(id);
        List<ReviewResponse> reviews = reviewService.getReviewsForMentor(id);
        return com.knowledgenexus.dto.MentorProfileResponse.builder()
                .user(user)
                .averageRating(avgRating)
                .reviews(reviews)
                .build();
    }
}