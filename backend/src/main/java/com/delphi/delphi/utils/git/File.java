package com.delphi.delphi.utils.git;

// TODO: use this to format the github api response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record File(
    String name,
    String content,
    String path,
    String sha,
    String branch,
    String commitMessage
) {
    
}
