package com.deepak.paymentService.DTO;

import com.deepak.paymentService.entity.PaymentStatus;

import lombok.Data;

@Data
public class PaymentResponseDto {


   
	 private Long paymentId;              // Payment table id

	    private Long orderId;                // Your order id

	    private String razorpayOrderId;      // order_xxxxx (Razorpay)

	    private String currency;             // INR

	    private Double amount;               // Display amount (559994)

	    private Long amountInPaise;          // Razorpay amount (55999400)

	    private String key;                  // Razorpay key id

	    //private String paymentLink;
	    
	    private PaymentStatus paymentStatus; // PENDING/SUCCESS/FAILED

	    private String message;
	
	
}
