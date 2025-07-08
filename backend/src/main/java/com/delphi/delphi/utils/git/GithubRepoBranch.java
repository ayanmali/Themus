package com.delphi.delphi.utils.git;

// TODO: use this to format the github api response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubRepoBranch {
    String name;

    public GithubRepoBranch(String name, String sha) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
}
