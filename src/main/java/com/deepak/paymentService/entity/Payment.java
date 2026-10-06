package com.deepak.paymentService.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "payments", indexes = { 
		@Index(name = "idx_payment_order_id", columnList = "order_id"),
		@Index(name = "idx_payment_razorpay_order_id", columnList = "razorpay_order_id"),
		@Index(name = "idx_payment_razorpay_payment_id", columnList = "razorpay_payment_id"),
		@Index(name = "idx_payment_transaction_id", columnList = "transaction_id") })
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long paymentId; // at payment service called..

	private Long orderId; // after order service called

	private Double amount;

	private String currency;

	private String paymentMethod;

	@Enumerated(EnumType.STRING)
	private PaymentStatus paymentStatus;

	private String transactionId;

	// private String paymentLink;

	private String razorpayOrderId; // order_R3xK9AbCdEf123 // after payment done

	private String razorpayPaymentId; // pay_R4LmN8PqRsT56 // after payment done

	private String razorpaySignature; // verification ke liye // after payment done

	// private String razorpayPaymentLinkId;

	private LocalDateTime paymentDate;

}
