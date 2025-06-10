package com.example.momo.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "status")
    private String status;

    @Column(name = "message", columnDefinition = "NVARCHAR(1000)")
    private String message;

    @Column(name = "userID")
    private Integer userID;

    @Column(name = "userName", columnDefinition = "NVARCHAR(255)")
    private String userName;
}
