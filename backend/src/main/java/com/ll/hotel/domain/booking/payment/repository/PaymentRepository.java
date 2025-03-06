package com.ll.hotel.domain.booking.payment.repository;

import com.ll.hotel.domain.booking.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    public boolean existsByMerchantUid(String merchantUid);
}
