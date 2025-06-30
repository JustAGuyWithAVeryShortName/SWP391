package com.swp2.demo.Controller;

import com.swp2.demo.entity.QuitPlan;
import com.swp2.demo.entity.UserPlanStep;
import com.swp2.demo.Repository.QuitPlanRepository;
import com.swp2.demo.repository.UserPlanStepRepository;
import com.swp2.demo.service.QuitPlanService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.temporal.ChronoUnit;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ProgressController {

    @Autowired
    private QuitPlanRepository quitPlanRepository;

    @Autowired
    private UserPlanStepRepository userPlanStepRepository;

    @Autowired
    private QuitPlanService quitPlanService;


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
    public String trackProgress(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        QuitPlan plan = quitPlanRepository.findLatestByUserId(userId).stream().findFirst().orElse(null);
        if (plan == null) {
            model.addAttribute("error", "Chưa có kế hoạch bỏ thuốc.");
            return "error";
        }

        ZoneId zoneVN = ZoneId.of("Asia/Ho_Chi_Minh");
        ZonedDateTime now = ZonedDateTime.now(zoneVN);
        ZonedDateTime startDateTime = plan.getStartDate().atStartOfDay(zoneVN);
        long daysSinceQuit = Duration.between(startDateTime, now).toDays();
        if (daysSinceQuit < 0) daysSinceQuit = 0;

        model.addAttribute("daysSinceQuit", daysSinceQuit);
        model.addAttribute("healthProgress", getHealthProgress(daysSinceQuit));
        model.addAttribute("achievedMilestones", getAchievedMilestones(daysSinceQuit, defineMilestones()));
        model.addAttribute("upcomingMilestones", getUpcomingMilestones(daysSinceQuit, defineMilestones()));

        // 💡 Chuẩn bị danh sách step đã xử lý completed + target
        List<UserPlanStep> originalSteps = userPlanStepRepository.findByQuitPlan(plan);
        List<UserPlanStep> displaySteps = new ArrayList<>();

        for (UserPlanStep step : originalSteps) {
            UserPlanStep copy = new UserPlanStep();
            copy.setId(step.getId());
            copy.setDate(step.getDate());
            copy.setDayIndex(step.getDayIndex());
            copy.setActualCigarettes(step.getActualCigarettes());
            copy.setQuitPlan(step.getQuitPlan());

            if ("quit_abruptly".equalsIgnoreCase(plan.getMethod())) {
                copy.setTargetCigarettes(0);
                if (copy.getActualCigarettes() != null) {
                    copy.setCompleted(copy.getActualCigarettes() == 0);
                }
            } else {
                copy.setTargetCigarettes(step.getTargetCigarettes());
                if (copy.getActualCigarettes() != null) {
                    copy.setCompleted(copy.getActualCigarettes() <= copy.getTargetCigarettes());
                }
            }
            displaySteps.add(copy);
        }

        // ✅ Gom theo tuần
        Map<Integer, List<UserPlanStep>> stepsByWeek = new TreeMap<>();
        for (UserPlanStep step : displaySteps) {
            int week = (int) ChronoUnit.WEEKS.between(plan.getStartDate(), step.getDate()) + 1;
            stepsByWeek.computeIfAbsent(week, k -> new ArrayList<>()).add(step);
        }

        model.addAttribute("stepsByWeek", stepsByWeek);
        model.addAttribute("planId", plan.getId());

        // 🧮 Tính toán số điếu đã tránh & tiền tiết kiệm
        int pricePerCigarette = 0;
        if (plan.getDailySmokingCigarettes() != null && plan.getDailySmokingCigarettes() > 0) {
            pricePerCigarette = plan.getDailySpending().intValue() / plan.getDailySmokingCigarettes();
        }

        int avoided = quitPlanService.calculateCigarettesAvoided(displaySteps);
        int saved = quitPlanService.calculateMoneySaved(displaySteps, pricePerCigarette);

        model.addAttribute("cigarettesAvoided", Math.max(0, avoided));
        model.addAttribute("cigarettesOver", Math.abs(Math.min(0, avoided)));
        model.addAttribute("moneySaved", String.format("%,d", saved));
        model.addAttribute("method", plan.getMethod());

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
        return Arrays.asList(
                new Milestone("1 Ngày Vàng", 1),
                new Milestone("Thanh Lọc Nicotine", 3),
                new Milestone("Tuần Đầu Tiên", 7),
                new Milestone("Hô Hấp Cải Thiện", 14),
                new Milestone("Chạm Mốc 1 Tháng", 30),
                new Milestone("Năng Lượng Trở Lại", 90),
                new Milestone("Nửa Năm Kiên Trì", 180),
                new Milestone("Chúc Mừng 1 Năm", 365)
        );
    }

    private List<Milestone> getAchievedMilestones(long days, List<Milestone> all) {
        return all.stream().filter(m -> days >= m.getDaysToAchieve()).collect(Collectors.toList());
    }

    private List<Milestone> getUpcomingMilestones(long days, List<Milestone> all) {
        return all.stream().filter(m -> days < m.getDaysToAchieve()).limit(2).collect(Collectors.toList());
    }
}
