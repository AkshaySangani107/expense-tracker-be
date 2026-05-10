package com.expensetracker.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetMail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Expense Tracker - Password Reset");
        message.setText("To reset your password, use the following token:\n\n" + token + "\n\nIf you did not request a password reset, please ignore this email.");
        try {
            mailSender.send(message);
        } catch (Exception e) {
            // Log error, but don't crash if dummy smtp isn't working
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    public void sendEmailWithAttachment(String to, String subject, String text, String attachmentName, byte[] attachmentData) {
        try {
            jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(message, true);
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);
            
            org.springframework.core.io.ByteArrayResource resource = new org.springframework.core.io.ByteArrayResource(attachmentData) {
                @Override
                public String getFilename() {
                    return attachmentName;
                }
            };
            
            helper.addAttachment(attachmentName, resource);
            
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email with attachment: " + e.getMessage());
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }
}
