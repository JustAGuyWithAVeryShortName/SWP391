package com.swp2.demo.security;


import com.swp2.demo.Repository.UserRepository;
import com.swp2.demo.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
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

        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        HttpSession session = request.getSession();
        session.setAttribute("loggedInUser", user); // ✅ GÁN USER VÀO SESSION

        response.sendRedirect("/dashboard"); // ✅ Chuyển hướng sau login thành công
    }
}