package com.swp2.demo.Controller;

import com.swp2.demo.Repository.OrderRepository;
import com.swp2.demo.Repository.UserRepository;
import com.swp2.demo.Repository.FeedbackRepository; // Assuming you have this
import com.swp2.demo.entity.Member;
import com.swp2.demo.entity.Role;
import com.swp2.demo.entity.User;
import com.swp2.demo.entity.Feedback; // Import Feedback
import com.swp2.demo.entity.dto.RegisterDTO;
import com.swp2.demo.service.FeedbackService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Controller
public class AdminController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeedbackRepository feedbackRepository; // Inject FeedbackRepository

    private final FeedbackService feedbackService;

    public AdminController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/admin/accounts")
    public String viewAllAccounts(Model model) {
        List<User> allUsers = userRepository.findAll();

        List<User> adminAndCoachUsers = allUsers.stream()
                .filter(user -> user.getRole() == Role.Admin || user.getRole() == Role.Coach)
                .toList();
        List<User> normalUsers = allUsers.stream()
                .filter(user -> user.getRole() != Role.Admin && user.getRole() != Role.Coach)
                .toList();

        model.addAttribute("adminCoachUsers", adminAndCoachUsers);
        model.addAttribute("normalUsers", normalUsers);

        return "accounts";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        long totalMembers = userRepository.count();
        long todayMembers = userRepository.countTodayMembers();
        long pendingOrders = orderRepository.countByStatus("PENDING");
        long freeCount = userRepository.countByMember(Member.FREE);
        long vipCount = userRepository.countByMember(Member.VIP);
        long premiumCount = userRepository.countByMember(Member.PREMIUM);
        long totalFeedbacks = feedbackService.getTotalFeedbackCount();


        model.addAttribute("totalMembers", totalMembers);
        model.addAttribute("todayMembers", todayMembers);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("totalFeedbacks", totalFeedbacks);

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

    @PostMapping("/admin/accounts/delete/{id}")
    public String deleteAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User userToDelete = userOptional.get();

            // Check if the user to be deleted has the ADMIN role
            if (userToDelete.getRole() == Role.Admin) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete an account with Admin role.");
            } else {
                try {
                    // Handle Feedback where this user is a coach (if you don't use ON DELETE SET NULL at DB level)
                    // This assumes you have a method like findByCoach in your FeedbackRepository
                    List<Feedback> feedbackAsCoach = feedbackRepository.findAllByCoach(userToDelete);
                    for (Feedback feedback : feedbackAsCoach) {
                        feedback.setCoach(null); // Set coach to null instead of deleting feedback
                        feedbackRepository.save(feedback);
                    }

                    // For Payment and ChatMessage, if they don't have cascade,
                    // you'd need similar manual steps here (e.g., delete associated payments, anonymize chat messages).
                    // Example for Payment (if you want to delete them):
                    // List<Payment> userPayments = paymentRepository.findByUserId(id); // Requires PaymentRepository and method
                    // paymentRepository.deleteAll(userPayments);

                    userRepository.deleteById(id);
                    redirectAttributes.addFlashAttribute("successMessage", "Account deleted successfully!");
                } catch (DataIntegrityViolationException e) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "This account cannot be deleted because it is associated with existing data that prevents direct deletion. Please ensure all related data is handled.");
                    // Log the exception for more details in the server logs
                    System.err.println("DataIntegrityViolationException when deleting user " + id + ": " + e.getMessage());
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Error deleting account: " + e.getMessage());
                    System.err.println("Unexpected error when deleting user " + id + ": " + e.getMessage());
                    e.printStackTrace(); // Print stack trace for debugging
                }
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Account not found with ID: " + id);
        }
        return "redirect:/admin/accounts";
    }

    @GetMapping("/admin/accounts/add")
    public String showAddAccountForm(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "register/formAdmin";
    }

    @PostMapping("/admin/accounts/save")
    public String saveAccount(@Valid RegisterDTO registerDTO, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("registerDTO", registerDTO);
            model.addAttribute("my_error", "Please correct the form errors.");
            return "register/formAdmin";
        }

        if (userRepository.findByUsername(registerDTO.getUsername()) != null) {
            model.addAttribute("my_error", "Username already exists.");
            model.addAttribute("registerDTO", registerDTO);
            return "register/formAdmin"; // Corrected return view name
        }
        if (userRepository.findByEmail(registerDTO.getEmail()) != null) {
            model.addAttribute("my_error", "Email already exists.");
            model.addAttribute("registerDTO", registerDTO);
            return "register/formAdmin"; // Corrected return view name
        }

        try {
            User newUser = new User();
            newUser.setUsername(registerDTO.getUsername());
            // TODO: In a real application, you should hash the password before saving!
            // newUser.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
            newUser.setPassword(registerDTO.getPassword()); // For now, saving as plain text
            newUser.setEmail(registerDTO.getEmail());
            newUser.setFirstName(registerDTO.getFirstName());
            newUser.setLastName(registerDTO.getLastName());
            newUser.setGender(registerDTO.getGender());
            newUser.setDateOfBirth(registerDTO.getDateOfBirth());
            newUser.setRole(registerDTO.getRole());
            newUser.setCreatedAt(LocalDate.now());
            newUser.setMember(Member.FREE);
            newUser.setStatus(com.swp2.demo.entity.Status.OFFLINE);

            userRepository.save(newUser);
            redirectAttributes.addFlashAttribute("successMessage", "Account added successfully!");
            return "redirect:/admin/accounts";
        } catch (Exception e) {
            System.err.println("Error saving new account: " + e.getMessage());
            model.addAttribute("my_error", "An error occurred while adding the account. Please try again.");
            model.addAttribute("registerDTO", registerDTO);
            return "register/formAdmin"; // Corrected return view name
        }
    }
}