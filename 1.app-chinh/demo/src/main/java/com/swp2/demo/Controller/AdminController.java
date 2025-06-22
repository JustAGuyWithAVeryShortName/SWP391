package com.swp2.demo.Controller;

import com.swp2.demo.Repository.OrderRepository;
import com.swp2.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/admin")
    public String admin(Model model) {
        long totalMembers = userRepository.count();
        long todayMembers = userRepository.countTodayMembers();
        long pendingOrders = orderRepository.countByStatus("PENDING");

        model.addAttribute("totalMembers", totalMembers);
        model.addAttribute("todayMembers", todayMembers);
        model.addAttribute("pendingOrders", pendingOrders);
        return "admin";
    }
}
