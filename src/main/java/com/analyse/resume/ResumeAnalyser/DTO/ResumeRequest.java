package com.analyse.resume.ResumeAnalyser.DTO;

public class ResumeRequest {

    private String resumeText;
    private String githubSummary;

    public String getResumeText() {
        return resumeText;
    }

    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }

    public String getGithubSummary() {
        return githubSummary;
    }

    public void setGithubSummary(String githubSummary) {
        this.githubSummary = githubSummary;
    }

    public ResumeRequest() {
        super();
    }

    public ResumeRequest(String resumeText, String githubSummary) {
        this.resumeText = resumeText;
        this.githubSummary = githubSummary;
    }

    @Override
    public String toString() {
        return "ResumeRequest{" +
                "resumeText='" + resumeText + '\'' +
                ", githubSummary='" + githubSummary + '\'' +
                '}';
    }
}
