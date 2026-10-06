package com.deepak.paymentService.service;

import org.json.JSONObject;

import com.deepak.paymentService.DTO.PaymentRequestDto;
import com.deepak.paymentService.DTO.PaymentResponseDto;
import com.deepak.paymentService.DTO.PaymentVerifyReq;
import com.deepak.paymentService.DTO.PaymentVerifyResDto;

public interface PaymentService {

	public PaymentResponseDto createOrder(PaymentRequestDto request);

	public PaymentResponseDto getPayment(Long orderId);
	
	public PaymentVerifyResDto verify(PaymentVerifyReq req);

	public boolean verifySignature(String payload, String signature);
	
	public void handlePaymentCaptured(JSONObject json,String signature);
}
