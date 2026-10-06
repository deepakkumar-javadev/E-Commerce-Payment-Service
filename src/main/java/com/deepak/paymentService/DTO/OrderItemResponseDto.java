package com.deepak.paymentService.DTO;

import lombok.Data;

@Data
public class OrderItemResponseDto {
	
	private Long productId;
	private String productName;
	private Integer quantity;
	private Double price;
	private String skuCode; 
}
