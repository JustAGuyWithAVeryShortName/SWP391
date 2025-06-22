package com.swp2.demo.Controller;

import com.swp2.demo.Repository.OrderRepository;
import com.swp2.demo.Repository.UserRepository;
import com.swp2.demo.entity.Member;
import com.swp2.demo.entity.Order;
import com.swp2.demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;



    @GetMapping
    public String listOrders(Model model) {
        List<Order> orders = orderRepository.findAll();
        model.addAttribute("orders", orders);
        return "admin-orders"; // Tạo file admin-orders.html
    }

    @PostMapping("/{orderId}/confirm")
    public String confirmOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if ("PAID".equals(order.getStatus())) {
            return "redirect:/admin/orders"; // Đã xác nhận rồi thì bỏ qua
        }

        order.setStatus("PAID");
        order.setConfirmedAt(LocalDateTime.now());

        // ➔ Cập nhật User sang Member tương ứng
        User user = order.getUser();
        user.setMember(order.getMemberPlan());

        orderRepository.save(order);
        userRepository.save(user);

        return "redirect:/admin/orders"; // Trang quản lý đơn hàng
    }

    @PostMapping("/{orderId}/delete")
    public String deleteOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        User user = order.getUser();

        // Nếu đơn đã được xác nhận → cần reset Member của User về FREE hoặc null
        if ("PAID".equals(order.getStatus()) && user != null) {
            user.setMember(null); // hoặc user.setMember(Member.FREE);
            userRepository.save(user);
        }

        orderRepository.delete(order);
        return "redirect:/admin/orders";
    }

}
