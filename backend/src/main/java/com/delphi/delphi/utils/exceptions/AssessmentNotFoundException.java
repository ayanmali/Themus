package com.delphi.delphi.utils.exceptions;

public class AssessmentNotFoundException extends IllegalArgumentException {
    
    public AssessmentNotFoundException(String message) {
        super(message);
    }

    public AssessmentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AssessmentNotFoundException(Throwable cause) {
        super(cause);
    }

}
