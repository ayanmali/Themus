package com.delphi.delphi.utils.git;

// TODO: use this to format the github api response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Branch(
    String name,
    String sha
) {
    
}
