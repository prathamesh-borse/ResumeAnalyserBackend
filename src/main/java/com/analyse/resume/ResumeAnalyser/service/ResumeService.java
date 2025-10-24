package com.analyse.resume.ResumeAnalyser.service;

import com.analyse.resume.ResumeAnalyser.DTO.AnalysisResponseDTO;
import com.analyse.resume.ResumeAnalyser.DTO.ReportRequestDTO;
import com.analyse.resume.ResumeAnalyser.DTO.ResumeRequest;
import org.apache.tika.exception.TikaException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

public interface ResumeService {

    Map<String, Object> parseResume(MultipartFile multipartFile) throws IOException, TikaException;

    AnalysisResponseDTO analyzeResumeTextAndShowResults(String jobDescription, ResumeRequest resumeRequest);

    ResponseEntity<byte[]> createPDFReport(String reportBasePath, ReportRequestDTO reportRequestDTO);
}
