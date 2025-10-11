package com.delphi.delphi.utils.git;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubCommitTree {
    private String url;
    private String sha;

    public GithubCommitTree() {}

    public GithubCommitTree(String url, String sha) {
        this.url = url;
        this.sha = sha;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
                url: {url}
                sha: {sha}
                """, url, sha);
    }
}
