// package com.delphi.delphi;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;

// import com.delphi.delphi.components.GithubClient;
// import com.delphi.delphi.utils.git.GithubRepoContents;

// @SpringBootTest
// public class GithubTests {
//     private final GithubClient githubClient;
//     private final String accessToken;

//     public GithubTests(GithubClient githubClient, @Value("${github.access.token}") String accessToken) {
//         this.accessToken = accessToken;
//         this.githubClient = githubClient;
//     }

//     @Test
//     public void testCreateRepo() {
//         System.out.println("Github personal access token: " + accessToken);
//         ResponseEntity<GithubRepoContents> response = githubClient.createRepo(accessToken, "test-repo");
//         assertEquals(HttpStatus.CREATED, response.getStatusCode());
//     }

// }
