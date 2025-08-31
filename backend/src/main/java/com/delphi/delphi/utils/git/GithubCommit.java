package com.delphi.delphi.utils.git;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubCommit {
    private String sha;
    private String nodeId;
    private String url;
    private String htmlUrl;
    private GithubCommitAuthor author;
    private GithubCommitAuthor committer;
    private String message;
    private GithubCommitTree tree;
    private GithubCommitParent[] parents;
    private GithubCommitVerification verification;

    public GithubCommit() {}

    public GithubCommit(String sha, String nodeId, String url, String htmlUrl, GithubCommitAuthor author, GithubCommitAuthor committer, String message, GithubCommitTree tree, GithubCommitParent[] parents, GithubCommitVerification verification) {
        this.sha = sha;
        this.nodeId = nodeId;
        this.url = url;
        this.htmlUrl = htmlUrl;
        this.author = author;
        this.committer = committer;
        this.message = message;
        this.tree = tree;
        this.parents = parents;
        this.verification = verification;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public GithubCommitAuthor getAuthor() {
        return author;
    }

    public void setAuthor(GithubCommitAuthor author) {
        this.author = author;
    }

    public GithubCommitAuthor getCommitter() {
        return committer;
    }

    public void setCommitter(GithubCommitAuthor committer) {
        this.committer = committer;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public GithubCommitTree getTree() {
        return tree;
    }

    public void setTree(GithubCommitTree tree) {
        this.tree = tree;
    }

    public GithubCommitParent[] getParents() {
        return parents;
    }

    public void setParents(GithubCommitParent[] parents) {
        this.parents = parents;
    }

    public GithubCommitVerification getVerification() {
        return verification;
    }

    public void setVerification(GithubCommitVerification verification) {
        this.verification = verification;
    }
}
