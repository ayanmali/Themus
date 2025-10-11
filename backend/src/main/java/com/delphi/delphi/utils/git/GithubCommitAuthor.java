package com.delphi.delphi.utils.git;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubCommitAuthor {
    private String date;
    private String name;
    private String email;

    public GithubCommitAuthor() {}

    public GithubCommitAuthor(String date, String name, String email) {
        this.date = date;
        this.name = name;
        this.email = email;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return String.format("""
                date: {date}
                name: {name}
                email: {email}
                """, date, name, email);
    }
}
