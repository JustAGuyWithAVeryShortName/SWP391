package com.swp2.demo.rest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class MainController {
    @GetMapping("/Main/{x}")
    public String Main(@PathVariable int x, Model model) {
        model.addAttribute("number", x);
        return "index";
    }

    @GetMapping("/about")
    public String aboutPage() {
        return "about us";
}

}

