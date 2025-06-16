package com.swp2.demo.entity;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quit_plan")
public class QuitPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // Nếu bạn có entity User

    private LocalDate startDate;
    private LocalDate targetDate;
    private String stages;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String customPlan;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL)
    private List<QuitReason> reasons = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public String getStages() {
        return stages;
    }

    public void setStages(String stages) {
        this.stages = stages;
    }

    public String getCustomPlan() {
        return customPlan;
    }

    public void setCustomPlan(String customPlan) {
        this.customPlan = customPlan;
    }

    public List<QuitReason> getReasons() {
        return reasons;
    }

    public void setReasons(List<QuitReason> reasons) {
        this.reasons = reasons;
    }
}
