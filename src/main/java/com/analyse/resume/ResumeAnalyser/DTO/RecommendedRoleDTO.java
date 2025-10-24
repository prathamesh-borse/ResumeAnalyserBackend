package com.analyse.resume.ResumeAnalyser.DTO;

public class RecommendedRoleDTO {

    private String role;
    private double score;

    public RecommendedRoleDTO() {
        super();
    }

    public RecommendedRoleDTO(String role, double score) {
        this.role = role;
        this.score = score;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "RecommendedRoleDTO{" +
                "role='" + role + '\'' +
                ", score=" + score +
                '}';
    }
}
