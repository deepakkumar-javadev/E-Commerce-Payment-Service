package com.deepak.paymentService.DTO;

import lombok.Data;

@Data
public class NotificationRequestDto {

	private Long orderId;
    private Long userId;
    private String email;
    private String phoneNumber;
    private Double amount;
    private String paymentMethod;
    private String paymentStatus;
    private String transactionId;
    private String notificationType;
}
