package com.swp2.demo.Controller;

import java.util.Map;

import com.swp2.demo.entity.dto.FeedbackRequest; // Ensure this package matches your project structure
import com.swp2.demo.entity.Feedback;     // Ensure this package matches your project structure
import com.swp2.demo.service.FeedbackService; // Ensure this package matches your project structure
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()") // Only authenticated users can submit feedback
    public ResponseEntity<?> submitFeedback(@RequestBody FeedbackRequest feedbackRequest) {
        // Basic validation: ensure either rating or comment is provided
        if (feedbackRequest.getRating() == null && (feedbackRequest.getComment() == null || feedbackRequest.getComment().trim().isEmpty())) {
            return ResponseEntity.badRequest().body("Rating or comment is required.");
        }

        if (feedbackRequest.getRating() == null && (feedbackRequest.getComment() == null || feedbackRequest.getComment().trim().isEmpty())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Rating or comment is required.")); // TRẢ VỀ JSON
        }

        try {
            Feedback savedFeedback = feedbackService.saveFeedback(feedbackRequest.getRating(), feedbackRequest.getComment());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFeedback);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage())); // TRẢ VỀ JSON
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage())); // TRẢ VỀ JSON
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to submit feedback: " + e.getMessage())); // TRẢ VỀ JSON
        }
    }
}