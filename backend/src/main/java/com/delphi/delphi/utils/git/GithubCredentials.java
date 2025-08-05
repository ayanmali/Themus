package com.delphi.delphi.utils.git;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubCredentials {
    private String login;
    private String type;

    public String getUsername() {
        return login;
    }

    public String getAccountType() {
        return type;
    }

    @Override
    public String toString() {
        return "GithubCredentials{" +
                "login='" + login + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
    
    
}
