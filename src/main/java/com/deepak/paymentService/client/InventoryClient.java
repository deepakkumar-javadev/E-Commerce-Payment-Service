package com.deepak.paymentService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.deepak.paymentService.config.FeignConfig;

@FeignClient(name = "ECOM-INVENTORY-SERVICE" , url = "http://localhost:8084",configuration = FeignConfig.class)
public interface InventoryClient {

	@PutMapping("stock/reduce/{skuCode}")
	public ResponseEntity<String> reduceInventoryBySkuCode(@PathVariable String skuCode,
			@RequestParam Integer quantity);
}
