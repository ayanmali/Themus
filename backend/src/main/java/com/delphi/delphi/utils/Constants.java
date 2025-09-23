package com.delphi.delphi.utils;

import java.util.Map;

public class Constants {
    public static final String CONTRIBUTOR_USERNAME = "themus-assessments[bot]";
    public static final Map<String, String> AUTHOR = Map.of("name", "themus-assessments[bot]", "email", "220768808+themus-assessments[bot]@users.noreply.github.com");

    // Themus account
    public static final String THEMUS_USERNAME = "themus-gh";
    public static final String THEMUS_ORG_NAME = "themus-assessments";

    public static final String DEFAULT_RULES_GUIDELINES = """
            Keep this tab open in your browser while you are working on the assessment. Do not close this tab until after you submit your work and receive confirmation.\n
            Clone the repository to your local machine.\n
            Do not create new branches or work on the main branch. You may only work on the 'assessment' branch that has been created for you.\n
            Regularly commit your changes to your branch to avoid losing your work.\n
            Submit a pull request on GitHub when you are done, explaining the bug fixes or features you implemented.\n
            \n
            You may create new files or directories as necessary to complete the task.\n
            Do not modify the README.md file or any files or code that include a comment indicating that the file/code is not to be edited.\n
            Refer to the README.md file at the root of the repository for further instructions and guidelines.
            """;

    // public static final String GITHUB_BRANCH_NAME = "main";
    // public static final String GITHUB_OWNER = "ayanmali";
    // public static final String GITHUB_ACCESS_TOKEN = "";
}
