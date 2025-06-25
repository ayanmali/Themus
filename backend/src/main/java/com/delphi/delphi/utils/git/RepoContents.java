package com.delphi.delphi.utils.git;

// TODO: use this to format the github api response
public record RepoContents(
    String name,
    String content,
    String path,
    String sha,
    String branch,
    String commitMessage
) {
    
}
