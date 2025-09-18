package com.delphi.delphi.utils.exceptions;

public class CandidateNotFoundException extends IllegalArgumentException {

    public CandidateNotFoundException(String message) {
        super(message);
    }

    public CandidateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
