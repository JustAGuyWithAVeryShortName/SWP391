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
 * Controller chịu trách nhiệm xử lý các yêu cầu liên quan đến
 * việc tạo và xem kế hoạch cai thuốc của người dùng.
 */
@Controller
public class QuitPlanController {

    /**
     * Hiển thị form để người dùng nhập thông tin tạo kế hoạch cai thuốc.
     *
     * @param model Model để truyền dữ liệu tới view.
     * @return Tên view "quit-plan".
     */
    @GetMapping("/quit-plan")
    public String showQuitPlanForm(Model model) {
        // Nếu model chưa có đối tượng "plan" (ví dụ: khi người dùng truy cập lần đầu),
        // tạo một đối tượng mới để binding với form.
        if (!model.containsAttribute("plan")) {
            model.addAttribute("plan", new QuitPlan());
        }
        return "quit-plan";
    }

    /**
     * Xử lý việc nộp form tạo kế hoạch.
     * Phương thức này thực hiện xác thực, tạo nội dung kế hoạch mặc định (nếu cần),
     * và lưu kế hoạch vào session của người dùng, sau đó chuyển hướng đến trang dashboard.
     *
     * @param plan              Đối tượng QuitPlan được binding từ form.
     * @param reasons           Danh sách các lý do được chọn.
     * @param reasonDetails     Nội dung chi tiết cho "lý do khác".
     * @param session           Đối tượng HttpSession để lưu trữ kế hoạch của người dùng.
     * @param redirectAttributes Dùng để truyền thông báo lỗi về form sau khi chuyển hướng.
     * @return Chuỗi chuyển hướng đến trang dashboard hoặc quay lại form nếu có lỗi.
     */
    @PostMapping("/plan/generate")
    public String handlePlanSubmission(@ModelAttribute("plan") QuitPlan plan,
                                       @RequestParam(required = false) List<String> reasons,
                                       @RequestParam(required = false) String reasonDetails,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {

        // Tập hợp và xử lý các lý do từ form
        populatePlanFromRequest(plan, reasons, reasonDetails);

        // --- Xác thực dữ liệu (Validation) ---
        if (plan.getStartDate() != null && plan.getTargetDate() != null &&
                plan.getTargetDate().isBefore(plan.getStartDate())) {
            // Nếu ngày mục tiêu trước ngày bắt đầu, báo lỗi
            redirectAttributes.addFlashAttribute("dateError", "Ngày mục tiêu phải sau hoặc trùng với ngày bắt đầu.");
            redirectAttributes.addFlashAttribute("plan", plan); // Gửi lại dữ liệu người dùng đã nhập
            return "redirect:/quit-plan";
        }

        // --- Tự động tạo nội dung kế hoạch gợi ý ---
        // Nếu người dùng không tự nhập và không chọn phương pháp "Tùy chỉnh"
        if ((plan.getCustomPlan() == null || plan.getCustomPlan().isBlank()) && !"custom".equals(plan.getStages())) {
            String suggestion = generatePlanSuggestion(plan);
            plan.setCustomPlan(suggestion);
        }

        // --- Lưu kế hoạch vào Session ---
        // Đây là bước quan trọng để duy trì trạng thái cho từng người dùng
        session.setAttribute("userQuitPlan", plan);

        // Chuyển hướng đến trang xem kế hoạch (Dashboard) để tránh việc nộp lại form khi F5
        return "redirect:/user/plan";
    }

    /**
     * Hiển thị trang dashboard với kế hoạch chi tiết của người dùng.
     *
     * @param session Đối tượng HttpSession để lấy kế hoạch đã lưu.
     * @param model   Model để truyền dữ liệu tới view.
     * @return Tên view "user-plan" hoặc chuyển hướng nếu chưa có kế hoạch.
     */
    @GetMapping("/user/plan")
    public String viewUserPlan(HttpSession session, Model model) {
        // Lấy kế hoạch từ session
        QuitPlan currentPlan = (QuitPlan) session.getAttribute("userQuitPlan");

        if (currentPlan != null) {
            // Nếu có kế hoạch, tính toán các chỉ số và gửi tới view
            model.addAttribute("plan", currentPlan);
            model.addAttribute("progress", calculateProgress(currentPlan));
            model.addAttribute("calendar", generateCalendarData(currentPlan));
            model.addAttribute("stats", calculateStats(currentPlan)); // Dữ liệu cho các thẻ thống kê
        } else {
            // Nếu không tìm thấy kế hoạch trong session, yêu cầu người dùng tạo mới
            return "redirect:/quit-plan";
        }
        return "user-plan";
    }

    // ===================================================================================
    // CÁC PHƯƠNG THỨC HỖ TRỢ (PRIVATE HELPER METHODS)
    // ===================================================================================

    private void populatePlanFromRequest(QuitPlan plan, List<String> reasons, String reasonDetails) {
        List<String> fullReasons = new ArrayList<>();
        if (reasons != null) {
            fullReasons.addAll(reasons.stream()
                    .map(this::mapReasonValueToText) // Chuyển value thành text dễ đọc
                    .collect(Collectors.toList()));
        }
        // Thêm lý do chi tiết nếu người dùng có nhập
        if (reasonDetails != null && !reasonDetails.isBlank()) {
            // Xóa "Lý do khác" mặc định nếu có và thay bằng chi tiết
            fullReasons.remove("Lý do khác");
            fullReasons.add(reasonDetails);
        }
        plan.setReasons(fullReasons);
    }

    private String mapReasonValueToText(String value) {
        return switch (value) {
            case "health" -> "Cải thiện sức khỏe";
            case "family" -> "Vì gia đình và người thân";
            case "financial" -> "Tiết kiệm chi phí";
            case "smell" -> "Loại bỏ mùi hôi";
            case "other" -> "Lý do khác"; // Giữ lại như một placeholder
            default -> value;
        };
    }

    private String generatePlanSuggestion(QuitPlan plan) {
        if (plan.getStages() == null) return "Hãy chọn một phương pháp để có gợi ý chi tiết.";
        return switch (plan.getStages()) {
            case "gradual" -> "Tuần 1: Giảm 2 điếu thuốc mỗi ngày.\nTuần 2: Giảm thêm 3 điếu thuốc mỗi ngày.\nTuần 3: Cố gắng không hút trong 24 giờ.\nLuôn chuẩn bị sẵn kẹo cao su hoặc đồ ăn vặt lành mạnh.";
            case "coldturkey" -> "Vứt bỏ toàn bộ thuốc lá, bật lửa và gạt tàn.\nThông báo cho gia đình và bạn bè về quyết định của bạn để nhận được sự hỗ trợ.\nKhi thèm thuốc, hãy uống một cốc nước lạnh hoặc đi dạo.";
            case "nicotine_replacement" -> "Bắt đầu sử dụng miếng dán nicotine từ ngày " + plan.getStartDate() + ".\nTham khảo ý kiến dược sĩ về liều lượng phù hợp.\nKết hợp với việc thay đổi thói quen (ví dụ: uống trà thay vì cà phê).";
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
     * CẢI TIẾN MỚI: Tính toán các chỉ số quan trọng cho dashboard.
     */
    private QuitStats calculateStats(QuitPlan plan) {
        if (plan.getStartDate() == null || plan.getTargetDate() == null) {
            return new QuitStats(0, 0);
        }

        LocalDate today = LocalDate.now();
        long daysSmokeFree = 0;
        // Số ngày không khói thuốc được tính từ ngày bắt đầu đến hôm nay
        if (!today.isBefore(plan.getStartDate())) {
            daysSmokeFree = ChronoUnit.DAYS.between(plan.getStartDate(), today);
        }

        long daysRemaining = 0;
        // Số ngày còn lại được tính từ hôm nay đến ngày mục tiêu
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
        // Thêm các ô trống cho những ngày trước ngày 1 của tháng
        for (int i = 1; i < firstDayOfWeekValue; i++) {
            days.add(new CalendarDay(0, "empty"));
        }

        // Thêm các ngày trong tháng
        for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
            LocalDate date = currentMonth.atDay(day);
            String status = getDayStatus(date, today, plan.getStartDate(), plan.getTargetDate());
            days.add(new CalendarDay(day, status));
        }

        return new CalendarData(currentMonth.toString(), List.of("T2", "T3", "T4", "T5", "T6", "T7", "CN"), days);
    }

    private String getDayStatus(LocalDate date, LocalDate today, LocalDate startDate, LocalDate targetDate) {
        if (date.isEqual(today)) return "today";
        if (startDate == null || targetDate == null) return "disabled";

        // Logic xác định trạng thái của một ngày trên lịch
        if (date.isBefore(startDate)) return "pre-plan"; // Trước khi bắt đầu
        if (date.isAfter(targetDate)) return "post-plan"; // Đã hoàn thành mục tiêu
        if (date.isBefore(today)) return "past"; // Ngày đã cai thành công trong quá khứ
        return "future"; // Ngày sắp tới trong kế hoạch
    }


    // ===================================================================================
    // CÁC LỚP DÙNG ĐỂ LƯU TRỮ DỮ LIỆU (DATA TRANSFER OBJECTS)
    // ===================================================================================

    /**
     * Đại diện cho kế hoạch cai thuốc của người dùng.
     */
    public static class QuitPlan {
        private List<String> reasons = new ArrayList<>();
        private LocalDate startDate;
        private LocalDate targetDate;
        private String stages;
        private String customPlan;

        // Getters and Setters
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
     * CẢI TIẾN MỚI: Dùng để chứa các chỉ số thống kê cho dashboard.
     * @param daysSmokeFree Số ngày đã không hút thuốc.
     * @param daysRemaining Số ngày còn lại để tới mục tiêu.
     */
    public record QuitStats(long daysSmokeFree, long daysRemaining) {}

    /**
     * Dùng để chứa dữ liệu cần thiết để hiển thị lịch.
     */
    public record CalendarData(String monthName, List<String> weekDays, List<CalendarDay> days) {}

    /**
     * Dùng để chứa thông tin của một ngày trên lịch.
     */
    public record CalendarDay(int dayNumber, String status) {}
}