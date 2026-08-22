package com.example.registeration.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otp, String purpose) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);

            String subject;
            String htmlContent;

            if ("password_reset".equalsIgnoreCase(purpose)) {
                subject = "Reset Your Password - Verification OTP";
                htmlContent = getPasswordResetHtmlTemplate(otp);
            } else {
                subject = "Verify Your Email - Verification OTP";
                htmlContent = getRegistrationHtmlTemplate(otp);
            }

            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("SMTP: Successfully sent OTP email to " + toEmail + " with purpose: " + purpose);
        } catch (MessagingException e) {
            System.err.println("SMTP Error: Failed to send email to " + toEmail + ". Error: " + e.getMessage());
            throw new RuntimeException("Failed to send OTP email. Please try again.", e);
        }
    }

    private String getRegistrationHtmlTemplate(String otp) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='utf-8'>\n" +
                "    <style>\n" +
                "        body { font-family: 'Outfit', 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #f4f6f9; margin: 0; padding: 0; -webkit-font-smoothing: antialiased; }\n" +
                "        .email-container { max-width: 550px; margin: 40px auto; background-color: #ffffff; border-radius: 16px; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05); overflow: hidden; border: 1px solid #eaeef2; }\n" +
                "        .header { background: linear-gradient(135deg, #4f46e5 0%, #6366f1 100%); padding: 35px 20px; text-align: center; color: #ffffff; }\n" +
                "        .header h1 { margin: 0; font-size: 24px; font-weight: 700; letter-spacing: -0.5px; }\n" +
                "        .content { padding: 40px 30px; text-align: center; color: #334155; line-height: 1.6; }\n" +
                "        .content p { font-size: 16px; margin-top: 0; margin-bottom: 24px; }\n" +
                "        .otp-card { background-color: #f8fafc; border: 1px dashed #cbd5e1; border-radius: 12px; padding: 20px; margin: 24px 0; display: inline-block; letter-spacing: 6px; font-size: 32px; font-weight: 800; color: #4f46e5; }\n" +
                "        .footer { background-color: #f8fafc; padding: 25px 20px; text-align: center; font-size: 13px; color: #64748b; border-top: 1px solid #eaeef2; }\n" +
                "        .footer p { margin: 5px 0; }\n" +
                "        .btn { display: inline-block; background-color: #4f46e5; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 8px; font-weight: 600; margin-top: 15px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='email-container'>\n" +
                "        <div class='header'>\n" +
                "            <h1>Welcome to Prabhim One</h1>\n" +
                "        </div>\n" +
                "        <div class='content'>\n" +
                "            <p>Thank you for registering! Please use the following One-Time Password (OTP) to complete your email verification. This OTP is valid for 10 minutes.</p>\n" +
                "            <div class='otp-card'>" + otp + "</div>\n" +
                "            <p style='font-size: 14px; color: #64748b;'>If you did not initiate this request, you can safely ignore this email.</p>\n" +
                "        </div>\n" +
                "        <div class='footer'>\n" +
                "            <p>&copy; 2026 Prabhim One. All rights reserved.</p>\n" +
                "            <p>Secured Authentication Platform</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    private String getPasswordResetHtmlTemplate(String otp) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='utf-8'>\n" +
                "    <style>\n" +
                "        body { font-family: 'Outfit', 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #f4f6f9; margin: 0; padding: 0; -webkit-font-smoothing: antialiased; }\n" +
                "        .email-container { max-width: 550px; margin: 40px auto; background-color: #ffffff; border-radius: 16px; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05); overflow: hidden; border: 1px solid #eaeef2; }\n" +
                "        .header { background: linear-gradient(135deg, #dc2626 0%, #ef4444 100%); padding: 35px 20px; text-align: center; color: #ffffff; }\n" +
                "        .header h1 { margin: 0; font-size: 24px; font-weight: 700; letter-spacing: -0.5px; }\n" +
                "        .content { padding: 40px 30px; text-align: center; color: #334155; line-height: 1.6; }\n" +
                "        .content p { font-size: 16px; margin-top: 0; margin-bottom: 24px; }\n" +
                "        .otp-card { background-color: #fff5f5; border: 1px dashed #fca5a5; border-radius: 12px; padding: 20px; margin: 24px 0; display: inline-block; letter-spacing: 6px; font-size: 32px; font-weight: 800; color: #dc2626; }\n" +
                "        .footer { background-color: #f8fafc; padding: 25px 20px; text-align: center; font-size: 13px; color: #64748b; border-top: 1px solid #eaeef2; }\n" +
                "        .footer p { margin: 5px 0; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='email-container'>\n" +
                "        <div class='header'>\n" +
                "            <h1>Password Reset Request</h1>\n" +
                "        </div>\n" +
                "        <div class='content'>\n" +
                "            <p>We received a request to reset your password. Use the verification OTP below to complete the action. This OTP is valid for 10 minutes.</p>\n" +
                "            <div class='otp-card'>" + otp + "</div>\n" +
                "            <p style='font-weight: 600; color: #dc2626;'>Never share this OTP with anyone.</p>\n" +
                "            <p style='font-size: 14px; color: #64748b;'>If you did not request a password reset, please secure your account immediately.</p>\n" +
                "        </div>\n" +
                "        <div class='footer'>\n" +
                "            <p>&copy; 2026 Prabhim One. All rights reserved.</p>\n" +
                "            <p>Secured Authentication Platform</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}
