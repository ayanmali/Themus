package com.delphi.delphi.utils.git;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
// contains the contents of a github repository (files and directories)
public class GithubRepoContents {
    String type;
    String name;
    String path;
    String content;
    String sha;
    List<Entry> entries;

    public GithubRepoContents(String type, String name, String content, String path, String sha, List<Entry> entries) {
        this.type = type;
        this.name = name;
        this.content = content;
        this.path = path;
        this.sha = sha;
        this.entries = entries;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

}
