package com.swp2.demo.service;

import com.swp2.demo.Repository.QuitPlanRepository;
import com.swp2.demo.entity.QuitPlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuitPlanService {

    @Autowired
    private QuitPlanRepository repository;

    public QuitPlan save(QuitPlan plan) {
        return repository.save(plan);
    }

    public List<QuitPlan> getPlansByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

}
