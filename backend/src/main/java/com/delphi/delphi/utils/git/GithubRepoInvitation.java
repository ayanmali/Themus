package com.delphi.delphi.utils.git;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubRepoInvitation {
    private Long id;
    private GithubUser invitee;
    private GithubUser inviter;
    private String permissions;
    private String createdAt;
    private String url;
    private String htmlUrl;
    private String nodeId;

    public GithubRepoInvitation() {}

    public GithubRepoInvitation(Long id, GithubUser invitee, GithubUser inviter, String permissions, String createdAt, String url, String htmlUrl, String nodeId) {
        this.id = id;
        this.invitee = invitee;
        this.inviter = inviter;
        this.permissions = permissions;
        this.createdAt = createdAt;
        this.url = url;
        this.htmlUrl = htmlUrl;
        this.nodeId = nodeId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GithubUser getInvitee() {
        return invitee;
    }

    public void setInvitee(GithubUser invitee) {
        this.invitee = invitee;
    }

    public GithubUser getInviter() {
        return inviter;
    }

    public void setInviter(GithubUser inviter) {
        this.inviter = inviter;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
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

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String toString() {
        return String.format("""
                id: {id}
                invitee: {invitee}
                inviter: {inviter}
                permissions: {permissions}
                createdAt: {createdAt}
                url: {url}
                htmlUrl: {htmlUrl}
                nodeId: {nodeId}
                """, id, invitee, inviter, permissions, createdAt, url, htmlUrl, nodeId);
    }
}