package com.delphi.delphi.utils.git;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubBranchDetails {
    String name;
    GithubCommit commit;

    public GithubBranchDetails(String name, GithubCommit commit) {
        this.name = name;
        this.commit = commit;
    }

    public String getName() {
        return name;
    }

    public GithubCommit getCommit() {
        return commit;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCommit(GithubCommit commit) {
        this.commit = commit;
    }
    
}
