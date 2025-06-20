package com.swp2.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

@Controller
public class HomeController {

    // Lớp nội tại (inner class) để biểu diễn dữ liệu mẫu một cách rõ ràng
    static class LeaderboardUser {
        private final int rank;
        private final String name;
        private final int daysSmokeFree;

        public LeaderboardUser(int rank, String name, int daysSmokeFree) {
            this.rank = rank;
            this.name = name;
            this.daysSmokeFree = daysSmokeFree;
        }

        // Getters
        public int getRank() { return rank; }
        public String getName() { return name; }
        public int getDaysSmokeFree() { return daysSmokeFree; }
    }

    static class BlogPost {
        private final String title;
        private final String excerpt;
        private final String imageUrl;

        public BlogPost(String title, String excerpt, String imageUrl) {
            this.title = title;
            this.excerpt = excerpt;
            this.imageUrl = imageUrl;
        }

        // Getters
        public String getTitle() { return title; }
        public String getExcerpt() { return excerpt; }
        public String getImageUrl() { return imageUrl; }
    }


    @GetMapping({"/", "/home"})
    public String home(Model model) {
        // --- Dữ liệu mẫu cho Bảng Xếp Hạng ---
        List<LeaderboardUser> leaderboard = Arrays.asList(
                new LeaderboardUser(1, "Thanh Nguyen", 152),
                new LeaderboardUser(2, "Minh Tran", 140),
                new LeaderboardUser(3, "An Le", 118),
                new LeaderboardUser(4, "Hoa Pham", 95)
        );

        // --- Dữ liệu mẫu cho Blog (ĐÃ CẬP NHẬT TÊN ẢNH) ---
        List<BlogPost> posts = Arrays.asList(
                new BlogPost("5 Mẹo Vượt Qua Cơn Thèm Thuốc Lá", "Học cách kiểm soát cơn thèm thuốc chỉ trong vài phút với những phương pháp đã được chứng minh...", "/images/11.png"),
                new BlogPost("Hành Trình Bỏ Thuốc Của Tôi", "Chia sẻ từ một người dùng đã thành công: từ điếu thuốc đầu tiên đến 365 ngày không khói thuốc.", "/images/12.png"),
                new BlogPost("Lợi Ích Sức Khỏe Bạn Nhận Được Sau 24h", "Bạn sẽ ngạc nhiên về những thay đổi tích cực của cơ thể chỉ sau một ngày ngừng hút thuốc.", "/images/13.png")
        );

        // Thêm dữ liệu vào model để Thymeleaf có thể truy cập
        model.addAttribute("leaderboardUsers", leaderboard);
        model.addAttribute("blogPosts", posts);

        return "home"; // Trả về file home.html
    }
}