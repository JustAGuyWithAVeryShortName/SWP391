package com.swp2.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ProgressController {

    @GetMapping("/track-progress")
    public String trackProgress(Model model) {
        LocalDate quitDate = LocalDate.of(2025, 5, 20);
        long daysSinceQuit = ChronoUnit.DAYS.between(quitDate, LocalDate.now());

        int cigarettesPerDay = 20;
        BigDecimal pricePerCigarette = BigDecimal.valueOf(0.1);
        long cigarettesAvoided = daysSinceQuit * cigarettesPerDay;
        BigDecimal moneySaved = pricePerCigarette.multiply(BigDecimal.valueOf(cigarettesAvoided));

        // Health status theo ngày bỏ thuốc
        String healthProgress;
        if (daysSinceQuit < 1) {
            healthProgress = "Your heart rate and blood pressure start to drop.";
        } else if (daysSinceQuit < 3) {
            healthProgress = "Carbon monoxide level drops. Oxygen increases.";
        } else if (daysSinceQuit < 7) {
            healthProgress = "Nicotine fully leaves your system.";
        } else if (daysSinceQuit < 30) {
            healthProgress = "Lung function and breathing improve.";
        } else if (daysSinceQuit < 90) {
            healthProgress = "Circulation gets better. Energy increases.";
        } else {
            healthProgress = "Coughing reduces. Lung repair in progress.";
        }

        // Milestones động theo thời gian
        List<String> milestones = new ArrayList<>();
        if (daysSinceQuit >= 1) milestones.add("🎉 1 day smoke-free");
        if (daysSinceQuit >= 3) milestones.add("✅ 3 days: Nicotine out of your system");
        if (daysSinceQuit >= 7) milestones.add("🗓️ 1 week: Improved breathing");
        if (daysSinceQuit >= 30) milestones.add("🔥 1 month: Lung repair starts");
        if (daysSinceQuit >= 90) milestones.add("🏆 3 months: Energy & stamina up");

        model.addAttribute("daysSinceQuit", daysSinceQuit);
        model.addAttribute("moneySaved", "$" + moneySaved);
        model.addAttribute("cigarettesAvoided", cigarettesAvoided);
        model.addAttribute("healthProgress", healthProgress);
        model.addAttribute("milestones", milestones);

        return "track-progress";
    }
}
