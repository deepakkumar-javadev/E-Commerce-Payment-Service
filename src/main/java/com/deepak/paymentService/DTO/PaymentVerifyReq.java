package com.deepak.paymentService.DTO;

import lombok.Data;

@Data
public class PaymentVerifyReq {

	private String razorpayOrderId;
	private String razorpayPaymentId;
	private String razorpaySignature;
}
