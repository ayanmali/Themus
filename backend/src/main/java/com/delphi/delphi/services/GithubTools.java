package com.delphi.delphi.services;

import org.springframework.stereotype.Service;

@Service
/*
 * Contains tool definitions for the LLM to make GitHub API calls
 * Uses the methods defined in the GithubClient class
 */
public class GithubTools {
    private final UserStateService userStateService;

    public GithubTools(UserStateService userStateService) {
        this.userStateService = userStateService;
    }
}
