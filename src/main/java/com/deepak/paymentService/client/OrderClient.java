package com.deepak.paymentService.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.deepak.paymentService.DTO.OrderItemResponseDto;
import com.deepak.paymentService.DTO.orderResponseDto;
import com.deepak.paymentService.config.FeignConfig;
import com.deepak.paymentService.entity.PaymentStatus;

@FeignClient(name = "ECOM-ORDER-SERVICE", url = "http://localhost:8086",configuration = FeignConfig.class)
public interface OrderClient {

	// 1. Update payment status
	@PutMapping("orders/{orderId}/payment-order-status")
	public void updatePaymentStatusfromPaymentService(@PathVariable Long orderId,
			@RequestParam(value = "status") PaymentStatus paymentStatus); // "status" explicitly bola

	
	// get order details
	@GetMapping("orders/getorder/{id}")
	public orderResponseDto  getOrders(@PathVariable Long id) ;
	
	// 3. Get order items details 
	@GetMapping("orders/{orderId}/items")
	List<OrderItemResponseDto> getOrderItems(@PathVariable Long orderId);
	
	
}