package com.analyse.resume.ResumeAnalyser.model;

import java.util.List;

public class GitHubRepo {

    private String name;
    private String language;
    private int stargazers_count;
    private String updated_at;
    private List<String> topics;

    public GitHubRepo() {
        super();
    }

    public GitHubRepo(String name, String language, int stargazers_count, String updated_at, List<String> topics) {
        this.name = name;
        this.language = language;
        this.stargazers_count = stargazers_count;
        this.updated_at = updated_at;
        this.topics = topics;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getStargazers_count() {
        return stargazers_count;
    }

    public void setStargazers_count(int stargazers_count) {
        this.stargazers_count = stargazers_count;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    @Override
    public String toString() {
        return "GitHubRepo{" +
                "name='" + name + '\'' +
                ", language='" + language + '\'' +
                ", stargazers_count=" + stargazers_count +
                ", updated_at='" + updated_at + '\'' +
                ", topics=" + topics +
                '}';
    }
}
