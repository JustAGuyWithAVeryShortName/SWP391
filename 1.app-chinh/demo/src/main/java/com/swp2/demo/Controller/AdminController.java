package com.swp2.demo.Controller;

import com.swp2.demo.Repository.OrderRepository;
import com.swp2.demo.Repository.UserRepository;
import com.swp2.demo.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        long freeCount = userRepository.countByMember(Member.FREE);
        long vipCount = userRepository.countByMember(Member.VIP);
        long premiumCount = userRepository.countByMember(Member.PREMIUM);

        model.addAttribute("totalMembers", totalMembers);
        model.addAttribute("todayMembers", todayMembers);
        model.addAttribute("pendingOrders", pendingOrders);

        model.addAttribute("labels", List.of("Free", "VIP", "Premium"));
        model.addAttribute("values", List.of(freeCount, vipCount, premiumCount));

        int currentYear = LocalDate.now().getYear();
        List<Object[]> results = userRepository.countUsersByMonth(currentYear);

        int[] monthlyUserData = new int[12];
        for (Object[] row : results) {
            int month = (Integer) row[0];
            Long count = (Long) row[1];
            monthlyUserData[month - 1] = count.intValue();  // month từ 1-12, array index từ 0
        }

        model.addAttribute("monthlyUserData", monthlyUserData);
        return "admin";
    }
}
