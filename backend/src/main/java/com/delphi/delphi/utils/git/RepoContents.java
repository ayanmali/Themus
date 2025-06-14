package com.delphi.delphi.utils.git;

public record RepoContents(
    String name,
    String content,
    String path,
    String sha,
    String branch,
    String commitMessage
) {
    
}
