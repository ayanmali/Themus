package com.delphi.delphi.utils.git;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubPullRequest {
    private String url;
    private String body;

    public GitHubPullRequest() {}

    public GitHubPullRequest(String url, String body) {
        this.url = url;
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return String.format("""
                url: {url}
                body: {body}
                """, url, body);
    }
}
