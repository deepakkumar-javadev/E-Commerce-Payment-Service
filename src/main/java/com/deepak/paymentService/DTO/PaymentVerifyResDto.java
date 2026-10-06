package com.deepak.paymentService.DTO;

import lombok.Data;

@Data
public class PaymentVerifyResDto {

	private String razorpayOrderId;
	private String msg;
}
