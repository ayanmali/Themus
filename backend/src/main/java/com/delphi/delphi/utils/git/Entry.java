package com.delphi.delphi.utils.git;

// represents a file or directory in a git repository
public interface Entry {
    String getType();
    String getName();
    String getPath();
    }
