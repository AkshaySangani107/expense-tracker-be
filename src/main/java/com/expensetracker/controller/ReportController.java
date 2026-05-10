package com.expensetracker.controller;

import com.expensetracker.security.CurrentUser;
import com.expensetracker.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final com.expensetracker.service.MailService mailService;

    public ReportController(ReportService reportService, com.expensetracker.service.MailService mailService) {
        this.reportService = reportService;
        this.mailService = mailService;
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportReport(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        byte[] pdfBytes = reportService.generatePdfReport(currentUser.id(), startDate, endDate);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Expense_Report_" + startDate + "_to_" + endDate + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PostMapping("/email")
    public ResponseEntity<Void> emailReport(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        byte[] pdfBytes = reportService.generatePdfReport(currentUser.id(), startDate, endDate);
        
        String subject = "Your Expense Report (" + startDate + " to " + endDate + ")";
        String text = "Hi " + currentUser.name() + ",\n\nPlease find your requested expense report attached.\n\nThanks,\nExpense Tracker Team";
        String attachmentName = "Expense_Report_" + startDate + "_to_" + endDate + ".pdf";
        
        mailService.sendEmailWithAttachment(currentUser.email(), subject, text, attachmentName, pdfBytes);
        
        return ResponseEntity.ok().build();
    }
}
