package com.delphi.delphi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.delphi.delphi.components.GithubClient;

@SpringBootTest
public class GithubTests {
    private final GithubClient githubClient;

    public GithubTests(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    @Test
    public void testCreateRepo() {
        ResponseEntity<String> response = githubClient.createRepo("accessToken", "test-repo");
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
}
