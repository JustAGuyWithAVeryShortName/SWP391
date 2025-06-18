package com.swp2.demo.security;

import com.swp2.demo.Repository.UserRepository;

import com.swp2.demo.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        HttpSession session = request.getSession();
        User user = null;

        if (authentication.getPrincipal() instanceof OAuth2User oauthUser) {
            // OAuth2 login (Google)
            String email = oauthUser.getAttribute("email");
            if (email != null) {
                user = userRepository.findByEmail(email);
                if (user == null) {
                    // Có thể thêm trường hợp gán ROLE mặc định hoặc thông tin khác nếu cần
                    user = new User();
                    user.setEmail(email);
                    user.setUsername(email); // Cho email là username luôn
                    user.setPassword(""); // Không dùng password cho OAuth2
                   // user.setRole("User");  // 👈 Nếu bạn có phân quyền role, gán ở đây
                    userRepository.save(user);
                }
            }
        } else if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            // Form login (username-password)
            user = userRepository.findByUsername(userDetails.getUsername());
        }

        if (user != null) {
            session.setAttribute("loggedInUser", user);
        }

        // ✅ Redirect về URL trước đó nếu có
        var savedRequest = new HttpSessionRequestCache().getRequest(request, response);
        if (savedRequest != null) {
            response.sendRedirect(savedRequest.getRedirectUrl());
        } else {
            response.sendRedirect("/dashboard"); // fallback nếu không có URL trước đó
        }
    }
}
