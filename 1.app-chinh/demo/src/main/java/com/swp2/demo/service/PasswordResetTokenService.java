package com.swp2.demo.service;

import com.swp2.demo.Repository.PasswordResetTokenRepository;
import com.swp2.demo.entity.PasswordResetToken;
import com.swp2.demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetTokenService {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailService emailService;

    public void createOrUpdatePasswordResetToken(User user) {
        String newToken = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(2);

        Optional<PasswordResetToken> existingToken = passwordResetTokenRepository.findByUser(user);

        if (existingToken.isPresent()) {
            PasswordResetToken token = existingToken.get();
            token.setToken(newToken);
            token.setExpiryDate(expiryDate);
            passwordResetTokenRepository.save(token);
        } else {
            PasswordResetToken token = new PasswordResetToken(newToken, expiryDate, user);
            passwordResetTokenRepository.save(token);
        }

        // Gửi email
        String resetLink = "http://localhost:8080/reset-password?token=" + newToken;
        emailService.sendEmail(user.getEmail(), "Reset Password", "Click here to reset: " + resetLink);
    }
}
