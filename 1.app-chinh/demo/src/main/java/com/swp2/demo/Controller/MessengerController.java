package com.swp2.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class MessengerController {

    // Cấu trúc record đã tốt, giữ nguyên
    public record User(long id, String name, String avatarUrl, boolean isOnline) {}
    public record Conversation(long id, User partner, String lastMessage, String time, boolean isStarred, boolean isActive) {}
    public record Message(long id, String content, long senderId, User sender, LocalDateTime timestamp) {}

    @GetMapping("/messenger")
    public String getMessengerPage(Model model) {
        User currentUser = new User(1L, "Current User", "https://i.pravatar.cc/150?img=10", true);
        User marzia = new User(2L, "Marzia Mithila", "https://i.pravatar.cc/150?img=1", true);
        User khalid = new User(3L, "Khalid Hasan", "https://i.pravatar.cc/150?img=32", false);
        User pewdiepie = new User(4L, "PewDiePie", "https://i.pravatar.cc/150?img=33", true);
        User rasel = new User(5L, "Rasel Ahmed", "https://i.pravatar.cc/150?img=35", false);
        User maidul = new User(6L, "Maidul Islam Saad", "https://i.pravatar.cc/150?img=40", false);
        User earid = new User(7L, "Earid Ahmed", "https://i.pravatar.cc/150?img=45", true);

        List<Conversation> conversations = List.of(
                new Conversation(1L, khalid, "Sup man! How is it going?", "9:30pm", true, false),
                new Conversation(2L, pewdiepie, "Hãy chắc chắn rằng bạn đã subscribe kênh của tôi nhé!", "6:30pm", true, false),
                new Conversation(3L, marzia, "Ok, em sẽ ở The Coffee House.", "3:00pm", true, true), // Active
                new Conversation(4L, rasel, "Đây là link: https://www.youtube.com/...", "11:00am", false, false),
                new Conversation(5L, maidul, "Valkotodin tore dekha...", "Hôm qua", true, false),
                new Conversation(6L, earid, "Hager Hager Dular karna tesi", "Hôm qua", true, false)
        );

        // CẢI TIẾN: Dữ liệu tin nhắn đa dạng hơn để test UI
        List<Message> messages = List.of(
                new Message(1L, "Chào em, em đang làm gì đó?", 1L, currentUser, LocalDateTime.now().minusDays(1).withHour(15).withMinute(0)),
                new Message(2L, "Em đang đọc sách thôi ạ.", 2L, marzia, LocalDateTime.now().minusDays(1).withHour(15).withMinute(1)),
                new Message(3L, "Hay quá ha. Em có muốn đi uống cafe chiều nay không?", 1L, currentUser, LocalDateTime.now().minusDays(1).withHour(15).withMinute(2)),
                new Message(4L, "Một ý kiến hay đó anh. Em cũng đang muốn ra ngoài một chút.", 1L, currentUser, LocalDateTime.now().minusDays(1).withHour(15).withMinute(2)),
                new Message(5L, "Vậy hẹn em 4h chiều nay ở The Coffee House nhé. Một nơi có không gian rất tuyệt vời để đọc sách và trò chuyện.", 1L, currentUser, LocalDateTime.now().minusDays(1).withHour(15).withMinute(3)),
                new Message(6L, "Dạ vâng ạ. Tuyệt vời!", 2L, marzia, LocalDateTime.now().withHour(9).withMinute(30)),
                new Message(7L, "Ok, em sẽ ở The Coffee House.", 2L, marzia, LocalDateTime.now().withHour(9).withMinute(31))
        );

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("conversations", conversations);
        model.addAttribute("activeConversationPartner", marzia);
        model.addAttribute("messages", messages);

        return "messenger";
    }
}