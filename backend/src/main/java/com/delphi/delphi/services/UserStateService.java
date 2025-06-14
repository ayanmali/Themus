package com.delphi.delphi.services;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

@Service
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
/*
 * Maintains the user state (user ID) between LLM tool calls
 * The user's GitHub username and personal access token is needed for GitHub API calls that the LLM uses as tools
 */
public class UserStateService {
    private ThreadLocal<Long> currentUserId = new ThreadLocal<>();

    public UserStateService(ThreadLocal<Long> currentUserId) {
        this.currentUserId = currentUserId;
    }

    public Long getCurrentUserId() {
        return currentUserId.get();
    }

    public void setCurrentUserId(Long currentUserId) {
        this.currentUserId.set(currentUserId);
    }

    @PreDestroy
    public void cleanup() {
        currentUserId.remove();
    }
}
