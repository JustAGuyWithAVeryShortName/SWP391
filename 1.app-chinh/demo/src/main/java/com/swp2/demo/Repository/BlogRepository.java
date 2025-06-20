package com.swp2.demo.Repository;

import com.swp2.demo.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    long count();
}
