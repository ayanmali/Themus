package com.delphi.delphi.components.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import com.delphi.delphi.utils.git.GithubRepoContents;

public class RepoAnalyzerTools implements Tools {
    private final Logger log = LoggerFactory.getLogger(RepoAnalyzerTools.class);
    
    @Tool(name = "addNote", description = "Adds a note to your notes file. Use this tool to record any information that you think may be helpful to you when analyzing the repository or anything that would be useful to incorporate into an assessment.")
    public String addNote(
        @ToolParam(required = true, description = "The note to add to the repository") String note) {
        throw new RuntimeException("Placeholder method being called for addNote");
    }

    @Tool(name = "getNotes", description = "Retrieves the notes from your notes file.")
    public String getNotes() {
        throw new RuntimeException("Placeholder method being called for getNotes");
    }

    @Tool(name = "getRepositoryContents", description = "Gets the file and directory contents of the default branch of the Git repository using the GitHub API.")
    public GithubRepoContents getRepoContents(
        @ToolParam(required = true, description = "The path of the file to get the contents of") String filePath//,
        //@ToolParam(required = false, description = "The branch to get the contents of. If not provided, the default branch will be used.") String branch) {
        ) {
        throw new RuntimeException("Placeholder method being called for getRepoContents");
    }

    @Tool(name = "returnRepositoryAnalysis", description = "Returns the analysis results of the repository in Markdown format. This tool should be called when the repository analysis is complete.")
    public String returnRepositoryAnalysis(
        @ToolParam(required = true, description = "The analysis results of the repository in Markdown format") String analysisResults) {
        log.info("Repository analysis completed with results: {}", analysisResults);
        return analysisResults;
    }
}
