package com.delphi.delphi.utils.git;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubFileResponse {
    private GithubFile content;
    private GithubCommit commit;

    public GithubFileResponse() {}

    public GithubFileResponse(GithubFile content, GithubCommit commit) {
        this.content = content;
        this.commit = commit;
    }

    public GithubFile getContent() {
        return content;
    }

    public void setContent(GithubFile content) {
        this.content = content;
    }

    public GithubCommit getCommit() {
        return commit;
    }

    public void setCommit(GithubCommit commit) {
        this.commit = commit;
    }

    @Override
    public String toString() {
        return String.format("""
                content: {content}
                commit: {commit}
                """, content, commit);
    }
}
