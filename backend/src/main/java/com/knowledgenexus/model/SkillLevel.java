package com.knowledgenexus.model;

public enum SkillLevel {
    BEGINNER(10),
    INTERMEDIATE(30),
    PRO(50),
    ADVANCED(72),
    SUPERIOR(92);

    private final int score;

    SkillLevel(int score) {
        this.score = score;
    }

    public int score() {
        return score;
    }

    public static SkillLevel fromScore(double score) {
        if (score < 20) {
            return BEGINNER;
        }
        if (score < 40) {
            return INTERMEDIATE;
        }
        if (score < 60) {
            return PRO;
        }
        if (score < 85) {
            return ADVANCED;
        }
        return SUPERIOR;
    }
}
