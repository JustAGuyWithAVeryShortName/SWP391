package com.swp2.demo.security;



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
        User user = null;

        if (authentication.getPrincipal() instanceof com.swp2.demo.security.CustomUserDetails userDetails) {
             user = userService.findByUsername(userDetails.getUsername());

            session.setAttribute("loggedInUser", user);
            session.setAttribute("userId", user.getId());
        } else if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");



             user = userService.findByEmail(email);

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
            session.setAttribute("userId", user.getId());
        }
        if (user != null && user.getRole() == com.swp2.demo.entity.Role.Admin) {
            response.sendRedirect("/admin");  // Nếu là Admin thì vào trang admin
            return;
        }

        // Redirect nếu cần cập nhật profile
        if (Boolean.TRUE.equals(session.getAttribute("needsProfileUpdate"))) {
            response.sendRedirect("/profile/edit");
            return;
        }



        // Redirect về trang trước đó, nếu có
        String redirectUrl = (String) session.getAttribute("url_prior_login");
        if (redirectUrl != null) {
            session.removeAttribute("url_prior_login");
            response.sendRedirect(redirectUrl);

        } else {
            response.sendRedirect("/dashboard");
        }

    }
}
