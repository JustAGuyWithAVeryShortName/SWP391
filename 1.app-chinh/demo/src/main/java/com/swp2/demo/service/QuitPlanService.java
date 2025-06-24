package com.swp2.demo.service;

import com.swp2.demo.Repository.QuitPlanRepository;
import com.swp2.demo.entity.QuitPlan;
import com.swp2.demo.entity.UserPlanStep;
import com.swp2.demo.repository.UserPlanStepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class QuitPlanService {

    @Autowired
    private QuitPlanRepository repository;

    @Autowired
    private UserPlanStepRepository userPlanStepRepository;

    public QuitPlan save(QuitPlan plan) {
        QuitPlan saved = repository.save(plan);
        generateDailyPlan(saved, 20); // giả sử mặc định 20 điếu/ngày, có thể thay bằng thông số từ user
        return saved;
    }

    public List<QuitPlan> getPlansByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    public boolean hasPlan(Long userId) {
        return repository.existsByUserId(userId);
    }

    public void deleteByUserId(Long userId) {
        repository.deleteByUserId(userId);
    }

    public void generateDailyPlan(QuitPlan quitPlan, int initialCigarettesPerDay) {
        LocalDate start = quitPlan.getStartDate();
        LocalDate end = quitPlan.getTargetDate();

        long totalDays = ChronoUnit.DAYS.between(start, end) + 1;
        int dailyDecrease = (int) Math.ceil((double) initialCigarettesPerDay / totalDays);

        for (int i = 0; i < totalDays; i++) {
            UserPlanStep step = new UserPlanStep();
            step.setQuitPlan(quitPlan);
            step.setDate(start.plusDays(i));
            step.setDayIndex(i + 1);

            int target = Math.max(initialCigarettesPerDay - (i * dailyDecrease), 0);
            step.setTargetCigarettes(target);
            step.setCompleted(false);
            step.setActualCigarettes(null);

            userPlanStepRepository.save(step);
        }
    }
}