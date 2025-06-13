package com.swp2.demo.Repository;

import com.swp2.demo.entity.PasswordResetToken;
import com.swp2.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByToken(String token);

    Optional<PasswordResetToken> findByUser(User user);  // THÊM METHOD NÀY
}
