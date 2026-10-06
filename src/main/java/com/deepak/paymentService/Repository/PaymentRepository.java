package com.deepak.paymentService.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.deepak.paymentService.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,Long> {

	// razorpay orderId
	

	Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

   
}