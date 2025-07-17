package com.swp2.demo.service;

import java.util.List;
import java.util.Optional;

import com.swp2.demo.entity.Feedback; // Your package
import com.swp2.demo.entity.User;     // Your package
import com.swp2.demo.repository.FeedbackRepository; // Your package
import com.swp2.demo.repository.UserRepository;     // Your package
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public FeedbackService(FeedbackRepository feedbackRepository, UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Feedback saveFeedback(Integer rating, String comment) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User is not authenticated to submit feedback.");
        }

        String username;
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            // Đối với người dùng đăng nhập bằng username/password thông thường
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof OidcUser) { // <-- THÊM ĐOẠN NÀY ĐỂ XỬ LÝ OIDC USER
            OidcUser oidcUser = (OidcUser) principal;
            // OidcUser.getName() trả về 'sub' claim (ID duy nhất).
            // Nếu bạn muốn lưu 'email' hoặc 'name' (full name) làm username trong DB của bạn,
            // bạn cần truy cập claim đó.
            // Ví dụ: username = oidcUser.getEmail();
            // Hoặc: username = oidcUser.getClaimAsString("name"); // Lấy tên đầy đủ từ claims
            // Giả sử bạn muốn dùng email của OIDC user làm username trong DB của bạn
            username = oidcUser.getEmail(); // HOẶC oidcUser.getClaimAsString("email");
            if (username == null || username.isEmpty()) {
                // Fallback nếu email không có, dùng 'sub' claim
                username = oidcUser.getName(); // getName() của OIDC user là 'sub' claim
            }
        } else if (principal instanceof OAuth2User) { // <-- THÊM ĐOẠN NÀY ĐỂ XỬ LÝ OAuth2User (không phải OIDC)
            OAuth2User oauth2User = (OAuth2User) principal;
            // Đối với các nhà cung cấp OAuth2 khác (ví dụ: GitHub), `getName()` thường trả về ID hoặc username của họ
            username = oauth2User.getName();
            // Hoặc truy cập các thuộc tính khác trong attributes của OAuth2User nếu bạn cần
            // Ví dụ: username = oauth2User.getAttribute("login"); // Đối với GitHub
            // username = oauth2User.getAttribute("email"); // Nếu có email
        }
        else if (principal instanceof User) {
            // Nếu principal là một đối tượng User của chính bạn (ít phổ biến khi sử dụng UserDetails)
            username = ((User) principal).getUsername();
        } else if (principal instanceof String) {
            // Trường hợp principal là một chuỗi (ví dụ: "anonymousUser" hoặc username đơn giản)
            username = (String) principal;
        } else {
            // Không thể xác định kiểu người dùng
            throw new IllegalStateException("Could not determine username from authentication principal type: " + principal.getClass().getName());
        }

        // Sau khi đã có username, tiếp tục tìm kiếm người dùng trong repository
        User currentUser = userRepository.findByUsername(username);

        if (currentUser == null) {
            // Đây là một vấn đề tiềm ẩn: Nếu người dùng OAuth/OIDC chưa tồn tại trong DB của bạn.
            // Bạn cần logic để tự động đăng ký người dùng này hoặc tạo một tài khoản mới cho họ.
            // Nếu không, lỗi này sẽ xảy ra khi người dùng đăng nhập bằng OAuth lần đầu.
            // Tạm thời, chúng ta sẽ ném RuntimeException.
            throw new RuntimeException("Authenticated user with username '" + username + "' not found in the database. " +
                "Please check your UserRepository.findByUsername implementation or implement user registration for OAuth/OIDC users.");
        }

        // Create the Feedback entity
        Feedback feedback = new Feedback(rating, comment, currentUser);

        return feedbackRepository.save(feedback);
    }
    public long getTotalFeedbackCount() {
        return feedbackRepository.count();
    }
    @Transactional // Deletion operations should typically be transactional
    public void deleteFeedbackById(Long id) {
        // It's good practice to check if the entity exists before attempting to delete
        // to avoid DataIntegrityViolationException if foreign keys are involved
        // or just to provide a more specific error message.
        if (feedbackRepository.existsById(id)) {
            feedbackRepository.deleteById(id);
        } else {
            // Optionally throw an exception if the feedback doesn't exist
            throw new IllegalArgumentException("Feedback with ID " + id + " not found.");
        }
    }
    public List<Feedback> findAllFeedback() {
        return feedbackRepository.findAll();
    }

    public Optional<Feedback> findById(Long id) {
        return feedbackRepository.findById(id);
    }
}
