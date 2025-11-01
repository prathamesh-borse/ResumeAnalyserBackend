package com.analyse.resume.ResumeAnalyser.controller;

import com.analyse.resume.ResumeAnalyser.DTO.AnalysisResponseDTO;
import com.analyse.resume.ResumeAnalyser.DTO.ReportRequestDTO;
import com.analyse.resume.ResumeAnalyser.DTO.ResumeRequest;
import com.analyse.resume.ResumeAnalyser.model.GitHubRepo;
import com.analyse.resume.ResumeAnalyser.service.Impl.ResumeServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.tika.exception.TikaException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeAnalyserController {

    private final ResumeServiceImpl resumeService;

    @Value("${reports.base-path}")
    private String reportsBasePath;

    private GitHubRepo[] githubRepos;

    @PostMapping("/health")
    public String getHealth() {
        return "Resume Analyser Service is up and running!";
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> handleFileUpload(@RequestParam("file") MultipartFile multipartFile) throws IOException, TikaException {
        Map<String, Object> parsedText = resumeService.parseResume(multipartFile);
        return ResponseEntity.status(200).body(Map.of("resumeText", parsedText));
    }


    @PostMapping("/analyzeProfile")
    public ResponseEntity<AnalysisResponseDTO> RoleMatchByUsingResumeText(@RequestParam String jobDescription, @RequestParam("file") MultipartFile multipartFile) throws TikaException, IOException {
        // 1. Parse Resume
        Map<String, Object> parsedText = resumeService.parseResume(multipartFile);
        String resumeText = parsedText.get("parsedText").toString();

        // 2. Create ResumeRequest
        ResumeRequest resumeRequest = new ResumeRequest();
        resumeRequest.setResumeText(resumeText);

        AnalysisResponseDTO analysisResponseDTO = resumeService.analyzeResumeTextAndShowResults(jobDescription, resumeRequest);
        return ResponseEntity.status(200).body(analysisResponseDTO);
    }

    @PostMapping("/downloadProfile")
    public ResponseEntity<byte[]> downloadReport(@RequestBody ReportRequestDTO reportRequestDTO) {
        return resumeService.createPDFReport(reportsBasePath, reportRequestDTO);
    }
}
