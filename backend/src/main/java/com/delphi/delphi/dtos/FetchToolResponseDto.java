package com.delphi.delphi.dtos;

import com.delphi.delphi.entities.OpenAiToolResponse;

public class FetchToolResponseDto {
    private String id;
    private String name;
    private String responseData;

    public FetchToolResponseDto(String id, String name, String responseData) {
        this.id = id;
        this.name = name;
        this.responseData = responseData;
    }

    public FetchToolResponseDto(OpenAiToolResponse toolResponse) {
        this.id = toolResponse.getId();
        this.name = toolResponse.getName();
        this.responseData = toolResponse.getResponseData();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }
    
}
