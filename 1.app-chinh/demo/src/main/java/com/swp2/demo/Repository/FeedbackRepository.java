package com.swp2.demo.Repository;

import com.swp2.demo.entity.Feedback;
import com.swp2.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findTop5ByOrderByIdDesc(); // Latest feedback
    List<Feedback> findAllByCoach(User coach);
    // You might also need findByUser for the 'authored' feedback if not using cascades
    List<Feedback> findAllByUser(User user);
}
