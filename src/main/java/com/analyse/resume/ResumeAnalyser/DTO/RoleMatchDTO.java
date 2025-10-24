package com.analyse.resume.ResumeAnalyser.DTO;

import java.util.List;

public class RoleMatchDTO {
    private String role;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private double coverage;

    public RoleMatchDTO() {
        super();
    }

    public RoleMatchDTO(String role, List<String> matchedSkills, List<String> missingSkills, double coverage) {
        this.role = role;
        this.matchedSkills = matchedSkills;
        this.missingSkills = missingSkills;
        this.coverage = coverage;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public double getCoverage() {
        return coverage;
    }

    public void setCoverage(double coverage) {
        this.coverage = coverage;
    }

    @Override
    public String toString() {
        return "RoleMatchDTO{" +
                "role='" + role + '\'' +
                ", matchedSkills=" + matchedSkills +
                ", missingSkills=" + missingSkills +
                ", coverage=" + coverage +
                '}';
    }
}
