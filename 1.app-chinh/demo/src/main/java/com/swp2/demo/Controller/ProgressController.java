package com.swp2.demo.Controller;

import com.swp2.demo.entity.QuitPlan;
import com.swp2.demo.entity.UserPlanStep;
import com.swp2.demo.Repository.QuitPlanRepository;
import com.swp2.demo.repository.UserPlanStepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ProgressController {

    @Autowired
    private QuitPlanRepository quitPlanRepository;

    @Autowired
    private UserPlanStepRepository userPlanStepRepository;

    public static class HealthProgress {
        private String currentStatus;
        private int percentage;
        private String nextMilestone;

        public HealthProgress(String currentStatus, int percentage, String nextMilestone) {
            this.currentStatus = currentStatus;
            this.percentage = percentage;
            this.nextMilestone = nextMilestone;
        }

        public String getCurrentStatus() {
            return currentStatus;
        }

        public int getPercentage() {
            return percentage;
        }

        public String getNextMilestone() {
            return nextMilestone;
        }
    }

    public static class Milestone {
        private String name;
        private int daysToAchieve;

        public Milestone(String name, int daysToAchieve) {
            this.name = name;
            this.daysToAchieve = daysToAchieve;
        }

        public String getName() {
            return name;
        }

        public int getDaysToAchieve() {
            return daysToAchieve;
        }
    }

    @GetMapping("/track-progress")
    public String trackProgress(Model model) {
        Long userId = 1L; // 👉 thay bằng user thật sau này
        QuitPlan plan = quitPlanRepository.findByUserId(userId).stream().findFirst().orElse(null);
        if (plan == null) {
            model.addAttribute("error", "Chưa có kế hoạch bỏ thuốc.");
            return "error";
        }
        System.out.println("🔍 Start Date from DB: " + plan.getStartDate());
        System.out.println("🕒 Server Timezone: " + ZoneId.systemDefault());

        ZoneId zoneVN = ZoneId.of("Asia/Ho_Chi_Minh");
        ZonedDateTime now = ZonedDateTime.now(zoneVN);
        ZonedDateTime startDateTime = plan.getStartDate().atStartOfDay(zoneVN);

        long daysSinceQuit = Duration.between(startDateTime, now).toDays();
        if (daysSinceQuit < 0) daysSinceQuit = 0;

        int cigarettesPerDay = 20; // bạn có thể lấy từ plan nếu có
        BigDecimal pricePerCigarette = new BigDecimal("1.20");

        long cigarettesAvoided = daysSinceQuit * cigarettesPerDay;
        BigDecimal moneySaved = pricePerCigarette.multiply(BigDecimal.valueOf(cigarettesAvoided));

        model.addAttribute("daysSinceQuit", daysSinceQuit);
        model.addAttribute("moneySaved", String.format("$%.2f", moneySaved));
        model.addAttribute("cigarettesAvoided", cigarettesAvoided);
        model.addAttribute("healthProgress", getHealthProgress(daysSinceQuit));

        List<Milestone> allMilestones = defineMilestones();
        model.addAttribute("achievedMilestones", getAchievedMilestones(daysSinceQuit, allMilestones));
        model.addAttribute("upcomingMilestones", getUpcomingMilestones(daysSinceQuit, allMilestones));

        List<UserPlanStep> steps = userPlanStepRepository.findByQuitPlan(plan);
        model.addAttribute("steps", steps);
        model.addAttribute("planId", plan.getId());

        return "track-progress";
    }

    @PostMapping("/track-progress/update/{stepId}")
    public String updateStep(
            @PathVariable Long stepId,
            @RequestParam("actualCigarettes") int actual,
            @RequestParam("planId") Long planId) {

        UserPlanStep step = userPlanStepRepository.findById(stepId).orElse(null);
        if (step != null) {
            step.setActualCigarettes(actual);
            step.setCompleted(actual <= step.getTargetCigarettes());
            userPlanStepRepository.save(step);
        }
        return "redirect:/track-progress";
    }

    private HealthProgress getHealthProgress(long days) {
        if (days < 1)
            return new HealthProgress("Nhịp tim bắt đầu giảm.", 10, "Mức CO trong máu giảm (1 ngày)");
        if (days < 3)
            return new HealthProgress("Mức CO giảm, Oxy tăng.", 25, "Nicotine được đào thải (3 ngày)");
        if (days < 14)
            return new HealthProgress("Nicotine đã được đào thải.", 40, "Chức năng phổi cải thiện (2 tuần)");
        if (days < 30)
            return new HealthProgress("Chức năng phổi & hô hấp cải thiện.", 60, "Tuần hoàn máu tốt hơn (1 tháng)");
        if (days < 90)
            return new HealthProgress("Tuần hoàn máu tốt, năng lượng tăng.", 80, "Phổi bắt đầu tự sửa chữa (3 tháng)");
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
                .limit(2)
                .collect(Collectors.toList());
    }
}
