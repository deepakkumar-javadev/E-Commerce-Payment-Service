package com.deepak.paymentService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.deepak.paymentService.config.FeignConfig;

@FeignClient(name = "ECOM-CART-SERVICE", url = "http://localhost:8085",configuration = FeignConfig.class)
public interface CartClient {

	@DeleteMapping("cart/clear/{userId}")
	public void clearCart(@PathVariable("userId") Long userId);
	
	
}
