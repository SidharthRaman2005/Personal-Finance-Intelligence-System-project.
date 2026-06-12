package com.financeapp.backend.service.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SignupOtpEmailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public SignupOtpEmailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from:no-reply@financeapp.local}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendOtp(String toEmail, String otp, int expirySeconds) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Your Finance App signup OTP");
        message.setText(buildBody(otp, expirySeconds));

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new RuntimeException("Unable to send OTP email right now. Please try again", ex);
        }
    }

    private String buildBody(String otp, int expirySeconds) {
        long expiryMinutes = Math.max(1, expirySeconds / 60L);
        return "Your OTP is: " + otp + "\n\n"
                + "This OTP is valid for " + expiryMinutes + " minutes.\n"
                + "If you did not request this, please ignore this email.";
    }
}
