package com.swp2.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ProgressController {

    // Model classes (can be in separate files)
    public static class HealthProgress {
        private String currentStatus;
        private int percentage;
        private String nextMilestone;
        // Constructors, Getters, and Setters
        public HealthProgress(String currentStatus, int percentage, String nextMilestone) {
            this.currentStatus = currentStatus;
            this.percentage = percentage;
            this.nextMilestone = nextMilestone;
        }
        public String getCurrentStatus() { return currentStatus; }
        public int getPercentage() { return percentage; }
        public String getNextMilestone() { return nextMilestone; }
    }

    public static class Milestone {
        private String name;
        private int daysToAchieve;
        // Constructors, Getters, and Setters
        public Milestone(String name, int daysToAchieve) {
            this.name = name;
            this.daysToAchieve = daysToAchieve;
        }
        public String getName() { return name; }
        public int getDaysToAchieve() { return daysToAchieve; }
    }


    @GetMapping("/track-progress")
    public String trackProgress(Model model) {
        // --- GIẢ LẬP: Dữ liệu này nên lấy từ user đang đăng nhập ---
        LocalDate quitDate = LocalDate.of(2025, 5, 20);
        int cigarettesPerDay = 20;
        BigDecimal pricePerCigarette = new BigDecimal("1.20"); // Cập nhật giá
        // -----------------------------------------------------------

        long daysSinceQuit = ChronoUnit.DAYS.between(quitDate, LocalDate.now());
        if (daysSinceQuit < 0) daysSinceQuit = 0; // Đảm bảo không âm

        long cigarettesAvoided = daysSinceQuit * cigarettesPerDay;
        BigDecimal moneySaved = pricePerCigarette.multiply(BigDecimal.valueOf(cigarettesAvoided));

        model.addAttribute("daysSinceQuit", daysSinceQuit);
        model.addAttribute("moneySaved", String.format("$%.2f", moneySaved));
        model.addAttribute("cigarettesAvoided", cigarettesAvoided);
        model.addAttribute("healthProgress", getHealthProgress(daysSinceQuit));

        List<Milestone> allMilestones = defineMilestones();
        model.addAttribute("achievedMilestones", getAchievedMilestones(daysSinceQuit, allMilestones));
        model.addAttribute("upcomingMilestones", getUpcomingMilestones(daysSinceQuit, allMilestones));

        return "track-progress-improved"; // Trả về template mới
    }

    private HealthProgress getHealthProgress(long days) {
        if (days < 1) return new HealthProgress("Nhịp tim bắt đầu giảm.", 10, "Mức CO trong máu giảm (1 ngày)");
        if (days < 3) return new HealthProgress("Mức CO giảm, Oxy tăng.", 25, "Nicotine được đào thải (3 ngày)");
        if (days < 14) return new HealthProgress("Nicotine đã được đào thải.", 40, "Chức năng phổi cải thiện (2 tuần)");
        if (days < 30) return new HealthProgress("Chức năng phổi & hô hấp cải thiện.", 60, "Tuần hoàn máu tốt hơn (1 tháng)");
        if (days < 90) return new HealthProgress("Tuần hoàn máu tốt, năng lượng tăng.", 80, "Giảm ho, phổi bắt đầu tự sửa chữa (3 tháng)");
        return new HealthProgress("Phổi đang trong quá trình tự sửa chữa mạnh mẽ.", 100, "Bạn đang làm rất tốt!");
    }

    private List<Milestone> defineMilestones() {
        List<Milestone> milestones = new ArrayList<>();
        milestones.add(new Milestone("1 Ngày Vàng", 1));
        milestones.add(new Milestone("Thanh Lọc Nicotine", 3));
        milestones.add(new Milestone("Tuần Đầu Tiên", 7));
        milestones.add(new Milestone("Hô Hấp Cải Thiện", 14));
        milestones.add(new Milestone("Chạm Mốc 1 Tháng", 30));
        milestones.add(new Milestone("Năng Lượng Trở Lại", 90));
        milestones.add(new Milestone("Nửa Năm Kiên Trì", 180));
        milestones.add(new Milestone("Chúc Mừng 1 Năm", 365));
        return milestones;
    }

    private List<Milestone> getAchievedMilestones(long days, List<Milestone> all) {
        return all.stream()
                .filter(m -> days >= m.getDaysToAchieve())
                .collect(Collectors.toList());
    }

    private List<Milestone> getUpcomingMilestones(long days, List<Milestone> all) {
        return all.stream()
                .filter(m -> days < m.getDaysToAchieve())
                .limit(2) // Chỉ hiển thị 2 cột mốc sắp tới
                .collect(Collectors.toList());
    }
}