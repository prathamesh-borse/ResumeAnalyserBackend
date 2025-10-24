package com.analyse.resume.ResumeAnalyser.DTO;

import java.util.List;
import java.util.Map;

public class ReportRequestDTO {
    private String username;
    private String roleMatch;
    private List<Map<String, Object>> overallCoverage;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private List<Map<String, Object>> skillFrequencies;
    private List<Map<String, Object>> learningResources;

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRoleMatch() {
        return roleMatch;
    }

    public void setRoleMatch(String roleMatch) {
        this.roleMatch = roleMatch;
    }

    public List<Map<String, Object>> getOverallCoverage() {
        return overallCoverage;
    }

    public void setOverallCoverage(List<Map<String, Object>> overallCoverage) {
        this.overallCoverage = overallCoverage;
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

    public List<Map<String, Object>> getSkillFrequencies() {
        return skillFrequencies;
    }

    public void setSkillFrequencies(List<Map<String, Object>> skillFrequencies) {
        this.skillFrequencies = skillFrequencies;
    }

    public List<Map<String, Object>> getLearningResources() {
        return learningResources;
    }

    public void setLearningResources(List<Map<String, Object>> learningResources) {
        this.learningResources = learningResources;
    }
}
