package com.ielts.pdfservice.controller;

import com.ielts.pdfservice.dto.ReportRequest;
import com.ielts.pdfservice.service.PdfGeneratorService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pdf")
@CrossOrigin(origins = "*")
public class PdfController {

    private final PdfGeneratorService pdfService;

    public PdfController(PdfGeneratorService pdfService) {
        this.pdfService = pdfService;
    }

    @PostMapping("/generate-report")
    public ResponseEntity<byte[]> generateReport(@RequestBody ReportRequest request) {
        try {
            byte[] pdf = pdfService.generateReport(request);

            String filename = "IELTS_Report_" + sanitize(request.getStudentName()) + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("PDF Service is running");
    }

    private String sanitize(String name) {
        if (name == null) return "Student";
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}
