package com.analyse.resume.ResumeAnalyser.exception;

import com.analyse.resume.ResumeAnalyser.DTO.ApiErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponseDTO> handleUserAlreadyExists(UserAlreadyExistsException ex, WebRequest webRequest) {
        ApiErrorResponseDTO apiErrorResponseDTO = new ApiErrorResponseDTO(
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getClass().getSimpleName(),
                webRequest.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(apiErrorResponseDTO, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDTO> handleAllExceptions(
            Exception ex, WebRequest request) {
        ApiErrorResponseDTO error = new ApiErrorResponseDTO(
                "INTERNAL_SERVER_ERROR",
                ex.getMessage(),
                ex.getClass().getSimpleName(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
