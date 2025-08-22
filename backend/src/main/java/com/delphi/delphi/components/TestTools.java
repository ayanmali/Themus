package com.delphi.delphi.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class TestTools {
    private static final Logger log = LoggerFactory.getLogger(TestTools.class);
    
    @Tool(description = "Gets the weather for a given city.", name = "getWeather")
    public int getWeather(@ToolParam(required = true, description = "The city to get the weather for. It must be a valid city name and must be in the format of 'City, State'. For example, 'New York, NY'.") String city) {
        log.info("--------------------------------");
        log.info("GETTING WEATHER - TEST TOOLS:");
        log.info("City: {}", city);
        log.info("--------------------------------");
        return 69;
    }
}
