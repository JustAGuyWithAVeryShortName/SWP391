package com.example.momo.repository;


import com.example.momo.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByOrderId(String orderId);
}
