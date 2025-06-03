package com.swp2.demo.rest.controller;

import ch.qos.logback.core.model.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }
    @GetMapping("/aboutUs")
    public String AboutUs() {
        return "aboutus";
    }
    @GetMapping("readMore")
    public String readMore() {
        return "readMore";
    }
    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }


}