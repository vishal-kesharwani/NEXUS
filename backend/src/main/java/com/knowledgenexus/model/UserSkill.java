package com.knowledgenexus.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSkill {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Column(name = "years_exp")
    private Integer yearsExp;

    @Column(name = "can_mentor")
    private Boolean canMentor;

    @Enumerated(EnumType.STRING)
    @Column(name = "self_level")
    private SkillLevel selfLevel;

    @Column(name = "self_score")
    private Integer selfScore;

    @Column(name = "adjusted_score")
    private Double adjustedScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjusted_level")
    private SkillLevel adjustedLevel;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (selfLevel == null) {
            selfLevel = SkillLevel.BEGINNER;
        }
        if (selfScore == null) {
            selfScore = selfLevel.score();
        }
        if (adjustedScore == null) {
            adjustedScore = (double) selfScore;
        }
        if (adjustedLevel == null) {
            adjustedLevel = SkillLevel.fromScore(adjustedScore);
        }
    }
}
