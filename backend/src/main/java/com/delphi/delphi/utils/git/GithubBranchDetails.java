package com.delphi.delphi.utils.git;

public class GithubBranchDetails {
    String name;
    GitCommit commit;

    public GithubBranchDetails(String name, GitCommit commit) {
        this.name = name;
        this.commit = commit;
    }

    public String getName() {
        return name;
    }

    public GitCommit getCommit() {
        return commit;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCommit(GitCommit commit) {
        this.commit = commit;
    }
    
}
