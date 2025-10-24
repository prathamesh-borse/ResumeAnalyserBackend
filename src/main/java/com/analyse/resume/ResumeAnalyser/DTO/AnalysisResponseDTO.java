package com.analyse.resume.ResumeAnalyser.DTO;

import java.util.List;
import java.util.Set;

public class AnalysisResponseDTO {
    private List<RoleMatchDTO> roles;
    private List<RecommendedRoleDTO> recommendedRoles;
    private Set<String> uniqueCurrentSkills;
    private Set<String> uniqueMissingSkills;
    private Set<String> matchingSkills;
    private Set<String> extraSkills;

    public AnalysisResponseDTO() {
        super();
    }

    public AnalysisResponseDTO(List<RoleMatchDTO> roles, List<RecommendedRoleDTO> recommendedRoles, Set<String> uniqueCurrentSkills, Set<String> uniqueMissingSkills, Set<String> matchingSkills, Set<String> extraSkills) {
        this.roles = roles;
        this.recommendedRoles = recommendedRoles;
        this.uniqueCurrentSkills = uniqueCurrentSkills;
        this.uniqueMissingSkills = uniqueMissingSkills;
        this.matchingSkills = matchingSkills;
        this.extraSkills = extraSkills;
    }

    public List<RoleMatchDTO> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleMatchDTO> roles) {
        this.roles = roles;
    }

    public List<RecommendedRoleDTO> getRecommendedRoles() {
        return recommendedRoles;
    }

    public void setRecommendedRoles(List<RecommendedRoleDTO> recommendedRoles) {
        this.recommendedRoles = recommendedRoles;
    }

    public Set<String> getUniqueMissingSkills() {
        return uniqueMissingSkills;
    }

    public void setUniqueMissingSkills(Set<String> uniqueMissingSkills) {
        this.uniqueMissingSkills = uniqueMissingSkills;
    }

    public Set<String> getUniqueCurrentSkills() {
        return uniqueCurrentSkills;
    }

    public void setUniqueCurrentSkills(Set<String> uniqueCurrentSkills) {
        this.uniqueCurrentSkills = uniqueCurrentSkills;
    }

    public Set<String> getMatchingSkills() {
        return matchingSkills;
    }

    public void setMatchingSkills(Set<String> matchingSkills) {
        this.matchingSkills = matchingSkills;
    }

    public Set<String> getExtraSkills() {
        return extraSkills;
    }

    public void setExtraSkills(Set<String> extraSkills) {
        this.extraSkills = extraSkills;
    }

    @Override
    public String toString() {
        return "AnalysisResponseDTO{" +
                "roles=" + roles +
                ", recommendedRoles=" + recommendedRoles +
                ", uniqueCurrentSkills=" + uniqueCurrentSkills +
                ", uniqueMissingSkills=" + uniqueMissingSkills +
                ", matchingSkills=" + matchingSkills +
                ", extraSkills=" + extraSkills +
                '}';
    }
}
