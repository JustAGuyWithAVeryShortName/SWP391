package com.swp2.demo.security;

import com.swp2.demo.Repository.UserRepository;

import com.swp2.demo.entity.User;
import com.swp2.demo.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        HttpSession session = request.getSession();

        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            // Login thường
            User user = userService.findByUsername(userDetails.getUsername());
            session.setAttribute("loggedInUser", user);

        } else if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            // OAuth2 login
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");


            User user = userService.findByEmail(email);
            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setUsername(email);
                user.setFirstName(name);
                user.setLastName(null);
                user.setGender(null);
                user.setDateOfBirth(null);
                user.setRole(com.swp2.demo.entity.Role.Guest);
                user.setPassword(UUID.randomUUID().toString());
                userService.save(user);
                session.setAttribute("needsProfileUpdate", true);
            }
            session.setAttribute("loggedInUser", user);
        }

        // Nếu user mới ➔ chuyển tới trang cập nhật thông tin
        if (Boolean.TRUE.equals(session.getAttribute("needsProfileUpdate"))) {
            response.sendRedirect("/profile/edit");
        } else {
            response.sendRedirect("/dashboard");
        }
      //  response.sendRedirect("/dashboard");
    }
}

