package com.knowledgenexus.service;

import com.knowledgenexus.dto.CreateReviewRequest;
import com.knowledgenexus.dto.ReviewResponse;
import com.knowledgenexus.model.MentorReview;
import com.knowledgenexus.model.MentorshipRequest;
import com.knowledgenexus.model.User;
import com.knowledgenexus.repository.MentorshipRequestRepository;
import com.knowledgenexus.repository.ReviewRepository;
import com.knowledgenexus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MentorshipRequestRepository mentorshipRequestRepository;
    private final UserSkillService userSkillService;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public ReviewResponse submitReview(String reviewerEmail, CreateReviewRequest request) {
        User reviewer = currentUserService.resolve(reviewerEmail);
        User mentor = userRepository.findById(request.getMentorId()).orElseThrow();

        if (reviewer.getId().equals(mentor.getId())) {
            throw new RuntimeException("You cannot review yourself");
        }

        MentorshipRequest mentorshipRequest = null;
        if (request.getMentorshipRequestId() != null) {
            mentorshipRequest = mentorshipRequestRepository.findById(request.getMentorshipRequestId()).orElseThrow();

            if (!"ACCEPTED".equals(mentorshipRequest.getStatus())) {
                throw new RuntimeException("You can review only accepted mentorships");
            }
            if (!mentorshipRequest.getMentor().getId().equals(mentor.getId()) ||
                    !mentorshipRequest.getMentee().getId().equals(reviewer.getId())) {
                throw new RuntimeException("You can review only your own mentorship");
            }
            if (request.getSkillId() == null || !request.getSkillId().equals(mentorshipRequest.getSkill().getId())) {
                throw new RuntimeException("You can rate only the mentored skill");
            }
            if (reviewRepository.existsByMentorshipRequestId(mentorshipRequest.getId())) {
                throw new RuntimeException("You have already reviewed this mentorship");
            }
        } else if (reviewRepository.existsByMentorIdAndReviewerId(mentor.getId(), reviewer.getId())) {
            throw new RuntimeException("You have already reviewed this mentor");
        }

        MentorReview review = MentorReview.builder()
                .mentor(mentor)
                .reviewer(reviewer)
                .skill(mentorshipRequest == null ? null : mentorshipRequest.getSkill())
                .mentorshipRequest(mentorshipRequest)
                .rating(request.getRating())
                .skillLevelRating(request.getSkillLevelRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        MentorReview saved = reviewRepository.save(review);

        if (mentorshipRequest != null && request.getSkillLevelRating() != null) {
            userSkillService.applyMentorshipRating(
                    mentor,
                    mentorshipRequest.getSkill(),
                    request.getSkillLevelRating()
            );
        }

        // Notify mentor
        notificationService.createNotification(
                mentor,
                "New Review Received",
                reviewer.getFirstName() + " left you a " + request.getRating() + " star review."
        );

        return map(saved);
    }

    public List<ReviewResponse> getReviewsForMentor(UUID mentorId) {
        return reviewRepository.findByMentorIdOrderByCreatedAtDesc(mentorId)
                .stream()
                .map(this::map)
                .toList();
    }

    public Double getAverageRating(UUID mentorId) {
        Double avg = reviewRepository.getAverageRating(mentorId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    private ReviewResponse map(MentorReview review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .mentorId(review.getMentor().getId())
                .reviewerId(review.getReviewer().getId())
                .reviewerName(review.getReviewer().getFirstName() + " " + review.getReviewer().getLastName())
                .skillId(review.getSkill() == null ? null : review.getSkill().getId())
                .mentorshipRequestId(review.getMentorshipRequest() == null ? null : review.getMentorshipRequest().getId())
                .rating(review.getRating())
                .skillLevelRating(review.getSkillLevelRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
