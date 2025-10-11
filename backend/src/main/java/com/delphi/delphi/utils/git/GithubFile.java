package com.delphi.delphi.utils.git;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubFile implements Entry {
    String type;
    String name;
    String content;
    String path;
    String sha;

    public GithubFile() {}

    public GithubFile(String type, String name, String content, String path, String sha) {
        this.type = type;
        this.name = name;
        this.content = content;
        this.path = path;
        this.sha = sha;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    @Override
    public String toString() {
        return String.format("""
                type: {type}
                name: {name}
                path: {path}
                sha: {sha}
                """, type, name, path, sha);
    }
    
}
