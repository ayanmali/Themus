package com.delphi.delphi.utils.git;

public record File(
    String name,
    String content,
    String path,
    String sha,
    String branch,
    String commitMessage
) {
    
}
