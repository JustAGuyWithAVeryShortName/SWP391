package com.swp2.demo.Controller;

import com.swp2.demo.Repository.PasswordResetTokenRepository;
import com.swp2.demo.Repository.UserRepository;
import com.swp2.demo.entity.PasswordResetToken;
import com.swp2.demo.entity.User;
import com.swp2.demo.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class ForgotPasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password"; // tên file Thymeleaf template = forgot-password.html
    }


    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model, HttpServletRequest request) {
        User user = userRepository.findByEmail(email);

        if (user != null) {
            String token = UUID.randomUUID().toString();

            PasswordResetToken passwordResetToken = new PasswordResetToken();
            passwordResetToken.setToken(token);
            passwordResetToken.setUser(user);
            passwordResetToken.setExpiryDate(LocalDateTime.now().plusHours(2));
            tokenRepository.save(passwordResetToken);

            String resetUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/reset-password?token=" + token;
            String content = "Để đặt lại mật khẩu, vui lòng nhấn vào liên kết sau:\n" + resetUrl;

            emailService.sendEmail(user.getEmail(), "Đặt lại mật khẩu", content);

            model.addAttribute("message", "Đã gửi email đặt lại mật khẩu!");
        } else {
            model.addAttribute("error", "Không tìm thấy email.");
        }
        return "forgot-password"; // Thymeleaf template
    }
}
