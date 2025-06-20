package com.swp2.demo.Repository;


import com.swp2.demo.entity.AnalysisResultEntity;
import com.swp2.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResultEntity, Long> {
    List<AnalysisResultEntity> findByUser(User user);
    void deleteByUser(User user);
}