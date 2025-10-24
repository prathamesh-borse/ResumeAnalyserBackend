package com.analyse.resume.ResumeAnalyser.model;

import java.util.List;

public class Role {
    private String role;
    private List<String> skills;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }
}
