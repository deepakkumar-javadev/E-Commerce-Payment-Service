package com.deepak.paymentService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.deepak.paymentService.DTO.NotificationRequestDto;
import com.deepak.paymentService.config.FeignConfig;

@FeignClient(name = "ECOM-NOTIFICATION-SERVICE", url = "http://localhost:8088",configuration = FeignConfig.class)
public interface NotificationClient {

	@PostMapping("notification/send")
	
	
	public ResponseEntity<String> sendNotification(@RequestBody NotificationRequestDto request);
}
