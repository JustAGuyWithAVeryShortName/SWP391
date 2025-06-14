package com.swp2.demo.Controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller responsible for handling requests related to
 * creating and viewing the user's quit smoking plan.
 */
@Controller
public class QuitPlanController {

    /**
     * Show the form for the user to enter quit smoking plan details.
     *
     * @param model Model to pass data to the view.
     * @return View name "quit-plan".
     */
    @GetMapping("/quit-plan")
    public String showQuitPlanForm(Model model) {
        // If the model does not contain the "plan" object (e.g., first visit),
        // create a new one for form binding.
        if (!model.containsAttribute("plan")) {
            model.addAttribute("plan", new QuitPlan());
        }
        return "quit-plan";
    }

    /**
     * Handle form submission for creating the quit plan.
     * This method performs validation, generates default plan content if needed,
     * saves the plan in user's session, then redirects to dashboard.
     *
     * @param plan               The QuitPlan object bound from the form.
     * @param reasons            List of selected reasons.
     * @param reasonDetails      Details for "other" reason.
     * @param session            HttpSession object to store the plan.
     * @param redirectAttributes For passing error messages back to the form on redirect.
     * @return Redirect string to dashboard or back to form if errors.
     */
    @PostMapping("/plan/generate")
    public String handlePlanSubmission(@ModelAttribute("plan") QuitPlan plan,
                                       @RequestParam(required = false) List<String> reasons,
                                       @RequestParam(required = false) String reasonDetails,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {

        // Aggregate and process reasons from form
        populatePlanFromRequest(plan, reasons, reasonDetails);

        // --- Validation ---
        if (plan.getStartDate() != null && plan.getTargetDate() != null &&
                plan.getTargetDate().isBefore(plan.getStartDate())) {
            // Target date must be same or after start date
            redirectAttributes.addFlashAttribute("dateError", "Target date must be the same as or after the start date.");
            redirectAttributes.addFlashAttribute("plan", plan); // Send back entered data
            return "redirect:/quit-plan";
        }

        // --- Automatically generate suggested plan content ---
        // If user didn't enter custom plan and stage is not "custom"
        if ((plan.getCustomPlan() == null || plan.getCustomPlan().isBlank()) && !"custom".equals(plan.getStages())) {
            String suggestion = generatePlanSuggestion(plan);
            plan.setCustomPlan(suggestion);
        }

        // --- Save plan to session ---
        // Important to keep user-specific state
        session.setAttribute("userQuitPlan", plan);

        // Redirect to dashboard to prevent form resubmission on refresh
        return "redirect:/user/plan";
    }

    /**
     * Show the dashboard page with detailed user quit plan.
     *
     * @param session HttpSession to retrieve saved plan.
     * @param model   Model to pass data to view.
     * @return View name "user-plan" or redirect if no plan found.
     */
    @GetMapping("/user/plan")
    public String viewUserPlan(HttpSession session, Model model) {
        // Get plan from session
        QuitPlan currentPlan = (QuitPlan) session.getAttribute("userQuitPlan");

        if (currentPlan != null) {
            // If plan exists, calculate stats and pass to view
            model.addAttribute("plan", currentPlan);
            model.addAttribute("progress", calculateProgress(currentPlan));
            model.addAttribute("calendar", generateCalendarData(currentPlan));
            model.addAttribute("stats", calculateStats(currentPlan)); // Data for stats cards
        } else {
            // If no plan found, redirect to create new one
            return "redirect:/quit-plan";
        }
        return "user-plan";
    }

    // ===================================================================================
    // PRIVATE HELPER METHODS
    // ===================================================================================

    private void populatePlanFromRequest(QuitPlan plan, List<String> reasons, String reasonDetails) {
        List<String> fullReasons = new ArrayList<>();
        if (reasons != null) {
            fullReasons.addAll(reasons.stream()
                    .map(this::mapReasonValueToText) // Map values to readable text
                    .collect(Collectors.toList()));
        }
        // Add detailed reason if user entered it
        if (reasonDetails != null && !reasonDetails.isBlank()) {
            // Remove default "Other" and replace with details
            fullReasons.remove("Other");
            fullReasons.add(reasonDetails);
        }
        plan.setReasons(fullReasons);
    }

    private String mapReasonValueToText(String value) {
        return switch (value) {
            case "health" -> "Improve health";
            case "family" -> "For family and loved ones";
            case "financial" -> "Save money";
            case "smell" -> "Eliminate bad smell";
            case "other" -> "Other"; // Keep as placeholder
            default -> value;
        };
    }

    private String generatePlanSuggestion(QuitPlan plan) {
        if (plan.getStages() == null) return "Please select a method to get detailed suggestions.";
        return switch (plan.getStages()) {
            case "Gradual reduction" -> "Week 1: Reduce 2 cigarettes per day.\nWeek 2: Reduce another 3 cigarettes per day.\nWeek 3: Try to go 24 hours without smoking.\nAlways have sugar-free gum or healthy snacks ready.";
            case "Cold turkey (quit abruptly)" -> "Throw away all cigarettes, lighters, and ashtrays.\nInform family and friends about your decision for support.\nWhen cravings hit, drink a glass of cold water or take a walk.";
            case "Nicotine replacement therapy" -> "Start using nicotine patches from " + plan.getStartDate() + ".\nConsult a pharmacist for appropriate dosage.\nCombine with habit changes (e.g., drink tea instead of coffee).";
            default -> "";
        };
    }

    private int calculateProgress(QuitPlan plan) {
        if (plan.getStartDate() == null || plan.getTargetDate() == null || plan.getStartDate().isAfter(plan.getTargetDate())) return 0;

        LocalDate today = LocalDate.now();
        if (today.isBefore(plan.getStartDate())) return 0;
        if (today.isAfter(plan.getTargetDate())) return 100;

        long totalDays = ChronoUnit.DAYS.between(plan.getStartDate(), plan.getTargetDate());
        if (totalDays <= 0) return 100;

        long daysPassed = ChronoUnit.DAYS.between(plan.getStartDate(), today);
        long progress = (daysPassed * 100) / totalDays;
        return (int) Math.min(progress, 100);
    }

    /**
     * Calculate key stats for dashboard.
     */
    private QuitStats calculateStats(QuitPlan plan) {
        if (plan.getStartDate() == null || plan.getTargetDate() == null) {
            return new QuitStats(0, 0);
        }

        LocalDate today = LocalDate.now();
        long daysSmokeFree = 0;
        // Days smoke-free from start date to today
        if (!today.isBefore(plan.getStartDate())) {
            daysSmokeFree = ChronoUnit.DAYS.between(plan.getStartDate(), today);
        }

        long daysRemaining = 0;
        // Days left from today to target date
        if (today.isBefore(plan.getTargetDate())) {
            daysRemaining = ChronoUnit.DAYS.between(today, plan.getTargetDate());
        }

        return new QuitStats(daysSmokeFree, daysRemaining);
    }

    private CalendarData generateCalendarData(QuitPlan plan) {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        LocalDate firstDayOfMonth = currentMonth.atDay(1);
        int firstDayOfWeekValue = firstDayOfMonth.getDayOfWeek().getValue(); // Monday=1, Sunday=7

        List<CalendarDay> days = new ArrayList<>();
        // Add empty slots before first day of month
        for (int i = 1; i < firstDayOfWeekValue; i++) {
            days.add(new CalendarDay(0, "empty"));
        }

        // Add days of the month
        for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
            LocalDate date = currentMonth.atDay(day);
            String status = getDayStatus(date, today, plan.getStartDate(), plan.getTargetDate());
            days.add(new CalendarDay(day, status));
        }

        return new CalendarData(currentMonth.toString(), List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"), days);
    }

    private String getDayStatus(LocalDate date, LocalDate today, LocalDate startDate, LocalDate targetDate) {
        if (startDate == null || targetDate == null) {
            return "disabled"; // Không đủ thông tin để xác định trạng thái
        }

        if (date.isEqual(today)) return "today";
        if (date.isBefore(startDate)) return "pre-plan";
        if (date.isEqual(targetDate)) return "post-plan"; // Ngày target

        if (date.isAfter(targetDate)) return "upcoming"; // Các ngày sau target

        // Trong khoảng thời gian của kế hoạch
        if (date.isBefore(today)) return "past";   // Đã qua
        if (date.isAfter(today)) return "future";  // Sắp tới

        return "today"; // fallback
    }




    // ===================================================================================
    // DATA CLASSES (DTOs)
    // ===================================================================================

    /**
     * Represents a user's quit smoking plan.
     */
    public static class QuitPlan {
        private List<String> reasons = new ArrayList<>();
        private LocalDate startDate;
        private LocalDate targetDate;
        private String stages;
        private String customPlan;

        // Getters and setters
        public List<String> getReasons() { return reasons; }
        public void setReasons(List<String> reasons) { this.reasons = reasons; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getTargetDate() { return targetDate; }
        public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
        public String getStages() { return stages; }
        public void setStages(String stages) { this.stages = stages; }
        public String getCustomPlan() { return customPlan; }
        public void setCustomPlan(String customPlan) { this.customPlan = customPlan; }
    }

    /**
     * Dashboard statistics record.
     * @param daysSmokeFree number of days without smoking.
     * @param daysRemaining days left to the target.
     */
    public record QuitStats(long daysSmokeFree, long daysRemaining) {}

    /**
     * Calendar data container for the month view.
     */
    public record CalendarData(String monthName, List<String> weekDays, List<CalendarDay> days) {}

    /**
     * Information about a calendar day.
     */
    public record CalendarDay(int dayNumber, String status) {}
}
