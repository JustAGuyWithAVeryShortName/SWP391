package com.swp2.demo.rest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index"; // Trang chủ
    }

    @GetMapping("/about-us")
    public String aboutUs() {
        return "aboutus"; // Trang giới thiệu
    }

    @GetMapping("/read-more")
    public String readMore() {
        return "readMore"; // Trang đọc thêm
    }

    @GetMapping("/admin")
    public String adminDashboard() {
        return "admin"; // Trang quản trị
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Trang đăng nhập
    }


}
