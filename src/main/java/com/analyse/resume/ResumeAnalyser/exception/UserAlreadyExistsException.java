package com.analyse.resume.ResumeAnalyser.exception;

public class UserAlreadyExistsException extends RuntimeException {
    private final String errorCode;

    public UserAlreadyExistsException(String message) {
        super(message);
        this.errorCode = "USER_ALREADY_EXISTS";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
