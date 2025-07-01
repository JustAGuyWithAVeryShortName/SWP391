package com.swp2.demo.Repository;

import com.swp2.demo.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndContentAndCreatedAtBetween(Long userId, String content,
                                                        LocalDateTime start, LocalDateTime end);

    void deleteByUserId(Long userId);
}
