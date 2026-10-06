package com.deepak.paymentService.kafka;

import com.deepak.paymentService.entity.PaymentStatus;

import lombok.Data;

@Data
public class PaymentSuccessEvent {

	private Long orderId;
	private Long paymentId;
	private Double amount;
	private String paymentMethod;
	private PaymentStatus paymentStatus;
	private String transactionId;
}
