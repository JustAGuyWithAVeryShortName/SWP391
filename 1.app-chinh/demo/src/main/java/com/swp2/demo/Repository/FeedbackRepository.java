package com.swp2.demo.Repository;

import com.swp2.demo.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findTop5ByOrderByIdDesc(); // Latest feedback
}
