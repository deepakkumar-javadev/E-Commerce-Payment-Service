package com.deepak.paymentService.DTO;

import lombok.Data;

@Data
public class orderResponseDto {

	private Long orderId;

	private String orderNumber;

	private Long userId;

	private Double totalAmount;

	private String paymentMethod;

	private String orderStatus;

	private String paymentStatus;

}
