package com.swp2.demo.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "quit_reason")
public class QuitReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String reasonText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private QuitPlan plan;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReasonText() {
        return reasonText;
    }

    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }

    public QuitPlan getPlan() {
        return plan;
    }

    public void setPlan(QuitPlan plan) {
        this.plan = plan;
    }
}
