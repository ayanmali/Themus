package com.delphi.delphi.utils.git;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubCredentials {
    private String username;
    private String accountType;

    public String getUsername() {
        return username;
    }

    public String getAccountType() {
        return accountType;
    }
    
    
}
