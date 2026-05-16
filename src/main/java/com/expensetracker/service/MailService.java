package com.expensetracker.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendPasswordResetMail(String to, String token) {
        long startTime = System.currentTimeMillis();
        logger.info("Sending password reset email to {}", to);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Expense Tracker - Password Reset");
        message.setText("To reset your password, use the following token:\n\n" + token + "\n\nIf you did not request a password reset, please ignore this email.");
        try {
            mailSender.send(message);
            logger.info("Password reset email sent successfully to {} in {}ms", to, (System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Async
    public void sendEmailWithAttachment(String to, String subject, String text, String attachmentName, byte[] attachmentData) {
        long startTime = System.currentTimeMillis();
        logger.info("Sending email with attachment '{}' to {}", attachmentName, to);
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
            logger.info("Email with attachment sent successfully to {} in {}ms", to, (System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            logger.error("Failed to send email with attachment to {}: {}", to, e.getMessage(), e);
        }
    }
}
