package com.delphi.delphi.utils.git;

public class GitCommit {
    String sha;
    GitCommitter committer;
    String message;

    public GitCommit(String sha, GitCommitter committer, String message) {
        this.sha = sha;
        this.committer = committer;
        this.message = message;
    }

    public String getSha() {
        return sha;
    }

    public GitCommitter getCommitter() {
        return committer;
    }

    public String getMessage() {
        return message;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public void setCommitter(GitCommitter committer) {
        this.committer = committer;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
