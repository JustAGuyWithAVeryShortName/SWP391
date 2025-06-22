package com.swp2.demo.Repository;

import com.swp2.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    long count();

    User findByUsername(String username);
    User findByEmail(String email);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt = CURRENT_DATE")
    long countTodayMembers();

}
