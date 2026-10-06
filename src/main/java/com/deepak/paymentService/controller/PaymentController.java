package com.deepak.paymentService.controller;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deepak.paymentService.DTO.PaymentRequestDto;
import com.deepak.paymentService.DTO.PaymentResponseDto;
import com.deepak.paymentService.Repository.PaymentRepository;
import com.deepak.paymentService.Util.RazorpaySignatureUtil;
import com.deepak.paymentService.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

	private final PaymentRepository paymentRepository;

	private final PaymentService service;

	private final RazorpaySignatureUtil signatureUtil;

	@PostMapping("/createOrder")
	public PaymentResponseDto createPaymentOrder(@RequestBody PaymentRequestDto request) {

		return service.createOrder(request);
	}

	@PostMapping("/webhook")
	public ResponseEntity<String> handleWebhook(@RequestBody String payload,
			@RequestHeader("X-Razorpay-Signature") String signature) {

		System.out.println("===== WEBHOOK RECEIVED =====");

		System.out.println("Payload = " + payload);

		System.out.println("Signature = " + signature);

		try {

			// =========================
			// 1. Verify Webhook Signature
			// =========================

			boolean isValid = signatureUtil.verifySignature(payload, signature);

			System.out.println("Signature Valid = " + isValid);

			if (!isValid) {

				System.out.println("INVALID RAZORPAY SIGNATURE");

				return ResponseEntity.badRequest().body("INVALID SIGNATURE");
			}

			// =========================
			// 2. Convert payload to JSON
			// =========================

			JSONObject json = new JSONObject(payload);

			// =========================
			// 3. Get Event
			// =========================

			String event = json.getString("event");

			System.out.println("Event = " + event);

			// =========================
			// 4. Payment Link Paid
			// =========================

			if ("payment.captured".equals(event)) {

				System.out.println("Calling handlePaymentCaptured()");

				service.handlePaymentCaptured(json, signature);

				System.out.println("handlePaymentCaptured() completed");
			}

			return ResponseEntity.ok("Webhook received");

		} catch (Exception e) {

			e.printStackTrace();

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook processing failed");
		}
	}

	@GetMapping("get/{orderId}")
	public PaymentResponseDto getPayment(@PathVariable Long orderId) {

		return service.getPayment(orderId);
	}
}
