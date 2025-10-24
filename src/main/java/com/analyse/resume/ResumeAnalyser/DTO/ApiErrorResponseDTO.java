package com.analyse.resume.ResumeAnalyser.DTO;

import java.time.LocalDateTime;

public class ApiErrorResponseDTO {

    private String errorCode;
    private String errorDescription;
    private String errorClass;
    private String path;
    private LocalDateTime timestamp;

    public ApiErrorResponseDTO(String errorCode, String errorDescription, String errorClass, String path) {
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
        this.errorClass = errorClass;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public String getErrorClass() {
        return errorClass;
    }

    public void setErrorClass(String errorClass) {
        this.errorClass = errorClass;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
