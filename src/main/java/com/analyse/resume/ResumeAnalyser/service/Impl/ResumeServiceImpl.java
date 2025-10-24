package com.analyse.resume.ResumeAnalyser.service.Impl;

import com.analyse.resume.ResumeAnalyser.DTO.*;
import com.analyse.resume.ResumeAnalyser.config.FooterPageEvent;
import com.analyse.resume.ResumeAnalyser.model.GitHubRepo;
import com.analyse.resume.ResumeAnalyser.model.Role;
import com.analyse.resume.ResumeAnalyser.service.ResumeService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.Font;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ResumeServiceImpl implements ResumeService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${huggingface.api.url}")
    private String huggingFaceAPIURL;

    @Value("${huggingface.api.token}")
    private String huggingFaceAPIToken;

    private GitHubRepo[] gitHubRepos;
    private AnalysisResponseDTO responseDTO;

    @Override
    public Map<String, Object> parseResume(MultipartFile multipartFile) throws IOException, TikaException {
        Map<String, Object> response = new HashMap<>();
        try {
            if (multipartFile.isEmpty() || !multipartFile.getOriginalFilename().endsWith(".pdf")) {
                return Map.of("error", "Invalid file type");
            }

            Tika tika = new Tika();
            String parsedText = tika.parseToString(multipartFile.getInputStream());

            if (parsedText.split("\n").length > 200 || parsedText.length() < 500) {
                StringBuilder fullText = new StringBuilder();

                try (PDDocument document = PDDocument.load(multipartFile.getInputStream())) {
                    int totalPages = document.getNumberOfPages();

                    for (int page = 0; page < totalPages; page++) {
                        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                        PDPage pdPage = document.getPage(page);

                        int pageWidth = (int) pdPage.getMediaBox().getWidth();
                        int pageHeight = (int) pdPage.getMediaBox().getHeight();

                        // Try configurable boundary (e.g., 55% left, 45% right for asymmetrical columns)
                        int leftColumnWidth = (int) (pageWidth * 0.55);
                        int rightColumnWidth = pageWidth - leftColumnWidth;

                        Rectangle left = new Rectangle(0, 0, leftColumnWidth, pageHeight);
                        Rectangle right = new Rectangle(leftColumnWidth, 0, rightColumnWidth, pageHeight);

                        stripper.addRegion("left", left);
                        stripper.addRegion("right", right);

                        stripper.extractRegions(pdPage);

                        String leftText = stripper.getTextForRegion("left");
                        String rightText = stripper.getTextForRegion("right");

                        // Optional: try detecting section headings spanning both columns
                        PDFTextStripper defaultStripper = new PDFTextStripper();
                        defaultStripper.setStartPage(page + 1);
                        defaultStripper.setEndPage(page + 1);
                        String pageFullText = defaultStripper.getText(document);

                        fullText.append("Headings:\n").append(pageFullText).append("\n");
                        fullText.append("Left:\n").append(leftText).append("\n");
                        fullText.append("Right:\n").append(rightText).append("\n");
                    }
                }
                parsedText = fullText.toString();
            }

            response.put("parsedText", parsedText);
            return response;

        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }


//    @Override
//    public List<GitHubRepo> showGitHubDataByUsername(String githubUsername, ResumeRequest resumeRequest) {
//        String url = "https://api.github.com/users/" + githubUsername + "/repos";
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Accept", "application/vnd.github+json");
//        headers.set("Access-Control-Allow-Origin", "http://localhost:3000");
//        headers.set("Access-Control-Allow-Credentials", "true");
//
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//        ResponseEntity<GitHubRepo[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, GitHubRepo[].class);
//        gitHubRepos = response.getBody();
//        AnalysisResponseDTO analysisResponseDTO = roleMatchByUsingResumeText(resumeRequest);
//        this.responseDTO = analysisResponseDTO;
//        return Arrays.asList(response.getBody());
//    }

    // parsedText from resume = this two I have to pass for AI Role Match Job + Skills Gap Engine
    // Skills from gitHub =

    private static List<Map.Entry<String, Double>> extractSkillsByFrequencyFromResume(String resumeText) {
        List<String> skillsList = ResumeServiceImpl.extractSkillsFromResume(resumeText);

        // Counting frequencies of each skill
        Map<String, Long> skillFrequency = skillsList.stream().map(ResumeServiceImpl::normalizeSkill).collect(Collectors.groupingBy(skillsGrouping -> skillsGrouping, Collectors.counting()));

        // Total number of occurrences for percentage calculation
        long totalOccurrences = skillFrequency.values().stream().mapToLong(Long::longValue).sum();

        // Convert to list of skill + percentage, sorted by frequency
        List<Map.Entry<String, Double>> topSkills = skillFrequency.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(),
                        (entry.getValue() * 100.0) / totalOccurrences))
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue())) // sort desc
                .collect(Collectors.toList());

        return topSkills;
    }

    public AnalysisResponseDTO analyzeResumeTextAndShowResults(String jobDescription, ResumeRequest resumeRequest) {
        List<Role> expectedRoles = loadExpectedRoles();
        List<String> candidateRoles = expectedRoles.stream()
                .map(Role::getRole)
                .collect(Collectors.toList());

        // Validate resume text
        if (resumeRequest.getResumeText() == null || resumeRequest.getResumeText().isEmpty()) {
            throw new IllegalArgumentException("Resume text cannot be null or empty");
        }

        // Validate candidate roles
        if (candidateRoles.isEmpty()) {
            throw new IllegalArgumentException("Candidate roles cannot be empty");
        }

        RestTemplate restTemplate = new RestTemplate();

        // Prepare Hugging Face API request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Accept", "application/json");
        headers.setBearerAuth(huggingFaceAPIToken);

        // Request Body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("inputs", resumeRequest.getResumeText());
        requestBody.put("parameters", Map.of("candidate_labels", candidateRoles));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // Call Hugging Face API
            ResponseEntity<Map> response = restTemplate.exchange(
                    huggingFaceAPIURL, HttpMethod.POST, entity, Map.class);

            // Process Hugging Face API response
            List<Map<String, Object>> pickTopLabelsWithHighestScores = pickTopLabelsWithHighestScores(response);

            // Get Recommended Roles
            // List<RecommendedRoleDTO> recommendedRoleDTOS = buildRecommendedRoles(pickTopLabelsWithHighestScores);

            // Combine Skills from extractSkills and extractSkillsByFrequency
            List<String> resumeSkills = extractSkillsFromResume(resumeRequest.getResumeText());
            List<String> jdSkills = extractSkillsFromJD(jobDescription);

            Set<String> matchingSkills = new HashSet<>(resumeSkills);
            matchingSkills.retainAll(jdSkills);

            Set<String> missingSkills = new HashSet<>(jdSkills);
            missingSkills.removeAll(resumeSkills);

            Set<String> extraSkills = new HashSet<>(resumeSkills);
            extraSkills.removeAll(jdSkills);

            List<Map.Entry<String, Double>> extractSkillsByFrequency = extractSkillsByFrequencyFromResume(resumeRequest.getResumeText());

            List<String> extractSkillsFromResume = extractSkillsFromResume((String) response.getBody().get("sequence"));

            // Combine two list into one list using flatMap
            List<Object> combinedSkills = Stream.of(resumeSkills, extractSkillsByFrequency, extractSkillsFromResume).flatMap(Collection::stream).collect(Collectors.toList());

            // loadExpectedRoles from Json File
            List<Role> roles = loadExpectedRoles();

            List<RoleMatchDTO> rolesMatches = buildRoleMatches(roles, combinedSkills).stream()
                    .filter(r -> r.getCoverage() > 0)
                    .collect(Collectors.toList());

            // Set Recommended Roles
            List<RecommendedRoleDTO> recommendedRoleDTOS = rolesMatches.stream()
                    .map(role -> new RecommendedRoleDTO(role.getRole(), role.getCoverage()))
                    .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                    .limit(3)
                    .collect(Collectors.toList());

            // Return AnalysisResponse DTO to pass to another Method
            AnalysisResponseDTO analysisResponseDTO = new AnalysisResponseDTO();
            analysisResponseDTO.setRoles(rolesMatches);
            analysisResponseDTO.setRecommendedRoles(recommendedRoleDTOS);
            analysisResponseDTO.setUniqueCurrentSkills(new HashSet<>(resumeSkills));
            analysisResponseDTO.setUniqueMissingSkills(missingSkills);
            analysisResponseDTO.setExtraSkills(extraSkills);
            returnAnalysisResponseDTO(analysisResponseDTO);
            System.out.println("AnalysisResponseDTO: " + analysisResponseDTO);
            return analysisResponseDTO;
        } catch (Exception e) {
            throw new RuntimeException("Error calling Hugging Face API: " + e.getMessage(), e);
        }
    }

    private List<String> extractSkillsFromJD(String jobDescription) {
        if (jobDescription == null || jobDescription.isBlank()) return Collections.emptyList();

        // Preprocess text
        String cleaned = preprocessResumeText(jobDescription);

        // Try dictionary-based extraction for now
        List<String> skills = extractSkillsByDictionaryScan(cleaned);

        return skills;
    }

    @Override
    public ResponseEntity<byte[]> createPDFReport(String reportBasePath, ReportRequestDTO reportRequestDTO) {
        // Implementation for PDF report generation goes here
        try {
            System.out.println("📩 Received payload:");
            System.out.println("Username: " + reportRequestDTO.getUsername());
            System.out.println("Role Match: " + reportRequestDTO.getRoleMatch());
            System.out.println("Overall Coverage: " + reportRequestDTO.getOverallCoverage());
            System.out.println("Matched Skills: " + reportRequestDTO.getMatchedSkills());
            System.out.println("Missing Skills: " + reportRequestDTO.getMissingSkills());
            System.out.println("Skill Frequencies: " + reportRequestDTO.getSkillFrequencies());
            System.out.println("Learning Resources: " + reportRequestDTO.getLearningResources());

            // --- PDF CREATION START ---
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);

            PdfWriter pdfWriter = PdfWriter.getInstance(document, out);
            pdfWriter.setPageEvent(new FooterPageEvent(reportRequestDTO.getUsername()));

            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
            Paragraph title = new Paragraph("SkillMatch AI | Resume Analysis Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\nGenerated on: " + new Date()));

            document.add(new Paragraph("\nUser: " + reportRequestDTO.getUsername()));
            document.add(new Paragraph("Role Match: " + reportRequestDTO.getRoleMatch()));
            document.add(new Paragraph("Top 3 Skills: " + reportRequestDTO.getMatchedSkills().stream().limit(3).collect(Collectors.joining(", "))));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Matched Skills:"));
            document.add(new Paragraph(String.join(", ", reportRequestDTO.getMatchedSkills())));
            document.add(new Paragraph("\nMissing Skills:"));
            document.add(new Paragraph(String.join(", ", reportRequestDTO.getMissingSkills())));

            // ------------------- Skill Frequency Table -------------------
            List<Map<String, Object>> skillFrequencies = reportRequestDTO.getSkillFrequencies();
            if (skillFrequencies != null && !skillFrequencies.isEmpty()) {
                document.add(new Paragraph("\nSkill Frequency Table", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK)));
                document.add(new Paragraph(" "));

                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setSpacingBefore(5f);
                table.setSpacingAfter(5f);

                PdfPCell cell1 = new PdfPCell(new Phrase("Skill"));
                cell1.setBackgroundColor(BaseColor.GRAY);
                cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell1);

                PdfPCell cell2 = new PdfPCell(new Phrase("Frequency"));
                cell2.setBackgroundColor(BaseColor.GRAY);
                cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell2);

                for (Map<String, Object> entry : skillFrequencies) {
                    table.addCell(String.valueOf(entry.get("skill")));
                    table.addCell(String.valueOf(entry.get("frequency")));
                }

                document.add(table);
            }

            // ------------------- Role Coverage Table -------------------
            List<Map<String, Object>> skillsCoverage = reportRequestDTO.getOverallCoverage();
            if (skillsCoverage != null && !skillsCoverage.isEmpty()) {
                document.add(new Paragraph("\nRole Coverage Table", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK)));
                document.add(new Paragraph(" "));

                PdfPTable newTable = new PdfPTable(2);
                newTable.setWidthPercentage(100);
                newTable.setSpacingBefore(5f);
                newTable.setSpacingAfter(5f);

                PdfPCell r1 = new PdfPCell(new Phrase("Role"));
                r1.setBackgroundColor(BaseColor.GRAY);
                r1.setHorizontalAlignment(Element.ALIGN_CENTER);
                newTable.addCell(r1);

                PdfPCell r2 = new PdfPCell(new Phrase("Coverage (%)"));
                r2.setBackgroundColor(BaseColor.GRAY);
                r2.setHorizontalAlignment(Element.ALIGN_CENTER);
                newTable.addCell(r2);

                for (Map<String, Object> entry : skillsCoverage) {
                    newTable.addCell(String.valueOf(entry.get("role")));
                    newTable.addCell(String.valueOf(entry.get("score")) + "%");
                }

                document.add(newTable);
            }

            // ------------------- Learning Resources Table -------------------
            List<Map<String, Object>> learningResources = reportRequestDTO.getLearningResources();
            System.out.println("Learning Resources: " + learningResources);
            if (learningResources != null && !learningResources.isEmpty()) {
                document.add(new Paragraph("\nLearning Resources Table", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK)));
                document.add(new Paragraph(" "));

                PdfPTable resourcesTable = new PdfPTable(3);
                resourcesTable.setWidthPercentage(100);
                resourcesTable.setSpacingBefore(5f);
                resourcesTable.setSpacingAfter(5f);

                // Header cells
                PdfPCell nameHeader = new PdfPCell(new Phrase("Name", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE)));
                nameHeader.setBackgroundColor(BaseColor.GRAY);
                nameHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
                resourcesTable.addCell(nameHeader);

                PdfPCell descHeader = new PdfPCell(new Phrase("Description", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE)));
                descHeader.setBackgroundColor(BaseColor.GRAY);
                descHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
                resourcesTable.addCell(descHeader);

                PdfPCell linkHeader = new PdfPCell(new Phrase("Link", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE)));
                linkHeader.setBackgroundColor(BaseColor.GRAY);
                linkHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
                resourcesTable.addCell(linkHeader);

                // Data rows
                for (Map<String, Object> entry : learningResources) {
                    // Name
                    PdfPCell nameCell = new PdfPCell(new Phrase(String.valueOf(entry.get("name"))));
                    nameCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    resourcesTable.addCell(nameCell);

                    // Description
                    PdfPCell descCell = new PdfPCell(new Phrase(String.valueOf(entry.get("desc"))));
                    descCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    resourcesTable.addCell(descCell);

                    // Clickable Link
                    String link = String.valueOf(entry.get("link"));
                    Chunk linkChunk = new Chunk("View Resource", FontFactory.getFont(FontFactory.HELVETICA, 10, Font.UNDERLINE, BaseColor.BLUE));
                    linkChunk.setAnchor(link); // Makes it clickable
                    PdfPCell linkCell = new PdfPCell(new Phrase(linkChunk));
                    linkCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    resourcesTable.addCell(linkCell);
                }

                document.add(resourcesTable);
            }

            // Summary Section
            document.add(new Paragraph("\nVisual Summary Of Resume", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK)));
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setSpacingBefore(5f);
            summaryTable.setSpacingAfter(5f);
            summaryTable.setWidthPercentage(80);
            summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            summaryTable.addCell("Role Match");
            summaryTable.addCell(reportRequestDTO.getRoleMatch());
            summaryTable.addCell("Overall Coverage");
            summaryTable.addCell(reportRequestDTO.getOverallCoverage()
                    .stream()
                    .mapToDouble(entry -> Double.parseDouble(String.valueOf(entry.get("score"))))
                    .sum() + "%");
            summaryTable.addCell("Matched Skills Count");
            summaryTable.addCell(String.valueOf(reportRequestDTO.getMatchedSkills().size()));
            summaryTable.addCell("Missing Skills Count");
            summaryTable.addCell(String.valueOf(reportRequestDTO.getMissingSkills().size()));
            document.add(summaryTable);

            document.close();
            byte[] pdfBytes = out.toByteArray();
            // --- PDF CREATION END ---

            // Save report (optional)
            Path reportsDir = Paths.get(reportBasePath);
            Files.createDirectories(reportsDir);
            String safeUsername = (reportRequestDTO.getUsername() == null)
                    ? "user"
                    : reportRequestDTO.getUsername().replaceAll("[^a-zA-Z0-9_-]", "_");
            String fileName = String.format("Resume_Report_%s_%d.pdf", safeUsername, System.currentTimeMillis());
            Path filePath = reportsDir.resolve(fileName);
            Files.write(filePath, pdfBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating PDF: " + e.getMessage()).getBytes());
        }
    }


    // Build Recommended Roles
    private static List<RecommendedRoleDTO> buildRecommendedRoles(List<Map<String, Object>> pickTopLabelsWithHighestScores) {
        return pickTopLabelsWithHighestScores.stream().limit(3).map(entry -> {
            RecommendedRoleDTO recommendedRoleDTO = new RecommendedRoleDTO();
            recommendedRoleDTO.setRole(entry.get("role").toString());
            recommendedRoleDTO.setScore(Double.parseDouble(entry.get("score").toString()));
            return recommendedRoleDTO;
        }).collect(Collectors.toList());
    }


    private static List<Map<String, Object>> pickTopLabelsWithHighestScores(ResponseEntity<Map> response) {

        Map<String, Object> responseBody = response.getBody();

        if (responseBody.isEmpty()) {
            throw new RuntimeException("Response Body is Empty");
        }

        // Pair both labels with scores list
        List<Object> labels = (List<Object>) responseBody.get("labels");
        List<Object> scores = (List<Object>) responseBody.get("scores");

        List<Map<String, Object>> pairedLabelsAndScoresList = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("role", labels.get(i));
            entry.put("score", scores.get(i));
            pairedLabelsAndScoresList.add(entry);
        }

        // Sort by score (descending) = highest -> lowest
        // When we don;t sort the score then the scores will be unsorted, raw format
        pairedLabelsAndScoresList.sort((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")));

        // Pick top N
        return pairedLabelsAndScoresList.stream().limit(3).collect(Collectors.toList());
    }

    private static List<RoleMatchDTO> buildRoleMatches(List<Role> roles, List<Object> combinedSkills) {
        List<RoleMatchDTO> roleMatches = new ArrayList<>();

        for (Role role : roles) {
            // normalize role skills
            Set<String> roleSkills = role.getSkills().stream()
                    .map(ResumeServiceImpl::normalizeSkill)
                    .collect(Collectors.toSet());

            // normalize user skills from combinedSkills
            Set<String> userSkills = combinedSkills.stream()
                    .map((s) -> normalizeSkill(s.toString()))
                    .collect(Collectors.toSet());

            // matchedSkills from both roleSkills and userSkills
            Set<String> matchedSkills = new HashSet<>(userSkills);
            matchedSkills.retainAll(roleSkills);

            // missing skills
            Set<String> missingSkills = new HashSet<>(roleSkills);
            missingSkills.removeAll(userSkills);

            // skills coverage by combining the matchedSkills and roleSkills
            double skillsCoverage = (double) matchedSkills.size() / roleSkills.size() * 100;

            RoleMatchDTO roleMatchDTO = new RoleMatchDTO();
            roleMatchDTO.setRole(role.getRole());
            roleMatchDTO.setMatchedSkills(new ArrayList<>(matchedSkills));
            roleMatchDTO.setMissingSkills(new ArrayList<>(missingSkills));
            roleMatchDTO.setCoverage(skillsCoverage);

            roleMatches.add(roleMatchDTO);
        }
        return roleMatches;
    }

    public AnalysisResponseDTO returnAnalysisResponseDTO(AnalysisResponseDTO analysisResponseDTO) {
        return analysisResponseDTO;
    }

    private static final Map<String, String> SKILL_NORMALIZATION_MAP = Map.ofEntries(
            Map.entry("React", "react js"),
            Map.entry("reactjs", "react js"),
            Map.entry("React js", "react js"),
            Map.entry("react js", "react js"),
            Map.entry("Spring", "spring"),
            Map.entry("Microservices", "microservices"),
            Map.entry("SpringBoot", "spring boot"),
            Map.entry("Spring Boot", "spring boot"),
            Map.entry("springboot", "spring boot"),
            Map.entry("Maven", "maven"),
            Map.entry("spring mvc", "spring mvc"),
            Map.entry("Rest Api", "rest api"),
            Map.entry("restapi", "rest api"),
            Map.entry("rest-api", "rest api"),
            Map.entry("Node js", "node js"),
            Map.entry("nodejs", "node js"),
            Map.entry("node.js", "node js"),
            Map.entry("Javascript", "Javascript"),
            Map.entry("c++", "cpp"),
            Map.entry("Html", "html"),
            Map.entry("HTML", "html"),
            Map.entry("html5", "html"),
            Map.entry("CSS", "css"),
            Map.entry("css3", "css"),
            Map.entry("Js", "js"),
            Map.entry("Redux", "redux"),
            Map.entry("javascript", "js"),
            Map.entry("PostgreSQL", "postgresql"),
            Map.entry("postgres", "postgresql"),
            Map.entry("Docker", "docker"),
            Map.entry("Python", "python"),
            Map.entry("Django", "Django")
    );

    // If you keep existing normalizeSkill elsewhere, ensure it is consistent with normalizeSkillToken.
    // Example normalizeSkill wrapper that reuses normalizeSkillToken:
    private static String normalizeSkill(String s) {
        return normalizeSkillToken(s);
    }

    private static List<Role> loadExpectedRoles() {
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream inputStream = ResumeServiceImpl.class.getResourceAsStream("/ExpectedRole.json")) {
            if (inputStream == null) {
                throw new RuntimeException("ExpectedRole.json file not found in resources");
            }
            return objectMapper.readValue(inputStream, new TypeReference<List<Role>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Error reading ExpectedRole.json file: " + e);
        }
    }

    private static final Set<String> KNOWN_SKILLS = Set.of(
            // core tech — extend this list as you like
            "java", "spring boot", "spring", "springboot", "spring mvc", "rest api", "rest", "restapi",
            "mysql", "postgresql", "postgres", "sql", "nosql", "mongodb", "hibernate", "maven", "gradle",
            "docker", "kubernetes", "aws", "azure", "gcp", "jenkins", "terraform", "ansible", "git",
            "microservices", "node js", "node", "nodejs", "node.js", "javascript", "js", "typescript",
            "react", "react js", "reactjs", "redux", "html", "css", "html5", "css3",
            "python", "django", "flask", "pandas", "numpy", "tensorflow", "pytorch", "scikit-learn",
            "machine learning", "deep learning", "data structures", "algorithms",
            "android", "kotlin", "swift", "ios", "flutter", "react native", "firebase",
            "ci/cd", "devops", "linux", "bash scripting", "monitoring",
            "ui/ux", "bootstrap", "tailwind css", "webpack"
    );

    private static String preprocessResumeText(String text) {
        if (text == null) return "";
        // 1) Remove the "Headings:" tokens or any debug markers added earlier
        text = text.replaceAll("(?i)headings?:\\s*", " ");

        // 2) Fix hyphenation at line ends like "user-\n experience" -> "user experience"
        text = text.replaceAll("-\\s*\\r?\\n\\s*", "");

        // 3) Join broken lines that were split in the middle of sentences:
        //    If a line break is followed by a lowercase letter, it's likely a wrapped line -> replace with space
        text = text.replaceAll("\\r?\\n\\s*([a-z0-9])", " $1");

        // 4) Normalize all other line breaks to newline marker, so we can split sections reliably
        text = text.replaceAll("\\r?\\n\\s*", "\n");

        // 5) Multiple spaces -> single
        text = text.replaceAll("\\s{2,}", " ").trim();

        return text;
    }

    private static String normalizeSkillToken(String token) {
        if (token == null) return "";
        String s = token.trim().toLowerCase();
        // remove leading bullets or decorative characters
        s = s.replaceAll("^[•\\-\\*\\u2022\\u2023\\u25E6\\s]+", "");
        // unify punctuation to spaces for multiword skills ("REST-API" -> "rest api")
        s = s.replaceAll("[\\-/]", " ");
        // collapse multiple spaces
        s = s.replaceAll("\\s{2,}", " ").trim();

        // apply mapping
        String mapped = SKILL_NORMALIZATION_MAP.getOrDefault(s, s);
        // remove stray punctuation except + (for c++)
        mapped = mapped.replaceAll("[^a-z0-9+ ]", "").trim();
        return mapped;
    }

    private static List<String> extractSkillsFromSkillsSection(String text) {
        List<String> skills = new ArrayList<>();
        if (text == null || text.isBlank()) return skills;

        String lower = text.toLowerCase();
        int start = -1;
        String[] startKeywords = {"software skills", "technical skills", "skills", "engineering skills"};
        for (String k : startKeywords) {
            int i = lower.indexOf(k);
            if (i >= 0 && (start == -1 || i < start)) start = i;
        }
        if (start == -1) return skills;

        // End at next major header
        String[] endKeywords = {"experience", "education", "projects", "certifications", "summary"};
        int end = text.length();
        for (String k : endKeywords) {
            int idx = lower.indexOf(k, start + 10);
            if (idx > 0 && idx < end) end = idx;
        }

        String skillsSection = text.substring(start, end);

        // Split by commas, semicolons, bullets, pipes
        String[] tokens = skillsSection.split("[,;|•·\\u2022\\n]");
        for (String token : tokens) {
            String normalized = normalizeSkillToken(token);
            if (!normalized.isEmpty() && KNOWN_SKILLS.contains(normalized)) {
                skills.add(normalized);
            }
        }

        return skills.stream().distinct().collect(Collectors.toList());
    }

    private static List<String> extractSkillsByDictionaryScan(String text) {
        List<String> found = new ArrayList<>();
        if (text == null || text.isBlank()) return found;
        String lower = text.toLowerCase();
        // look for whole word matches for known skills (prefer multi-word skills first)
        // sort KNOWN_SKILLS by length descending to match "spring boot" before "spring"
        List<String> sorted = KNOWN_SKILLS.stream()
                .sorted((a, b) -> Integer.compare(b.length(), a.length()))
                .collect(Collectors.toList());

        for (String skill : sorted) {
            String s = skill.toLowerCase();
            // word boundary match
            if (lower.matches("(?s).*\\b" + Pattern.quote(s) + "\\b.*")) {
                found.add(normalizeSkillToken(s));
            }
        }
        return found.stream().distinct().collect(Collectors.toList());
    }

    private static List<String> extractSkillsFromResume(String resumeText) {
        // 1. Preprocess the text (fix hyphens, join wrapped lines)
        String cleaned = preprocessResumeText(resumeText);

        // 2. Try targeted skills-section extraction
        List<String> fromSection = extractSkillsFromSkillsSection(cleaned);

        // 3. Fallback dictionary scan across whole document
        List<String> byDict = extractSkillsByDictionaryScan(cleaned);

        // 4. Combine with priority to section results, but include dictionary-only matches
        LinkedHashSet<String> combined = new LinkedHashSet<>();
        // add from section first (they are more explicit)
        combined.addAll(fromSection);
        // add dictionary hits (may include things missing from section)
        combined.addAll(byDict);

        // 5. Final normalization (if SKILL_NORMALIZATION_MAP produces new forms)

        return combined.stream()
                .map(ResumeServiceImpl::normalizeSkill) // reuse your normalizeSkill if preferred
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }
}
