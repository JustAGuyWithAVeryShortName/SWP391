package com.swp2.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class UserController {

    @GetMapping("/dashboard")
    public String userDashboard(Model model, Principal principal) {
        String username = principal.getName();
        model.addAttribute("username", username);
        return "userpage";
    }
}